package com.xs0.asyncdb.mysql.codec.statemachine;

import com.xs0.asyncdb.common.exceptions.DatabaseException;
import com.xs0.asyncdb.mysql.codec.MySQLConnectionHandler;
import io.netty.buffer.ByteBuf;

public interface MySQLStateMachine {
    enum ResultType {
        STATE_MACHINE_FINISHED,
        EXPECTING_MORE_PACKETS,
        PROTOCOL_ERROR_ABORT_ABORT_ABORT
    }

    class Result {
        public final ResultType resultType;
        public final Throwable error;

        private Result(ResultType resultType, Throwable error) {
            this.resultType = resultType;
            this.error = error;
        }

        private static Result STATE_MACHINE_FINISHED = new Result(ResultType.STATE_MACHINE_FINISHED, null);
        private static Result EXPECTING_MORE_PACKETS = new Result(ResultType.EXPECTING_MORE_PACKETS, null);

        public static Result stateMachineFinished() {
            return STATE_MACHINE_FINISHED;
        }

        public static Result expectingMorePackets() {
            return EXPECTING_MORE_PACKETS;
        }

        public static Result protocolErrorAbortEverything(Throwable error) {
            if (error == null)
                throw new IllegalArgumentException("An error must be provided");

            return new Result(ResultType.PROTOCOL_ERROR_ABORT_ABORT_ABORT, error);
        }

        public static Result protocolErrorAbortEverything(String message) {
            if (message == null || message.isEmpty())
                throw new IllegalArgumentException("An error must be provided");

            return new Result(ResultType.PROTOCOL_ERROR_ABORT_ABORT_ABORT, new DatabaseException(message));
        }

        public static Result unknownHeaderByte(int headerByte, String stateName) {
            return protocolErrorAbortEverything("Unexpected packet type (" + headerByte + ") received during " + stateName);
        }
    }

    Result init(MySQLConnectionHandler conn);
    Result processPacket(ByteBuf packet);
}
