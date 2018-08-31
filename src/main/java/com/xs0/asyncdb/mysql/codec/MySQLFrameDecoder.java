/*
package com.xs0.asyncdb.mysql.codec;

import com.xs0.asyncdb.common.exceptions.BufferNotFullyConsumedException;
import com.xs0.asyncdb.common.exceptions.NegativeMessageSizeException;
import com.xs0.asyncdb.common.exceptions.ParserNotAvailableException;
import com.xs0.asyncdb.common.util.BufferDumper;
import com.xs0.asyncdb.mysql.decoder.*;
import com.xs0.asyncdb.mysql.message.server.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.read3ByteInt;
import static com.xs0.asyncdb.mysql.binary.ByteBufUtils.readBinaryLength;


public class MySQLFrameDecoder extends ByteToMessageDecoder {
    private final Charset charset;
    private final String connectionId;

    private final Logger log;
    private final AtomicInteger messagesCount;
    private final ErrorDecoder errorDecoder;
    private final OkDecoder okDecoder;
    private final ResultSetRowDecoder rowDecoder;
    private final PreparedStatementPrepareResponseDecoder preparedStatementPrepareDecoder;
    private final AuthenticationSwitchRequestDecoder authenticationSwitchDecoder;

    public MySQLFrameDecoder(Charset charset, String connectionId, DecoderRegistry decoderRegistry) {
        this.charset = charset;
        this.connectionId = connectionId;

        log = LoggerFactory.getLogger("[frame-decoder]" + connectionId);
        messagesCount = new AtomicInteger();
        errorDecoder = new ErrorDecoder(charset);
        okDecoder = new OkDecoder(charset);
        rowDecoder = new ResultSetRowDecoder(charset);
        preparedStatementPrepareDecoder = PreparedStatementPrepareResponseDecoder.instance();
        authenticationSwitchDecoder = new AuthenticationSwitchRequestDecoder(charset);
    }


    boolean processingColumns = false;
    boolean processingParams = false;
    boolean isInQuery = false;
    boolean isPreparedStatementPrepare = false;
    boolean isPreparedStatementExecute = false;
    boolean isPreparedStatementExecuteRows = false;
    boolean hasDoneHandshake = false;

    long totalParams = 0L;
    long processedParams = 0L;
    long totalColumns = 0L;
    long processedColumns = 0L;

    private boolean hasReadColumnsCount = false;

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
        System.err.println("decode called with " + buffer);

        if (buffer.readableBytes() > 4) {
            buffer.markReaderIndex();
            int size = read3ByteInt(buffer);

            int sequence = buffer.readUnsignedByte(); // we have to read this

            if (buffer.readableBytes() >= size) {
                messagesCount.incrementAndGet();

                int messageType = buffer.getByte(buffer.readerIndex());

                if (size < 0) {
                    throw new NegativeMessageSizeException(messageType, size);
                }

                ByteBuf slice = buffer.readSlice(size);

                if (log.isTraceEnabled()) {
                    log.trace("Reading message type {} - " +
                              "(count={},hasDoneHandshake={},size={},isInQuery={},processingColumns={}," +
                              "processingParams={},processedColumns={},processedParams={})" +
                              "\n{}",
                            messageType,
                            messagesCount, hasDoneHandshake, size, isInQuery, processingColumns,
                            processingParams, processedColumns, processedParams,
                            BufferDumper.dumpAsHex(slice));
                }

                slice.readByte();

                if (this.hasDoneHandshake) {
                    this.handleCommonFlow(messageType, slice, out);
                } else {
                    MessageDecoder decoder;

                    switch (messageType) {
                        case ServerMessage.ERROR:
                            clear();
                            decoder = this.errorDecoder;
                            break;

                        default:
                            decoder = HandshakeV10Decoder.instance();
                    }

                    this.doDecoding(decoder, slice, out);
                }
            } else {
                buffer.resetReaderIndex();
            }
        }
    }

    void handleCommonFlow(int messageType, ByteBuf slice, List<Object> out) {
        MessageDecoder decoder;

        switch (messageType) {
            case ServerMessage.ERROR: {
                this.clear();
                decoder = errorDecoder;
                break;
            }

            case ServerMessage.EOF: {
                if (this.processingParams && this.totalParams > 0) {
                    this.processingParams = false;
                    if (this.totalColumns == 0) {
                        decoder = ParamAndColumnProcessingFinishedDecoder.instance();
                    } else {
                        decoder = ParamProcessingFinishedDecoder.instance();
                    }
                } else {
                    if (this.processingColumns) {
                        this.processingColumns = false;
                        decoder = ColumnProcessingFinishedDecoder.instance();
                    } else {
                        this.clear();
                        decoder = EOFMessageDecoder.instance();
                    }
                }
                break;
            }

            case ServerMessage.OK: {
                if (this.isPreparedStatementPrepare) {
                    decoder = this.preparedStatementPrepareDecoder;
                } else {
                    if (this.isPreparedStatementExecuteRows) {
                        decoder = null;
                    } else {
                        this.clear();
                        decoder = this.okDecoder;
                    }
                }
                break;
            }

            default: {
                if (this.isInQuery) {
                    decoder = null;
                } else {
                    throw new ParserNotAvailableException(messageType);
                }
                break;
            }
        }

        doDecoding(decoder, slice, out);
    }

    private void doDecoding(MessageDecoder decoder, ByteBuf slice, List<Object> out) {
        if (decoder == null) {
            slice.readerIndex(slice.readerIndex() - 1);
            Object result = decodeQueryResult(slice);

            if (slice.readableBytes() != 0) {
                throw new BufferNotFullyConsumedException(slice);
            }

            if (result != null) {
                out.add(result);
            }
        } else {
            ServerMessage result = decoder.decode(slice);

            if (result instanceof PreparedStatementPrepareResponse) {
                PreparedStatementPrepareResponse m = (PreparedStatementPrepareResponse) result;
                this.hasReadColumnsCount = true;
                this.totalColumns = m.columnsCount;
                this.totalParams = m.paramsCount;
            } else
            if (result instanceof ParamAndColumnProcessingFinishedMessage) {
                this.clear();
            } else
            if (result instanceof ColumnProcessingFinishedMessage && this.isPreparedStatementPrepare) {
                this.clear();
            } else
            if (result instanceof ColumnProcessingFinishedMessage && this.isPreparedStatementExecute) {
                this.isPreparedStatementExecuteRows = true;
            }

            if (slice.readableBytes() != 0) {
                throw new BufferNotFullyConsumedException(slice);
            }

            if (result != null) {
                if (result instanceof PreparedStatementPrepareResponse) {
                    PreparedStatementPrepareResponse m = (PreparedStatementPrepareResponse) result;
                    out.add(result);
                    if (m.columnsCount == 0 && m.paramsCount == 0) {
                        this.clear();
                        out.add(new ParamAndColumnProcessingFinishedMessage(new EOFMessage(0, 0)));
                    }
                } else {
                    out.add(result);
                }
            }
        }
    }

    private Object decodeQueryResult(ByteBuf slice) {
        if (!hasReadColumnsCount) {
            this.hasReadColumnsCount = true;
            this.totalColumns = readBinaryLength(slice);
            return null;
        }

        if (this.processingParams && this.totalParams != this.processedParams) {
            this.processedParams += 1;
            return this.columnDecoder.decode(slice);
        }

        if (this.totalColumns == this.processedColumns) {
            if (this.isPreparedStatementExecute) {
                ByteBuf row = slice.readBytes(slice.readableBytes());
                row.readByte(); // reads initial 00 at message
                return new BinaryRowMessage(row);
            } else {
                return this.rowDecoder.decode(slice);
            }
        } else {
            this.processedColumns += 1;
            return this.columnDecoder.decode(slice);
        }
    }

    void preparedStatementPrepareStarted() {
        this.queryProcessStarted();
        this.hasReadColumnsCount = true;
        this.processingParams = true;
        this.processingColumns = true;
        this.isPreparedStatementPrepare = true;
    }

    void preparedStatementExecuteStarted(int columnsCount, int paramsCount) {
        this.queryProcessStarted();
        this.hasReadColumnsCount = false;
        this.totalColumns = columnsCount;
        this.totalParams = paramsCount;
        this.isPreparedStatementExecute = true;
        this.processingParams = false;
    }

    void queryProcessStarted() {
        this.isInQuery = true;
        this.processingColumns = true;
        this.hasReadColumnsCount = false;
    }

    private void clear() {
        this.isPreparedStatementPrepare = false;
        this.isPreparedStatementExecute = false;
        this.isPreparedStatementExecuteRows = false;
        this.isInQuery = false;
        this.processingColumns = false;
        this.processingParams = false;
        this.totalColumns = 0;
        this.processedColumns = 0;
        this.totalParams = 0;
        this.processedParams = 0;
        this.hasReadColumnsCount = false;
    }
}
*/