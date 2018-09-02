package com.xs0.asyncdb.mysql.codec.statemachine;

import com.xs0.asyncdb.common.PreparedStatement;
import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.mysql.binary.BinaryRowEncoder;
import com.xs0.asyncdb.mysql.codec.DecoderRegistry;
import com.xs0.asyncdb.mysql.codec.PreparedStatementInfo;
import com.xs0.asyncdb.mysql.message.client.ClientMessage;
import com.xs0.asyncdb.mysql.message.server.ErrorMessage;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

public interface MySQLStateMachine {
    enum ResultType {
        STATE_MACHINE_FINISHED,
        EXPECTING_MORE_PACKETS,
        PROTOCOL_ERROR_ABORT_ABORT_ABORT,
        DISCONNECT
    }

    interface Support {
        void sendMessage(ClientMessage message);

        ErrorMessage decodeErrorAfterHeader(ByteBuf packet);

        DecoderRegistry decoderRegistry();

        BinaryRowEncoder getBinaryEncoders();

        PreparedStatement createPreparedStatement(String query, PreparedStatementInfo info);
    }

    class Result {
        public final ResultType resultType;
        public final Throwable error;
        public final CompletableFuture<?> promise;

        private Result(ResultType resultType, Throwable error, CompletableFuture<?> promise) {
            this.resultType = resultType;
            this.error = error;
            this.promise = promise;
        }

        private static Result STATE_MACHINE_FINISHED = new Result(ResultType.STATE_MACHINE_FINISHED, null, null);
        private static Result EXPECTING_MORE_PACKETS = new Result(ResultType.EXPECTING_MORE_PACKETS, null, null);

        public static Result stateMachineFinished() {
            return STATE_MACHINE_FINISHED;
        }

        public static Result expectingMorePackets() {
            return EXPECTING_MORE_PACKETS;
        }

        public static Result disconnect(CompletableFuture<Void> promise) {
            return new Result(ResultType.DISCONNECT, null, promise);
        }

        public static Result protocolErrorAbortEverything(Throwable error) {
            if (error == null)
                throw new IllegalArgumentException("An error must be provided");

            return new Result(ResultType.PROTOCOL_ERROR_ABORT_ABORT_ABORT, error, null);
        }

        public static Result protocolErrorAbortEverything(String message) {
            if (message == null || message.isEmpty())
                throw new IllegalArgumentException("An error must be provided");

            return new Result(ResultType.PROTOCOL_ERROR_ABORT_ABORT_ABORT, new DatabaseException(message), null);
        }

        public static Result unknownHeaderByte(int headerByte, String stateName) {
            return protocolErrorAbortEverything("Unexpected packet type (" + headerByte + ") received during " + stateName);
        }
    }

    Result start(Support support);
    Result processPacket(ByteBuf packet, Support support);
}
