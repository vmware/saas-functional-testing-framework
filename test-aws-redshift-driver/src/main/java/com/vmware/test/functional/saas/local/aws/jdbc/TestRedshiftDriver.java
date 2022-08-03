/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
// CHECKSTYLE:OFF
import java.util.logging.Logger;
// CHECKSTYLE:ON

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Extends PostgreSQL SQL driver with a client-side emulation of the {@code UNLOAD} and {@code COPY} SQL commands.
 */
@Slf4j
public class TestRedshiftDriver implements Driver {

    public static final String S3_ENDPOINT_URL = "s3EndpointUrl";
    public static final String DEFAULT_AWS_ACCESS_KEY = "defaultAwsAccessKey";
    public static final String DEFAULT_AWS_SECRET_ACCESS_KEY = "defaultAwsSecretAccessKey";
    public static final String DEFAULT_AWS_REGION = "defaultAwsRegion";
    public static final String SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS = "skipErrorsOnMissingConfigOptions";
    public static final String JDBC_TESTREDSHIFT_PREFIX = "jdbc:testredshift:";
    public static final String QUERY_MAP_CLASS = "queryMapClass";

    private static TestRedshiftDriver registeredDriver;
    private static final Driver POSTGRES_DRIVER_WRAPPED = new org.postgresql.Driver();

    static {
        try {
            register();
            log.info("{} was successfully registered.", TestRedshiftDriver.class.getName());
        } catch (final SQLException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void register() throws SQLException {
        if (isRegistered()) {
            throw new IllegalStateException(TestRedshiftDriver.class.getName() + " is already registered.");
        }

        final TestRedshiftDriver newRegisteredDriver = new TestRedshiftDriver();
        DriverManager.registerDriver(newRegisteredDriver);
        TestRedshiftDriver.registeredDriver = newRegisteredDriver;
    }

    public static boolean isRegistered() {
        return registeredDriver != null;
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        final Connection connection = POSTGRES_DRIVER_WRAPPED.connect(url.replace(JDBC_TESTREDSHIFT_PREFIX, "jdbc:postgresql:"), info);

        return (Connection)Proxy.newProxyInstance(
                TestRedshiftDriver.class.getClassLoader(),
                new Class<?>[] { Connection.class },
                new ConnectionProxy(connection, info));
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url.startsWith(JDBC_TESTREDSHIFT_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(getClass().getName()).getParent();
    }

    /**
     * Intercepts createStatement calls and processes {@code UNLOAD} and {@code COPY} commands.
     * Intercepts prepareStatement calls and creates {@code UNLOAD} and {@code COPY} commands from the SQL argument.
     */
    @AllArgsConstructor
    static class ConnectionProxy implements InvocationHandler {

        private final Connection connection;
        private final Properties info;

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws InvocationTargetException, IllegalAccessException, SQLException {
            switch (method.getName()) {
            case "createStatement":
                return proxyStatement(method, args);
            case "prepareStatement":
                final Optional<ExecutionCommand<?>> executionCommand = ExecutionCommandParser.parse(args,
                        ExecutionCommandParser.ExecutionCommandContext.builder()
                                .connection(this.connection)
                                .info(this.info)
                                .build());
                /*
                 * Proxy the prepared statements that have a matching SQL argument, else use the original instance.
                 */
                return executionCommand
                        .map(this::proxyPreparedStatement)
                        .orElse(proxyPreparedStatement(
                                (PreparedStatement)invokeMethod(method, this.connection, args)));
            default:
            }
            return invokeMethod(method, this.connection, args);
        }

        public Object proxyPreparedStatement(final PreparedStatement preparedStatement) {
            return Proxy.newProxyInstance(
                    TestRedshiftDriver.class.getClassLoader(),
                    new Class<?>[] { PreparedStatement.class },
                    TestRedshiftDriver.PreparedStatementProxy.builder()
                            .info(this.info)
                            .statement(preparedStatement)
                            .build());
        }

        public Object proxyPreparedStatement(final ExecutionCommand<?> executionCommand) {
            return Proxy.newProxyInstance(
                    TestRedshiftDriver.class.getClassLoader(),
                    new Class<?>[] { PreparedStatement.class },
                    TestRedshiftDriver.PreparedStatementProxy.builder()
                            .executionCommand(executionCommand)
                            .info(this.info)
                            .statement(executionCommand.prepareStatement(this.connection))
                            .build());
        }

        private Statement proxyStatement(final Method method, final Object[] args)
                throws InvocationTargetException, IllegalAccessException, SQLException {
            final StatementProxy statementProxy = StatementProxy.builder()
                    .connection(this.connection)
                    .statement((Statement)invokeMethod(method, this.connection, args))
                    .info(this.info)
                    .build();
            final Statement statement = (Statement)Proxy.newProxyInstance(
                    TestRedshiftDriver.class.getClassLoader(),
                    new Class<?>[] { Statement.class },
                    statementProxy);
            statementProxy.setProxyStatement(statement);
            return statement;
        }

        private static Object invokeMethod(final Method method, final Object obj, final Object... args)
                throws SQLException, IllegalAccessException, InvocationTargetException {
            try {
                return method.invoke(obj, args);
            } catch (final InvocationTargetException e) {
                if (e.getTargetException() instanceof SQLException) {
                    throw (SQLException)e.getTargetException();
                }
                throw e;
            }
        }
    }

    /**
     * Base class for StatementProxy and PreparedStatementProxy will intercept execute
     * calls and process {@code UNLOAD} and {@code COPY} commands.
     *
     * @param <T> Statement or PreparedStatement.
     */
    @AllArgsConstructor
    abstract static class AbstractStatementProxy<T extends Statement> implements InvocationHandler {

        protected final T statement;
        protected final Properties info;

        @Override
        public final Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable {
            final Optional<?> objectOptional;
            switch (method.getName()) {
            case "execute":
            case "executeUpdate":
                objectOptional = doExecute(args);
                break;
            case "executeQuery":
                objectOptional = doExecuteQuery(args);
                break;
            case "addBatch":
                objectOptional = doAddBatch(args);
                break;
            case "clearBatch":
                objectOptional = doClearBatch();
                break;
            case "executeBatch":
                objectOptional = doExecuteBatch();
                break;
            default:
                objectOptional = Optional.empty();
            }
            /*
             * Execute Redshift command if the execution result is present, else proceed with the original execution.
             */
            if (objectOptional.isPresent()) {
                return objectOptional.get();
            }
            try {
                return method.invoke(this.statement, args);
            } catch (final InvocationTargetException e) {
                return ExceptionUtils.handleSqlException(e, this.info);
            }
        }

        protected abstract Optional<Object> doExecute(Object[] args);

        protected abstract Optional<Object> doExecuteQuery(Object[] args);

        protected Optional<Object> doAddBatch(final Object[] args) {
            return Optional.empty();
        }

        protected Optional<Object> doClearBatch() {
            return Optional.empty();
        }

        @SneakyThrows
        protected Optional<int[]> doExecuteBatch() {
            return Optional.empty();
        }
    }

    /**
     * Intercepts execute calls, creates {@code UNLOAD} and {@code COPY} commands from the SQL
     * argument, and executes those commands.
     */
    static class StatementProxy extends
            AbstractStatementProxy<Statement> {

        protected final Connection connection;
        protected List<String> batch = new ArrayList<>();
        @Setter
        protected Statement proxyStatement;

        @Builder
        StatementProxy(final Statement statement, final Properties info, final Connection connection) {
            super(statement, info);
            this.connection = connection;
        }

        @Override
        public Optional<Object> doExecute(final Object[] args) {
            return ExecutionCommandParser.parse(args,
                    ExecutionCommandParser.ExecutionCommandContext.builder()
                            .connection(this.connection)
                            .info(this.info)
                            .build())
                    .map(command -> command.execute(this.statement));
        }

        @Override
        protected Optional<Object> doExecuteQuery(final Object[] args) {
            return ExecutionCommandParser.parse(args,
                            ExecutionCommandParser.ExecutionCommandContext.builder()
                                    .connection(this.connection)
                                    .info(this.info)
                                    .build())
                    .map(command -> command.executeQuery(this.statement));
        }

        @Override
        protected Optional<Object> doAddBatch(final Object[] args) {
            this.batch.add(args[0].toString());
            return Optional.of(true);
        }

        @Override
        protected Optional<Object> doClearBatch() {
            this.batch.clear();
            return Optional.of(true);
        }

        @Override
        @SneakyThrows
        protected Optional<int[]> doExecuteBatch() {
            final boolean isAutoCommit = this.connection.getAutoCommit();
            this.connection.setAutoCommit(true);
            try {
                return Optional.of(this.batch.stream().mapToInt(this::exec).toArray());
            } finally {
                this.connection.setAutoCommit(isAutoCommit);
            }
        }

        @SneakyThrows
        private int exec(final String sql) {
            this.proxyStatement.execute(sql);
            return 0;
        }
    }

    /**
     * Intercepts execute methods of a PreparedStatement and executes {@code UNLOAD} and {@code COPY} commands.
     */
    static class PreparedStatementProxy extends
            AbstractStatementProxy<PreparedStatement> {

        final ExecutionCommand<?> executionCommand;

        @Builder
        PreparedStatementProxy(final PreparedStatement statement,
                final Properties info, final ExecutionCommand<?> executionCommand) {
            super(statement, info);
            this.executionCommand = executionCommand;
        }

        @Override
        public Optional<Object> doExecute(final Object[] args) {
            if (this.executionCommand == null) {
                return Optional.empty();
            }
            return Optional.of(this.executionCommand.execute(this.statement));
        }

        @Override
        protected Optional<Object> doExecuteQuery(final Object[] args) {
            if (this.executionCommand == null) {
                return Optional.empty();
            }
            return Optional.of(this.executionCommand.executeQuery(this.statement));
        }
    }
}
