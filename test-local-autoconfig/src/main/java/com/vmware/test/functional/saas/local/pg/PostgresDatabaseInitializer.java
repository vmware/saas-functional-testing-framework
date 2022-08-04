/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.pg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility bean to initialize the application database (postgreSQL).
 */
@Slf4j
public class PostgresDatabaseInitializer {

    private final String postgresContainerId;

    public PostgresDatabaseInitializer(final String postgresContainerId) {
        this.postgresContainerId = postgresContainerId;
    }

    /**
     * Call {@code initdb.sh} to create the app database.
     *
     * @param path {@code initdb} script absolute or relative path.
     * @param dbName Database name.
     * @param dbRole Database role.
     * @return Process exit value.
     */
    public int runAppInitDbScript(final String path, final String dbName, final String dbRole) {
        try {
            final CommandLine cmd = new CommandLine("/bin/bash");
            cmd.addArgument(path);
            cmd.addArgument("-f");
            cmd.addArgument("-d");
            cmd.addArgument(dbName);
            cmd.addArgument("-r");
            cmd.addArgument(dbRole);

            final Map<String, String> env = new HashMap<>(System.getenv());
            env.put("PG_CONTAINER_ID", this.postgresContainerId);

            final Executor executor = new DefaultExecutor();

            final PumpStreamHandler psh = new PumpStreamHandler(new CustomLogOutputStream());
            executor.setStreamHandler(psh);

            final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            executor.execute(cmd, env, resultHandler);
            resultHandler.waitFor();
            return resultHandler.getExitValue();
        } catch (final InterruptedException exn) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("failed to run initdb.sh", exn);
        } catch (final IOException exn) {
            throw new IllegalStateException("failed to run initdb.sh - I') exception", exn);
        }
    }

    /**
     * Sends/redirects ("pumps") the script {@code stdout} to the application logs ({@code INFO} level).
     */
    static class CustomLogOutputStream extends LogOutputStream {

        @Override
        protected void processLine(final String line, final int logLevel) {
            log.debug(line);
        }
    }
}
