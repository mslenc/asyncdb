package com.xs0.asyncdb.mysql.message.client;

public class QueryMessage implements ClientMessage {
    public final String query;

    public QueryMessage(String query) {
        this.query = query;
    }

    @Override
    public int kind() {
        return QUERY;
    }
}
