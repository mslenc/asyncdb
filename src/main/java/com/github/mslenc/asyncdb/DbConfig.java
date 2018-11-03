package com.github.mslenc.asyncdb;

import com.github.mslenc.asyncdb.impl.my.MyDbDataSource;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.util.NettyUtils;
import io.netty.channel.EventLoopGroup;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class DbConfig {
    public enum SslMode {
        DISABLE("disable"),
        PREFER("prefer"),
        REQUIRE("require"),
        VERIFY_CA("verify-ca"),
        VERIFY_FULL("verify-full");

        private final String asString;

        SslMode(String asString) {
            this.asString = asString;
        }
    }

    public enum DbType {
        MYSQL
    }

    public static final String DEFAULT_MYSQL_INIT_SQL = "set session time_zone='+00:00', sql_mode='STRICT_ALL_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,ANSI_QUOTES', autocommit=1";

    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_QUERY_TIMEOUT = null;
    public static final String DEFAULT_HOST = "localhost";

//    public static final int DEFAULT_POSTGRES_POST = 5432;
    public static final int DEFAULT_MYSQL_PORT = 3306;

    private final DbType dbType;
    private final String host;
    private final int port;
    private final String defaultUsername;
    private final String defaultPassword;
    private final String defaultDatabase;
    private final Duration connectTimeout;
    private final Duration queryTimeout;
    private final EventLoopGroup eventLoopGroup;
    private final int maxIdleConnections;
    private final int maxTotalConnections;
    private final List<String> initStatements;
    private final MyEncoders mySqlEncoders;
    private final SslMode sslMode;
    private final Path rootCertFile;

    private DbConfig(
        DbType dbType,
        String defaultUsername,
        String host,
        int port,
        String defaultPassword,
        String defaultDatabase,
        Duration connectTimeout,
        Duration queryTimeout,
        EventLoopGroup eventLoopGroup,
        int maxIdleConnections,
        int maxTotalConnections,
        List<String> initStatements,
        MyEncoders mySqlEncoders,
        SslMode sslMode,
        Path rootCertFile
    ) {
        this.dbType = dbType;
        this.defaultUsername = Objects.requireNonNull(defaultUsername, "username is required");
        this.host = host != null ? host : DEFAULT_HOST;
        this.port = port;
        this.defaultPassword = defaultPassword;
        this.defaultDatabase = defaultDatabase;
        this.connectTimeout = positiveOrDefault(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
        this.queryTimeout = positiveOrDefault(queryTimeout, DEFAULT_QUERY_TIMEOUT);
        this.eventLoopGroup = eventLoopGroup;
        this.maxIdleConnections = maxIdleConnections;
        this.maxTotalConnections = maxTotalConnections;
        this.initStatements = initStatements;
        this.mySqlEncoders = mySqlEncoders;
        this.sslMode = sslMode;
        this.rootCertFile = rootCertFile;
    }

    public static Builder newBuilder(DbType dbType) {
        switch (dbType) {
            case MYSQL:
                return new Builder(dbType, DEFAULT_MYSQL_PORT, DEFAULT_MYSQL_INIT_SQL);
        }

        throw new AssertionError("Unreachable");
    }

    public DbDataSource makeDataSource() {
        switch (dbType) {
            case MYSQL:
                return new MyDbDataSource(this);
        }

        throw new AssertionError("Unreachable");
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String defaultUsername() {
        return defaultUsername;
    }

    public String defaultPassword() {
        return defaultPassword;
    }

    public String defaultDatabase() {
        return defaultDatabase;
    }

    public Duration connectTimeout() {
        return connectTimeout;
    }

    public Duration queryTimeout() {
        return queryTimeout;
    }

    public EventLoopGroup eventLoopGroup() {
        return eventLoopGroup;
    }

    public int maxIdleConnections() {
        return maxIdleConnections;
    }

    public int maxTotalConnections() {
        return maxTotalConnections;
    }

    public List<String> initStatements() {
        return initStatements;
    }

    public MyEncoders mySqlEncoders() {
        return mySqlEncoders;
    }

    public SslMode sslMode() {
        return sslMode;
    }

    public Path rootCertFile() {
        return rootCertFile;
    }

    private static Duration positiveOrDefault(Duration provided, Duration defaultValue) {
        if (provided == null)
            return defaultValue;

        if (provided.isNegative() || provided.isZero())
            throw new IllegalArgumentException("Invalid duration, it must be > 0");

        return provided;
    }

    public static class Builder {
        private final DbType dbType;
        private String username;
        private String host = DEFAULT_HOST;
        private int port;
        private String password = null;
        private String database = null;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration queryTimeout = DEFAULT_QUERY_TIMEOUT;
        private EventLoopGroup eventLoopGroup;
        private int maxIdleConnections = 0;
        private int maxTotalConnections = Integer.MAX_VALUE;
        private List<String> initStatements = new ArrayList<>();
        private MyEncoders mySqlEncoders = MyEncoders.DEFAULT;
        private SslMode sslMode = SslMode.DISABLE;
        private Path rootCertFile;

        private Builder(DbType dbType, int port, String... initStatements) {
            this.dbType = dbType;
            this.port = port;
            this.initStatements.addAll(asList(initStatements));
        }

        public DbConfig build() {
            return new DbConfig(
                dbType,
                username,
                host,
                port,
                password,
                database,
                connectTimeout,
                queryTimeout,
                eventLoopGroup != null ? eventLoopGroup : NettyUtils.getDefaultEventLoopGroup(),
                maxIdleConnections,
                maxTotalConnections,
                initStatements,
                mySqlEncoders,
                sslMode,
                rootCertFile
            );
        }

        public Builder setDefaultCredentials(String username, String password) {
            setDefaultUsername(username);
            setDefaultPassword(password);
            return this;
        }

        public Builder setDefaultUsername(String username) {
            if (username == null)
                throw new IllegalArgumentException("username can't be null");

            this.username = username;
            return this;
        }

        public Builder setDefaultPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            if (port < 1 || port > 65535)
                throw new IllegalArgumentException("Invalid port number");

            this.port = port;
            return this;
        }

        public Builder setDefaultDatabase(String database) {
            this.database = database;
            return this;
        }

        public Builder setQueryTimeout(Duration queryTimeout) {
            if (queryTimeout == null || queryTimeout.isNegative() || queryTimeout.isZero()) {
                this.queryTimeout = null;
            } else {
                this.queryTimeout = queryTimeout;
            }
            return this;
        }

        public Builder setConnectTimeout(Duration connectTimeout) {
            if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()) {
                this.connectTimeout = null;
            } else {
                this.connectTimeout = connectTimeout;
            }
            return this;
        }

        public Builder setEventLoopGroup(EventLoopGroup eventLoopGroup) {
            this.eventLoopGroup = eventLoopGroup;
            return this;
        }

        public Builder addInitStatement(String statement) {
            this.initStatements.add(requireNonNull(statement));
            return this;
        }

        public Builder setInitStatements(List<String> initStatements) {
            this.initStatements.clear();
            if (initStatements != null) {
                for (String statement : initStatements) {
                    this.initStatements.add(requireNonNull(statement));
                }
            }
            return this;
        }

        public Builder setInitStatements(String... initStatements) {
            this.initStatements.clear();
            for (String statement : initStatements) {
                this.initStatements.add(requireNonNull(statement));
            }
            return this;
        }

        public Builder setMySqlEncoders(MyEncoders mySqlEncoders) {
            this.mySqlEncoders = requireNonNull(mySqlEncoders);
            return this;
        }

        public Builder setSslMode(SslMode sslMode) {
            this.sslMode = requireNonNull(sslMode);
            return this;
        }

        public Builder setRootCertFile(Path rootCertFile) {
            this.rootCertFile = rootCertFile;
            return this;
        }
    }
}
