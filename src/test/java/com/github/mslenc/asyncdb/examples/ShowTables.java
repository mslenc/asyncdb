package com.github.mslenc.asyncdb.examples;

import com.github.mslenc.asyncdb.DbConfig;
import com.github.mslenc.asyncdb.DbDataSource;
import com.github.mslenc.asyncdb.DbRow;

import static com.github.mslenc.asyncdb.DbConfig.DbType.MYSQL;

public class ShowTables {
    public static void main(String[] args) {
        DbConfig config =
            DbConfig.newBuilder(MYSQL).
                setHost("127.0.0.1", 3356).
                setDefaultCredentials("asyncdb", "asyncdb").
                setDefaultDatabase("asyncdb").
            build();

        DbDataSource dataSource = config.makeDataSource();

        dataSource.connect().whenComplete((conn, error) -> {
            if (error != null) {
                error.printStackTrace();
                config.eventLoopGroup().shutdownGracefully();
                return;
            }

            conn.executeQuery("SELECT * FROM information_schema.tables").whenComplete((result, queryError) -> {
                if (queryError == null) {
                    System.out.println("Here are the tables present:");
                    for (DbRow row : result) {
                        String schema = row.getString("TABLE_SCHEMA");
                        String table = row.getString("TABLE_NAME");
                        int rows = row.getInt("TABLE_ROWS");
                        int avgLen = row.getInt("AVG_ROW_LENGTH");

                        System.out.println("- " + schema + "." + table + " (" + rows + " rows, average length " + avgLen + ")");
                    }
                } else {
                    queryError.printStackTrace();
                }

                conn.close().thenRun(() -> config.eventLoopGroup().shutdownGracefully());
            });
        });
    }
}
