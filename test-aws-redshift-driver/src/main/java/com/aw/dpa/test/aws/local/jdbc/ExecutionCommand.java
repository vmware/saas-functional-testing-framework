/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * An adapter for the {@code UNLOAD} and {@code COPY} commands to execute methods using the
 * {@link PreparedStatement} and {@link Statement} interfaces.
 *
 * @param <T> return type of the execute methods.
 */
public interface ExecutionCommand<T> {

    /**
     * Creates {@link PreparedStatement} specific for {@code UNLOAD} and {@code COPY} commands.
     *
     * @param connection SQL connection.
     * @return {@link PreparedStatement}.
     */
    PreparedStatement prepareStatement(Connection connection);

    /**
     * Executes SQL {@link PreparedStatement} specific for {@code UNLOAD} and {@code COPY} commands.
     *
     * @param preparedStatement SQL {@link PreparedStatement}.
     * @return Execution result.
     */
    T execute(PreparedStatement preparedStatement);

    /**
     * Execute SQL {@link Statement} specific for {@code UNLOAD} and {@code COPY} commands.
     *
     * @param statement SQL {@link Statement}.
     * @return Execution result.
     */
    T execute(Statement statement);

    /**
     * Execute a SQL {@link Statement}.
     *
     * @param statement SQL {@link Statement}.
     * @return {@link ResultSet}
     */
    default ResultSet executeQuery(Statement statement) {
        throw new UnsupportedOperationException();
    }

    /**
     * Execute a SQL {@link PreparedStatement}.
     *
     * @param preparedStatement SQL {@link PreparedStatement}.
     * @return {@link ResultSet}
     */
    default ResultSet executeQuery(PreparedStatement preparedStatement) {
        throw new UnsupportedOperationException();
    }
}
