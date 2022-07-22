/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process.output;

import java.util.Queue;

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;

/**
 * Stream Handler used to process log output from local processes started.
 */
public class LocalProcessStreamHandler extends LogOutputStream {

    private final Logger logger;
    private final Queue<String> logQueue;

    public LocalProcessStreamHandler(final Logger logger, final Queue<String> logQueue) {
        this.logger = logger;
        this.logQueue = logQueue;
    }

    @Override
    protected void processLine(final String line, final int logLevel) {
        this.logger.debug(line);
        this.logQueue.add(line);
    }
}
