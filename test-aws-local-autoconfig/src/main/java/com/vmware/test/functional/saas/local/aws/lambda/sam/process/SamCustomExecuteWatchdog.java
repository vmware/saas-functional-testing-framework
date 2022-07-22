/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.exec.ExecuteWatchdog;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom class extending {@link ExecuteWatchdog}. To be used for configuring how sam local process is destroyed.
 * Destroys the process tree of the sam process - not just the shell which starts it.
 */
@Slf4j
class SamCustomExecuteWatchdog extends ExecuteWatchdog {

    private static final int DESTROY_PROCESS_WAIT_TIME_MILLIS = 10000;

    private Process process;

    SamCustomExecuteWatchdog(final long timeout) {
        super(timeout);
    }

    @Override
    public synchronized void start(final Process processToMonitor) {
        super.start(processToMonitor);
        this.process = processToMonitor;
        log.info("SAM local start-lambda process started [{}]", getPid());
    }

    @Override
    public synchronized void destroyProcess() {
        kill(ProcessHandle.of(this.process.pid()).orElseThrow());
        super.destroyProcess();
    }

    public synchronized long getPid() {
        return (this.process == null) ? -1 : this.process.pid();
    }

    private void kill(final ProcessHandle handle) {
        handle.descendants().forEach(this::kill);
        handle.destroy();
        final CompletableFuture<Void> exitFuture = handle.onExit().thenAccept(a -> log.info("killed {}", a));
        if (!handle.destroy()) {
            return;
        }
        try {
            exitFuture.get(DESTROY_PROCESS_WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            log.error("SAM local start-lambda process was not destroyed so trying to destroy it forcibly [{}]", handle);
            handle.destroyForcibly();
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
