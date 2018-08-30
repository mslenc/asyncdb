package com.xs0.asyncdb.mysql.codec;

import java.util.List;

public class PreparedStatement {
    public final String statement;
    public final List<Object> values;

    public PreparedStatement(String statement, List<Object> values) {
        this.statement = statement;
        this.values = values;
    }
}
