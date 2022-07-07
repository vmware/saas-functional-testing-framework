/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.sql.Connection;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.DEFAULT_AWS_ACCESS_KEY;
import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.DEFAULT_AWS_REGION;
import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.DEFAULT_AWS_SECRET_ACCESS_KEY;
import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.S3_ENDPOINT_URL;
import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.QUERY_MAP_CLASS;

/**
 * Creates {@code ExecutionCommand} from the provided SQL argument.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class ExecutionCommandParser {

    /**
     *  Context for {@link ExecutionCommand}.
     *  Contains configuration and runtime references
     *  required by classes implementing {@link ExecutionCommand}.
     */
    @Builder
    @Getter
    static class ExecutionCommandContext {
        private final Properties info;
        private final Connection connection;

        public String getS3EndpointUrl() {
            return this.getInfo().getProperty(S3_ENDPOINT_URL);
        }

        public String getDefaultAwsAccessKey() {
            return this.getInfo().getProperty(DEFAULT_AWS_ACCESS_KEY);
        }

        public String getDefaultAwsSecretAccessKey() {
            return this.getInfo().getProperty(DEFAULT_AWS_SECRET_ACCESS_KEY);
        }

        public String getDefaultRegion() {
            return this.getInfo().getProperty(DEFAULT_AWS_REGION);
        }

        public String getQueryMapClass() {
            return this.getInfo().getProperty(QUERY_MAP_CLASS);
        }
    }

    final ExecutionCommand<?> executionCommand;

    static Optional<ExecutionCommand<?>> parse(final Object[] args, final ExecutionCommandContext context) {
        if (ArrayUtils.isNotEmpty(args) && (args[0] instanceof String)) {
            return parseInternal((String)args[0], context)
                    .map(ExecutionCommandParser::getExecutionCommand);
        }
        throw new IllegalArgumentException("First argument of the function should be a SQL string");
    }

    private static Optional<ExecutionCommandParser> parseUnload(final String sql, final ExecutionCommandContext context) {
        return UnloadCommand.parse(sql, context).map(ExecutionCommandParser::new);
    }

    private static Optional<ExecutionCommandParser> parseCopy(final String sql, final ExecutionCommandContext context) {
        return CopyCommand.parse(sql, context).map(ExecutionCommandParser::new);
    }

    private static Optional<ExecutionCommandParser> parseGenericCommand(final String sql, final ExecutionCommandContext context) {
        return GenericCommand.parse(sql, context).map(ExecutionCommandParser::new);
    }

    private static Optional<ExecutionCommandParser> parseInternal(final String sql, final ExecutionCommandContext context) {
        return parseUnload(sql, context).or(() -> parseCopy(sql, context)).or(() -> parseGenericCommand(sql, context));
    }
}
