/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements Generic commands.
 * Class used for parsing provided SQL queries before executing the SQL command.
 */
@Slf4j
@Data
@AllArgsConstructor
public class GenericCommand implements ExecutionCommand<Boolean> {

    private static final FindAndReplace VARCHAR_MAX_DATA_TYPE_PATTERN = FindAndReplace.builder()
            .findPattern(Pattern.compile("VARCHAR\\(MAX\\)", Pattern.CASE_INSENSITIVE))
            .replace("VARCHAR\\(256\\)")
            .build();
    private static final FindAndReplace DISTSTYLE_PATTERN = FindAndReplace.builder()
            .findPattern(Pattern.compile("diststyle\\s+(key|auto|even|all)", Pattern.CASE_INSENSITIVE))
            .replace("")
            .build();
    private static final FindAndReplace DISTKEY_PATTERN = FindAndReplace.builder()
            .findPattern(Pattern.compile("distkey\\s+(\\(.+?\\))", Pattern.CASE_INSENSITIVE))
            .replace("")
            .build();

    private static final List<FindAndReplace> FIND_AND_REPLACE_CHAIN = List.of(VARCHAR_MAX_DATA_TYPE_PATTERN, DISTSTYLE_PATTERN, DISTKEY_PATTERN);

    private final String query;

    @SneakyThrows
    static Optional<GenericCommand> parse(final String sql, final ExecutionCommandParser.ExecutionCommandContext context) {
        return parseQueryMap(sql, context).or(() -> findAndReplace(sql));
    }

    private static Optional<GenericCommand> findAndReplace(final String sql) {
        final String parsedQuery = FIND_AND_REPLACE_CHAIN.stream()
                .collect(FindAndReplace.replacingInSqlStatement(sql));
        if (parsedQuery.equals(sql)) {
            return Optional.empty();
        }
        return Optional.of(new GenericCommand(parsedQuery));
    }

    @SneakyThrows
    private static Optional<GenericCommand> parseQueryMap(final String sql, final ExecutionCommandParser.ExecutionCommandContext context) {
        final String queryMapClass = context.getQueryMapClass();
        if (queryMapClass == null) {
            return Optional.empty();
        }
        final Object queryMapObj = Class.forName(queryMapClass).getConstructor().newInstance();
        if (!(queryMapObj instanceof Map)) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") final Map<String, String> queryMap = (Map<String, String>)queryMapObj;
        final String key = sql.trim();
        if (!queryMap.containsKey(key)) {
            return Optional.empty();
        }
        log.info("query map : " + queryMap.get(key));
        return Optional.of(new GenericCommand(queryMap.get(key)));
    }

    @SneakyThrows
    @Override
    public PreparedStatement prepareStatement(final Connection connection) {
        return connection.prepareStatement(this.query);
    }

    @SneakyThrows
    @Override
    public Boolean execute(final PreparedStatement preparedStatement) {
        return preparedStatement.execute();
    }

    @SneakyThrows
    @Override
    public Boolean execute(final Statement statement) {
        return statement.execute(this.query);
    }

    @SneakyThrows
    @Override
    public ResultSet executeQuery(final Statement statement) {
        return statement.executeQuery(this.query);
    }

    @SneakyThrows
    @Override
    public ResultSet executeQuery(final PreparedStatement preparedStatement) {
        return preparedStatement.executeQuery();
    }

    /**
     * Definition used to replace tokens in the sql query.
     */
    @Builder
    static class FindAndReplace {

        private final Pattern findPattern;
        private final String replace;

        /**
         * {@code Collector} that uses FindAndReplace input elements to replace the matches in the specified sql string.
         * It accumulates the changes so that if we have two FindAndReplace items for example
         * {pattern: "foo", replace: "FOO"} and {pattern: "bar", replace: "BAR"} and a sql string "SELECT foo, bar"
         * the result of the reduction is "SELECT FOO, BAR".
         *
         * @param sql sql statement to be reduced
         * @return the result of the reduction
         */
        static Collector<FindAndReplace, StringBuilder, String> replacingInSqlStatement(final String sql) {
            return Collector.of(
                    () -> new StringBuilder(sql),
                    FindAndReplace::findAndReplaceInSqlString,
                    FindAndReplace::ensureSerial,
                    FindAndReplace::finish);
        }

        private static StringBuilder ensureSerial(final StringBuilder state1, final StringBuilder state2) {
            if (state1.toString().equals(state2.toString())) {
                return state1;
            }
            throw new RuntimeException("Concurrent findAndReplaceInSqlString is not supported.");
        }

        private static String finish(final StringBuilder finalState) {
            return finalState.toString();
        }

        private static void findAndReplaceInSqlString(final StringBuilder sql, final FindAndReplace fr) {
            final Matcher matcher = fr.findPattern.matcher(sql);
            if (matcher.find()) {
                final String parsedQuery = matcher.replaceAll(fr.replace);
                log.info("Sql query [{}] is modified. New command to be executed is [{}] ", sql, parsedQuery);
                sql.setLength(0);
                sql.append(parsedQuery);
            }
        }
    }
}
