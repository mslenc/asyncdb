package com.xs0.asyncdb.mysql.message.client;

public class QuitMessage implements ClientMessage {
    private static final QuitMessage instance = new QuitMessage();

    public static QuitMessage instance() {
        return instance;
    }

    @Override
    public int kind() {
        return Quit;
    }
}
