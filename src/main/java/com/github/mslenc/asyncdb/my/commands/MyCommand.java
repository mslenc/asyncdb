package com.github.mslenc.asyncdb.my.commands;

import com.github.mslenc.asyncdb.ex.DatabaseException;
import com.github.mslenc.asyncdb.my.MyConnection;
import com.github.mslenc.asyncdb.my.msgclient.ClientMessage;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

public abstract class MyCommand {
    protected final MyConnection conn;

    protected MyCommand(MyConnection conn) {
        this.conn = conn;
    }

    public abstract CompletableFuture<?> getPromise();
    public abstract Result start();
    public abstract Result processPacket(ByteBuf packet);

    public static class Result {
        public enum Type {
            STATE_MACHINE_FINISHED,
            EXPECTING_MORE_PACKETS,
            PROTOCOL_ERROR_ABORT_ABORT_ABORT,
            DISCONNECT,
            SWITCH_TO_SSL,
        }

        public final Type resultType;
        public final Throwable error;
        public final CompletableFuture<?> promise;
        public final ClientMessage message;

        private Result(Type resultType, Throwable error, CompletableFuture<?> promise, ClientMessage message) {
            this.resultType = resultType;
            this.error = error;
            this.promise = promise;
            this.message = message;
        }

        private static Result STATE_MACHINE_FINISHED = new Result(Type.STATE_MACHINE_FINISHED, null, null, null);
        private static Result EXPECTING_MORE_PACKETS = new Result(Type.EXPECTING_MORE_PACKETS, null, null, null);

        public static Result stateMachineFinished() {
            return STATE_MACHINE_FINISHED;
        }

        public static Result expectingMorePackets() {
            return EXPECTING_MORE_PACKETS;
        }

        public static Result switchToSsl(ClientMessage message, CompletableFuture<Void> promise) {
            return new Result(Type.SWITCH_TO_SSL, null, promise, message);
        }

        public static Result disconnect(CompletableFuture<Void> promise) {
            return new Result(Type.DISCONNECT, null, promise, null);
        }

        public static Result protocolErrorAbortEverything(Throwable error) {
            if (error == null)
                throw new IllegalArgumentException("An error must be provided");

            return new Result(Type.PROTOCOL_ERROR_ABORT_ABORT_ABORT, error, null, null);
        }

        public static Result protocolErrorAbortEverything(String message) {
            if (message == null || message.isEmpty())
                throw new IllegalArgumentException("An error must be provided");

            return new Result(Type.PROTOCOL_ERROR_ABORT_ABORT_ABORT, new DatabaseException(message), null, null);
        }

        public static Result unknownHeaderByte(int headerByte, String stateName) {
            return protocolErrorAbortEverything("Unexpected packet type (" + headerByte + ") received during " + stateName);
        }
    }
}
