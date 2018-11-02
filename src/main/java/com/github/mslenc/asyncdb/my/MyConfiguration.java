package com.github.mslenc.asyncdb.my;

import com.github.mslenc.asyncdb.conf.Configuration;
import com.github.mslenc.asyncdb.my.encoders.MyEncoders;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class MyConfiguration {
    public static final String DEFAULT_INIT_SQL = "set session time_zone='+00:00', sql_mode='STRICT_ALL_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO,ANSI_QUOTES', autocommit=1";

    private final Configuration generalConfig;
    private final MyEncoders encoders;
    private final int maxIdleConnections;
    private final int maxTotalConnections;
    private final List<String> initStatements;

    private MyConfiguration(Configuration generalConfig, MyEncoders encoders,
                            int maxIdleConnections, int maxTotalConnections,
                            List<String> initStatements) {
        this.generalConfig = requireNonNull(generalConfig);
        this.encoders = encoders;
        this.maxIdleConnections = maxIdleConnections;
        this.maxTotalConnections = maxTotalConnections;
        this.initStatements = initStatements;
    }

    public Configuration generalConfig() {
        return generalConfig;
    }

    public MyEncoders encoders() {
        return encoders;
    }

    public List<String> initStatements() {
        return initStatements;
    }

    public int maxIdleConnections() {
        return maxIdleConnections;
    }

    public int maxTotalConnections() {
        return maxTotalConnections;
    }

    public static Builder newBuilder(Configuration generalConfig) {
        return new Builder(generalConfig);
    }

    public static class Builder {
        private Configuration generalConfig;
        private MyEncoders encoders = MyEncoders.DEFAULT;
        private int maxIdleConnections = 0;
        private int maxTotalConnections = Integer.MAX_VALUE;

        private ArrayList<String> initStatements = new ArrayList<>(); {
            initStatements.add(DEFAULT_INIT_SQL);
        }

        private Builder(Configuration generalConfig) {
            this.generalConfig = requireNonNull(generalConfig);
        }

        public Builder setEncoders(MyEncoders encoders) {
            this.encoders = requireNonNull(encoders);
            return this;
        }

        public Builder setMaxIdleConnections(int maxIdleConnections) {
            this.maxIdleConnections = Math.max(maxIdleConnections, 0);
            return this;
        }

        public Builder setMaxTotalConnections(int maxTotalConnections) {
            if (maxTotalConnections < 1)
                throw new IllegalArgumentException("maxTotalConnections must be positive (but was " + maxTotalConnections + ")");

            this.maxTotalConnections = maxTotalConnections;
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

        public MyConfiguration build() {
            return new MyConfiguration(generalConfig, encoders, maxIdleConnections, maxTotalConnections, initStatements);
        }
    }
}
