package com.github.mslenc.asyncdb;

import com.github.mslenc.asyncdb.impl.my.MyDbDataSource;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;
import com.github.mslenc.asyncdb.util.NettyUtils;
import io.netty.channel.EventLoopGroup;

import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

import static java.util.Collections.unmodifiableList;
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

    public static final String DEFAULT_MYSQL_INIT_SQL = "SET SESSION time_zone='+00:00', SESSION sql_mode='STRICT_ALL_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,ANSI_QUOTES', autocommit=1";

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
    private final DbTxIsolation defaultTxIsolation;
    private final DbTxMode defaultTxMode;
    private final boolean hasDefaultInitStatement;

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
        Path rootCertFile,
        DbTxIsolation defaultTxIsolation,
        DbTxMode defaultTxMode
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
        this.mySqlEncoders = mySqlEncoders;
        this.sslMode = sslMode;
        this.rootCertFile = rootCertFile;
        this.defaultTxIsolation = defaultTxIsolation == DbTxIsolation.DEFAULT ? DbTxIsolation.REPEATABLE_READ : defaultTxIsolation;
        this.defaultTxMode = defaultTxMode == DbTxMode.DEFAULT ? DbTxMode.READ_WRITE : defaultTxMode;
        this.initStatements = makeInitStatements(initStatements, dbType, this.defaultTxIsolation, this.defaultTxMode);
        this.hasDefaultInitStatement = initStatements.stream().anyMatch(Objects::isNull);
    }

    static List<String> makeInitStatements(List<String> initStatements, DbType dbType, DbTxIsolation txIsolation, DbTxMode txMode) {
        ArrayList<String> result = new ArrayList<>();

        for (String stmt : initStatements) {
            if (stmt != null) {
                result.add(stmt);
            } else {
                makeDefaultInitStatements(dbType, txIsolation, result);
            }
        }

        return unmodifiableList(result);
    }

    static void makeDefaultInitStatements(DbType dbType, DbTxIsolation txIsolation, List<String> out) {
        out.add(DEFAULT_MYSQL_INIT_SQL);
        out.add("SET SESSION TRANSACTION ISOLATION LEVEL " + txIsolation);
    }

    public static Builder newBuilder(DbType dbType) {
        return new Builder(requireNonNull(dbType));
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

    public DbTxIsolation defaultTxIsolation() {
        return defaultTxIsolation;
    }

    public DbTxMode defaultTxMode() {
        return defaultTxMode;
    }

    private static Duration positiveOrDefault(Duration provided, Duration defaultValue) {
        if (provided == null)
            return defaultValue;

        if (provided.isNegative() || provided.isZero())
            throw new IllegalArgumentException("Invalid duration, it must be > 0");

        return provided;
    }

    public Builder toBuilder() {
        Builder builder = new Builder(dbType);

        builder.setInitStatements(this.initStatements);
        if (hasDefaultInitStatement) {
            builder.initStatements.set(0, null); // to generate them again based on new settings
            builder.initStatements.remove(1); // we currently generate two default statements, so removing the other one
        }

        builder.defaultUsername = this.defaultUsername;
        builder.defaultPassword = this.defaultPassword;
        builder.defaultDatabase = this.defaultDatabase;
        builder.host = this.host;
        builder.port = this.port;
        builder.connectTimeout = this.connectTimeout;
        builder.queryTimeout = this.queryTimeout;
        builder.eventLoopGroup = this.eventLoopGroup;
        builder.maxIdleConnections = this.maxIdleConnections;
        builder.maxTotalConnections = this.maxTotalConnections;
        builder.mySqlEncoders = this.mySqlEncoders;
        builder.sslMode = this.sslMode;
        builder.rootCertFile = this.rootCertFile;
        builder.defaultTxIsolation = this.defaultTxIsolation;
        builder.defaultTxMode = this.defaultTxMode;

        return builder;
    }

    public static class Builder {
        private final DbType dbType;
        private String defaultUsername = "asyncdb";
        private String defaultPassword = null;
        private String defaultDatabase = null;
        private String host = DEFAULT_HOST;
        private int port;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration queryTimeout = DEFAULT_QUERY_TIMEOUT;
        private EventLoopGroup eventLoopGroup;
        private int maxIdleConnections = 0;
        private int maxTotalConnections = Integer.MAX_VALUE;
        private List<String> initStatements = new ArrayList<>();
        private MyEncoders mySqlEncoders = MyEncoders.DEFAULT;
        private SslMode sslMode = SslMode.DISABLE;
        private Path rootCertFile;
        private DbTxIsolation defaultTxIsolation = DbTxIsolation.REPEATABLE_READ;
        private DbTxMode defaultTxMode = DbTxMode.READ_WRITE;

        private Builder(DbType dbType) {
            this.dbType = dbType;
            this.port = DEFAULT_MYSQL_PORT;
            this.initStatements.add(null); // null means the default init statement (but we generate it based
                                           // on some other properties, so can't do it yet)
        }

        public DbConfig build() {
            return new DbConfig(
                dbType,
                defaultUsername,
                host,
                port,
                defaultPassword,
                defaultDatabase,
                connectTimeout,
                queryTimeout,
                eventLoopGroup != null ? eventLoopGroup : NettyUtils.getDefaultEventLoopGroup(),
                maxIdleConnections,
                maxTotalConnections,
                initStatements,
                mySqlEncoders,
                sslMode,
                rootCertFile,
                defaultTxIsolation,
                defaultTxMode
            );
        }

        public Builder setDefaultCredentials(String username, String password) {
            setDefaultUsername(username);
            setDefaultPassword(password);
            return this;
        }

        public Builder setDefaultUsername(String defaultUsername) {
            if (defaultUsername == null)
                throw new IllegalArgumentException("username can't be null");

            this.defaultUsername = defaultUsername;
            return this;
        }

        public Builder setDefaultPassword(String defaultPassword) {
            this.defaultPassword = defaultPassword;
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
            this.defaultDatabase = database;
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
            return setInitStatements(Arrays.asList(initStatements));
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

        public Builder setMaxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = maxIdleConnections;
            return this;
        }

        public Builder setMaxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
            return this;
        }

        public Builder setHost(String host, int port) {
            setHost(host);
            setPort(port);
            return this;
        }

        public Builder setDefaultTxIsolation(DbTxIsolation isolation) {
            this.defaultTxIsolation = isolation;
            return this;
        }

        public Builder setDefaultTxMode(DbTxMode mode) {
            this.defaultTxMode = mode;
            return this;
        }
    }
}
