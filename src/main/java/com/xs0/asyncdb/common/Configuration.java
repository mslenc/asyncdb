package com.xs0.asyncdb.common;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class Configuration {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_TEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_QUERY_TIMEOUT = null;
    public static final ByteBufAllocator DEFAULT_ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    public static final int DEFAULT_MAXIMUM_MESSAGE_SIZE = 16777216;
    public static final SSLConfiguration DEFAULT_SSL_CONFIGURATION = new SSLConfiguration(SSLConfiguration.Mode.DISABLE);
    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_POSTGRES_POST = 5432;
    public static final int DEFAULT_MYSQL_PORT = 3306;

    public final String username;
    public final String host;
    public final int port;
    public final String password;
    public final String database;
    public final SSLConfiguration sslConfiguration;
    public final Charset charset;
    public final int maximumMessageSize;
    public final ByteBufAllocator allocator;
    public final Duration connectTimeout;
    public final Duration testTimeout;
    public final Duration queryTimeout;

    public Configuration(
        String username,
        String host,
        int port,
        String password,
        String database,
        SSLConfiguration sslConfiguration,
        Charset charset,
        Integer maximumMessageSize,
        ByteBufAllocator allocator,
        Duration connectTimeout,
        Duration testTimeout,
        Duration queryTimeout
    ) {
        this.username = Objects.requireNonNull(username, "username is required");
        this.host = host != null ? host : DEFAULT_HOST;
        this.port = port;
        this.password = password;
        this.database = database;
        this.sslConfiguration = sslConfiguration != null ? sslConfiguration : DEFAULT_SSL_CONFIGURATION;
        this.charset = charset != null ? charset : DEFAULT_CHARSET;
        this.maximumMessageSize = maximumMessageSize != null ? maximumMessageSize : DEFAULT_MAXIMUM_MESSAGE_SIZE;
        this.allocator = allocator != null ? allocator : DEFAULT_ALLOCATOR;
        this.connectTimeout = positiveOrDefault(connectTimeout, DEFAULT_CONNECT_TIMEOUT);
        this.testTimeout = positiveOrDefault(testTimeout, DEFAULT_TEST_TIMEOUT);
        this.queryTimeout = positiveOrDefault(queryTimeout, DEFAULT_QUERY_TIMEOUT);
    }

    private static Duration positiveOrDefault(Duration provided, Duration defaultValue) {
        if (provided == null)
            return defaultValue;

        if (provided.isNegative() || provided.isZero())
            throw new IllegalArgumentException("Invalid duration, it must be > 0");

        return provided;
    }
}
