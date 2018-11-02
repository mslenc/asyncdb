package com.github.mslenc.asyncdb.conf;

import com.github.mslenc.asyncdb.util.NettyUtils;
import io.netty.channel.EventLoopGroup;

import java.time.Duration;
import java.util.Objects;

public class Configuration {
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_TEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_QUERY_TIMEOUT = null;
    public static final SSLConfiguration DEFAULT_SSL_CONFIGURATION = new SSLConfiguration(SSLConfiguration.Mode.DISABLE);
    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_POSTGRES_POST = 5432;
    public static final int DEFAULT_MYSQL_PORT = 3306;

    public final String host;
    public final int port;
    public final String defaultUsername;
    public final String defaultPassword;
    public final String defaultDatabase;
    public final SSLConfiguration sslConfiguration;
    public final Duration connectTimeout;
    public final Duration testTimeout;
    public final Duration queryTimeout;
    public final EventLoopGroup eventLoopGroup;

    public Configuration(
        String defaultUsername,
        String host,
        int port,
        String defaultPassword,
        String defaultDatabase,
        SSLConfiguration sslConfiguration,
        Duration connectTimeout,
        Duration testTimeout,
        Duration queryTimeout,
        EventLoopGroup eventLoopGroup
    ) {
        this.defaultUsername = Objects.requireNonNull(defaultUsername, "username is required");
        this.host = host != null ? host : DEFAULT_HOST;
        this.port = port;
        this.defaultPassword = defaultPassword;
        this.defaultDatabase = defaultDatabase;
        this.sslConfiguration = sslConfiguration != null ? sslConfiguration : DEFAULT_SSL_CONFIGURATION;
        this.connectTimeout = positiveOrDefault(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
        this.testTimeout = positiveOrDefault(testTimeout, DEFAULT_TEST_TIMEOUT);
        this.queryTimeout = positiveOrDefault(queryTimeout, DEFAULT_QUERY_TIMEOUT);
        this.eventLoopGroup = eventLoopGroup;
    }

    public static Builder newMySQLBuilder() {
        return new Builder(DEFAULT_MYSQL_PORT);
    }

    public static Builder newPostgresBuilder() {
        return new Builder(DEFAULT_POSTGRES_POST);
    }

    private static Duration positiveOrDefault(Duration provided, Duration defaultValue) {
        if (provided == null)
            return defaultValue;

        if (provided.isNegative() || provided.isZero())
            throw new IllegalArgumentException("Invalid duration, it must be > 0");

        return provided;
    }

    public static class Builder {
        private String username;
        private String host = DEFAULT_HOST;
        private int port;
        private String password = null;
        private String database = null;
        private SSLConfiguration sslConfiguration = DEFAULT_SSL_CONFIGURATION;
        private Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private Duration testTimeout = DEFAULT_TEST_TIMEOUT;
        private Duration queryTimeout = DEFAULT_QUERY_TIMEOUT;
        private EventLoopGroup eventLoopGroup;

        private Builder(int defaultPort) {
            this.port = defaultPort;
        }

        public Configuration build() {
            return new Configuration(
                username,
                host,
                port,
                password,
                database,
                sslConfiguration,
                connectTimeout,
                testTimeout,
                queryTimeout,
                eventLoopGroup != null ? eventLoopGroup : NettyUtils.getDefaultEventLoopGroup()
            );
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

        public Builder setSSLConfiguration(SSLConfiguration sslConfiguration) {
            if (sslConfiguration != null) {
                this.sslConfiguration = sslConfiguration;
            } else {
                this.sslConfiguration = DEFAULT_SSL_CONFIGURATION;
            }
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

        public Builder setTestTimeout(Duration testTimeout) {
            if (testTimeout == null || testTimeout.isNegative() || testTimeout.isZero()) {
                this.testTimeout = null;
            } else {
                this.testTimeout = testTimeout;
            }
            return this;
        }

        public Builder setEventLoopGroup(EventLoopGroup eventLoopGroup) {
            this.eventLoopGroup = eventLoopGroup;
            return this;
        }
    }
}
