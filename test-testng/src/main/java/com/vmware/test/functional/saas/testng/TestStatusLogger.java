/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

// CHECKSTYLE DISABLE IllegalImport FOR 1 LINES
import ch.qos.logback.classic.LoggerContext;

/**
 * Log test start/stop, the test result, and the time it took to run the test.
 */
public class TestStatusLogger implements ITestListener {

    @Override
    public void onTestStart(final ITestResult tr) {
        getLogger(tr).info("{} -- START",
                tr.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(final ITestResult tr) {
        logTestEnd(tr, "FAILED");
    }

    @Override
    public void onTestSkipped(final ITestResult tr) {
        logTestEnd(tr, "SKIPPED");
    }

    @Override
    public void onTestSuccess(final ITestResult tr) {
        logTestEnd(tr, "SUCCESS");
    }

    private static void logTestEnd(final ITestResult tr, final String msg) {
        getLogger(tr).info("{} -- {} ({} ms)\n",
                tr.getMethod().getMethodName(),
                msg,
                tr.getEndMillis() - tr.getStartMillis());
    }

    private static Logger getLogger(final ITestResult tr) {
        final Logger log = LoggerFactory.getLogger(tr.getMethod().getRealClass());
        if (!log.isInfoEnabled()) {
            // Make sure that logging is on.
            final LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
            final ch.qos.logback.classic.Logger l = lc.exists(tr.getMethod().getRealClass().getName());
            l.setLevel(ch.qos.logback.classic.Level.INFO);
        }
        return log;
    }

}
