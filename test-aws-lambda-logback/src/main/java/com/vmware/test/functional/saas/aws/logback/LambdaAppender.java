/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.logback;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jlib.cloud.aws.lambda.logback.AwsLambdaAppender;
import org.slf4j.MDC;

// CHECKSTYLE DISABLE IllegalImport FOR 5 LINES
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Writes the {@code functionalTestRequestId} variable to the MDC and dispatches an event
 * with {@value #CONTEXT_INITIALIZED} payload.
 */
public final class LambdaAppender extends AwsLambdaAppender {

    public static final String FUNCTIONAL_TEST_REQUEST_ID = "functionalTestRequestId";
    public static final String CONTEXT_INITIALIZED = "ContextInitialized";
    public static final String APPENDER_STOPPED = "AppenderStopped";
    public static final String FAILED_OPENING_REQID_FILE = "Failed opening reqId file with exception:";
    public static final String REQID_FILE = "reqId";

    private final Logger logger;
    private String requestId;
    private boolean printStatus;

    public LambdaAppender() {
        super();
        this.logger = new LoggerContext().getLogger(LambdaAppender.class);
    }

    @Override
    public void start() {
        if (printStatus) {
            getStatusManager().add(status -> System.out.println("!!!Status!!! " + status.toString()));
        }

        super.start();
        this.requestId = readTestRequestId();

        // deferred to not pollute the MDC
        Executors.newScheduledThreadPool(1).schedule(this::setRequestIdAndAppend,
              100, TimeUnit.MILLISECONDS);
    }

    private void setRequestIdAndAppend() {
        final LoggingEvent event = getLogEvent(Level.INFO, CONTEXT_INITIALIZED, null);
        event.setMDCPropertyMap(Map.of(FUNCTIONAL_TEST_REQUEST_ID, this.requestId));
        this.append(event);
    }

    @Override
    public void stop() {
        final ILoggingEvent event = getLogEvent(Level.INFO, APPENDER_STOPPED, null);
        this.append(event);
        super.stop();
    }

    @Override
    public void append(final ILoggingEvent event) {
        MDC.put(FUNCTIONAL_TEST_REQUEST_ID, this.requestId);
        super.append(event);
    }

    public void setPrintStatus(boolean printStatus) {
        this.printStatus = printStatus;
    }

    private LoggingEvent getLogEvent(final Level level, final String message, final Throwable throwable) {
        return new LoggingEvent(
                this.getClass().getCanonicalName(),
                this.logger,
                level,
                message,
                throwable,
                new Object[0]);
    }

    private String readTestRequestId() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/var/task/" + REQID_FILE))) {
            return reader.readLine();
        } catch (final FileNotFoundException e) {
            final ILoggingEvent event = getLogEvent(Level.INFO, FAILED_OPENING_REQID_FILE, e);
            this.append(event);
            return "unknown";
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
