package com.xs0.asyncdb.mysql.codec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.xs0.asyncdb.common.Configuration;
import com.xs0.asyncdb.common.ExecutionContext;
import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.common.general.MutableResultSet;
import com.xs0.asyncdb.mysql.binary.BinaryRowDecoder;
import com.xs0.asyncdb.mysql.message.client.*;
import com.xs0.asyncdb.mysql.message.server.*;
import com.xs0.asyncdb.mysql.util.CharsetMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLConnectionHandler extends SimpleChannelInboundHandler<Object> {
    private final Configuration configuration;
    private final MySQLHandlerDelegate handlerDelegate;
    private final EventLoopGroup group;
    private final ExecutionContext executionContext;
    private final String connectionId;

    private final Logger log;
    private final Bootstrap bootstrap;
    private final CompletableFuture<MySQLConnectionHandler> connectionPromise;
    private final MySQLFrameDecoder decoder;
    private final MySQLOneToOneEncoder encoder;

    private final SendLongDataEncoder sendLongDataEncoder = SendLongDataEncoder.instance();
    private final ArrayList<ColumnDefinitionMessage> currentParameters = new ArrayList<>();
    private final ArrayList<ColumnDefinitionMessage> currentColumns = new ArrayList<>();
    private final HashMap<String, PreparedStatementHolder> parsedStatements = new HashMap<>();
    private final BinaryRowDecoder binaryRowDecoder = BinaryRowDecoder.instance();

    private PreparedStatementHolder currentPreparedStatementHolder = null;
    private PreparedStatement currentPreparedStatement = null;
    private MutableResultSet<ColumnDefinitionMessage> currentQuery = null;
    private ChannelHandlerContext currentContext = null;

    public MySQLConnectionHandler(Configuration configuration,
                                  CharsetMapper charsetMapper,
                                  MySQLHandlerDelegate handlerDelegate,
                                  EventLoopGroup group,
                                  ExecutionContext executionContext,
                                  String connectionId) {

        this.configuration = configuration;
        this.handlerDelegate = handlerDelegate;
        this.group = group;
        this.executionContext = executionContext;
        this.connectionId = connectionId;

        this.log = LoggerFactory.getLogger("[connection-handler]" + connectionId);
        this.bootstrap = new Bootstrap().group(this.group);
        this.connectionPromise = new CompletableFuture<>();
        this.decoder = new MySQLFrameDecoder(configuration.charset, connectionId);
        this.encoder = new MySQLOneToOneEncoder(configuration.charset, charsetMapper);
    }

    public CompletableFuture<MySQLConnectionHandler> connect() {
        this.bootstrap.channel(NioSocketChannel.class);

        this.bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(
                    decoder,
                    encoder,
                    sendLongDataEncoder,
                    MySQLConnectionHandler.this
                );
            }
        });

        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        this.bootstrap.option(ChannelOption.ALLOCATOR, LittleEndianByteBufAllocator.instance());

        ChannelFuture connectFuture = this.bootstrap.connect(new InetSocketAddress(configuration.host, configuration.port));
        connectFuture.addListener(f -> {
            if (f.isSuccess()) {
                // nothing?
            } else {
                connectionPromise.completeExceptionally(f.cause());
            }
        });

        return this.connectionPromise;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object message) {
        if (message instanceof ServerMessage) {
            ServerMessage m = (ServerMessage) message;
            switch (m.kind()) {
                case ServerMessage.SERVER_PROTOCOL_VERSION: {
                    handlerDelegate.onHandshake((HandshakeMessage) m);
                    break;
                }

                case ServerMessage.OK: {
                    this.clearQueryState();
                    handlerDelegate.onOk((OkMessage) m);
                    break;
                }

                case ServerMessage.ERROR: {
                    this.clearQueryState();
                    handlerDelegate.onError((ErrorMessage) m);
                    break;
                }

                case ServerMessage.EOF: {
                    this.handleEOF(m);
                    break;
                }

                case ServerMessage.COLUMN_DEFINITION: {
                    ColumnDefinitionMessage colDef = (ColumnDefinitionMessage)m;

                    if (currentPreparedStatementHolder != null && this.currentPreparedStatementHolder.needsAny()) {
                        this.currentPreparedStatementHolder.add(colDef);
                    }

                    this.currentColumns.add(colDef);

                    break;
                }

                case ServerMessage.COLUMN_DEFINITION_FINISHED: {
                    this.onColumnDefinitionFinished();
                    break;
                }

                case ServerMessage.PREPARED_STATEMENT_PREPARE_RESPONSE: {
                    this.onPreparedStatementPrepareResponse((PreparedStatementPrepareResponse)m);
                    break;
                }

                case ServerMessage.ROW: {
                    ResultSetRowMessage rowMsg = (ResultSetRowMessage) m;

                    Object[] items = new Object[rowMsg.size()];

                    int x = 0;
                    while (x < rowMsg.size()) {
                        if (rowMsg.get(x) == null) {
                            items[x] = null;
                        } else {
                            ColumnDefinitionMessage colDef = this.currentQuery.columnTypes.get(x);
                            colDef.textDecoder.decode(colDef, rowMsg.get(x), configuration.charset);
                        }
                        x++;
                    }

                    this.currentQuery.addRow(items);
                    break;
                }

                case ServerMessage.BINARY_ROW: {
                    BinaryRowMessage rowMsg = (BinaryRowMessage) m;
                    this.currentQuery.addRow(this.binaryRowDecoder.decode(rowMsg.buffer, this.currentColumns));
                    break;
                }

                case ServerMessage.PARAM_PROCESSING_FINISHED: {
                    break;
                }

                case ServerMessage.PARAM_AND_COLUMN_PROCESSING_FINISHED: {
                    this.onColumnDefinitionFinished();
                    break;
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel became active");
        handlerDelegate.connected(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel became inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof CodecException) {
            handleException(cause.getCause());
        } else {
            handleException(cause);
        }
    }

    private void handleException(Throwable cause) {
        if (!this.connectionPromise.isDone()) {
            this.connectionPromise.completeExceptionally(cause);
        }

        handlerDelegate.exceptionCaught(cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.currentContext = ctx;
    }

    public ChannelFuture write(QueryMessage message)  {
        this.decoder.queryProcessStarted();
        return writeAndHandleError(message);
    }

    public ChannelFuture sendPreparedStatement(String query, List<Object> values) {
        this.currentColumns.clear();
        this.currentParameters.clear();

        this.currentPreparedStatement = new PreparedStatement(query, values);

        PreparedStatementHolder existing = this.parsedStatements.get(query);
        if (existing != null) {
            return executePreparedStatement(existing.statementId(), existing.columns.size(), values, existing.parameters);
        } else {
            decoder.preparedStatementPrepareStarted();
            return writeAndHandleError(new PreparedStatementPrepareMessage(query));
        }
    }

    public ChannelFuture write(HandshakeResponseMessage message) {
        decoder.hasDoneHandshake = true;
        return writeAndHandleError(message);
    }

    public ChannelFuture write(AuthenticationSwitchResponse message) {
        return writeAndHandleError(message);
    }

    public ChannelFuture write(QuitMessage message) {
        return writeAndHandleError(message);
    }

    public ChannelFuture disconnect() {
        return this.currentContext.close();
    }

    public void clearQueryState() {
        this.currentColumns.clear();
        this.currentParameters.clear();
        this.currentQuery = null;
    }

    public boolean isConnected() {
        if (this.currentContext != null && this.currentContext.channel() != null) {
            return this.currentContext.channel().isActive();
        } else {
            return false;
        }
    }

    ChannelFuture executePreparedStatement(byte[] statementId, int columnsCount, List<Object> values, List<ColumnDefinitionMessage> parameters) {
        decoder.preparedStatementExecuteStarted(columnsCount, parameters.size());
        this.currentColumns.clear();
        this.currentParameters.clear();

        HashSet<Integer> nonLongIndices = new HashSet<>();

        int index = 0;
        for (Object value : values) {
            if (!isLong(value)) {
                nonLongIndices.add(index);
            }
            index++;
        }

        // quick case - all values are non-long, so we just send them along
        if (nonLongIndices.size() == values.size())
            return writeAndHandleError(new PreparedStatementExecuteMessage(statementId, values, nonLongIndices, parameters));

        // long case - we send the parameters one by one, finally resolving this promise after everything is done
        ChannelPromise promise = currentContext.newPromise();
        continueSendingLongParameters(statementId, values, parameters, 0, promise, nonLongIndices);
        return promise;
    }

    void continueSendingLongParameters(byte[] statementId, List<Object> values, List<ColumnDefinitionMessage> parameters, int currentIndex, ChannelPromise finalPromise, HashSet<Integer> nonLongIndices) {
        while (currentIndex < values.size() && nonLongIndices.contains(currentIndex))
            currentIndex++;

        if (currentIndex < values.size()) {
            // send next long value and call back here

            int nextIndex = currentIndex + 1;
            ChannelFuture future = sendLongParameter(statementId, currentIndex, values.get(currentIndex));
            future.addListener(f -> {
                if (f.isSuccess()) {
                    continueSendingLongParameters(statementId, values, parameters, nextIndex, finalPromise, nonLongIndices);
                } else
                if (f.isCancelled()) {
                    finalPromise.cancel(false);
                } else {
                    finalPromise.tryFailure(f.cause());
                }
            });
        } else {
            // we're done

            ChannelFuture future = writeAndHandleError(new PreparedStatementExecuteMessage(statementId, values, nonLongIndices, parameters));
            future.addListener(f -> {
                if (f.isSuccess()) {
                    finalPromise.trySuccess();
                } else
                if (f.isCancelled()) {
                    finalPromise.cancel(false);
                } else {
                    finalPromise.tryFailure(f.cause());
                }
            });
        }
    }

    boolean isLong(Object value) {
        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            return bytes.length > SendLongDataEncoder.LONG_THRESHOLD;
        }
        if (value instanceof ByteBuf) {
            ByteBuf bytes = (ByteBuf) value;
            return bytes.readableBytes() > SendLongDataEncoder.LONG_THRESHOLD;
        }
        if (value instanceof ByteBuffer) {
            ByteBuffer bytes = (ByteBuffer) value;
            return bytes.remaining() > SendLongDataEncoder.LONG_THRESHOLD;
        }

        return false;
    }

    private ChannelFuture sendLongParameter(byte[] statementId, int index, Object longValue) {
        ByteBuf buffer;

        if (longValue instanceof ByteBuf) {
            buffer = (ByteBuf) longValue;
        } else
        if (longValue instanceof byte[]) {
            buffer = Unpooled.wrappedBuffer((byte[]) longValue);
        } else
        if (longValue instanceof ByteBuffer) {
            buffer = Unpooled.wrappedBuffer((ByteBuffer) longValue);
        } else {
            throw new IllegalStateException("Unknown long parameter type"); // see isLong
        }

        return writeAndHandleError(new SendLongDataMessage(statementId, buffer, index));
    }

    private void onPreparedStatementPrepareResponse(PreparedStatementPrepareResponse message) {
        this.currentPreparedStatementHolder = new PreparedStatementHolder(this.currentPreparedStatement.statement, message);
    }

    void onColumnDefinitionFinished() {
        ArrayList<ColumnDefinitionMessage> columns;

        if (this.currentPreparedStatementHolder != null) {
            columns = this.currentPreparedStatementHolder.columns;
        } else {
            columns = this.currentColumns;
        }

        this.currentQuery = new MutableResultSet<>(columns);

        if (this.currentPreparedStatementHolder != null) {
            this.parsedStatements.put(this.currentPreparedStatementHolder.statement, this.currentPreparedStatementHolder);

            this.executePreparedStatement(
                this.currentPreparedStatementHolder.statementId(),
                this.currentPreparedStatementHolder.columns.size(),
                this.currentPreparedStatement.values,
                this.currentPreparedStatementHolder.parameters
            );

            this.currentPreparedStatementHolder = null;
            this.currentPreparedStatement = null;
        }
    }

    private ChannelFuture writeAndHandleError(Object message) {
        if (this.currentContext.channel().isActive()) {
            ChannelFuture res = this.currentContext.writeAndFlush(message);

            res.addListener(future -> {
                if (!future.isSuccess()) {
                    handleException(future.cause());
                }
            });

            return res;
        } else {
            DatabaseException error = new DatabaseException("This channel is not active and can't take messages");
            handleException(error);
            return this.currentContext.channel().newFailedFuture(error);
        }
    }

    void handleEOF(ServerMessage m) {
        if (m instanceof EOFMessage) {
            EOFMessage eof = (EOFMessage) m;

            MutableResultSet<ColumnDefinitionMessage> resultSet = this.currentQuery;
            this.clearQueryState();

            if (resultSet != null) {
                handlerDelegate.onResultSet(resultSet, eof);
            } else {
                handlerDelegate.onEOF(eof);
            }
        } else
        if (m instanceof AuthenticationSwitchRequest) {
            AuthenticationSwitchRequest authenticationSwitch = (AuthenticationSwitchRequest) m;
            handlerDelegate.switchAuthentication(authenticationSwitch);
        }
    }

    void schedule(Runnable block, Duration duration) {
        this.currentContext.channel().eventLoop().schedule(block, duration.toMillis(), TimeUnit.MILLISECONDS);
    }
}