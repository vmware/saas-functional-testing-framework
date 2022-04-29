/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process.wait.strategy;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.aw.dpa.test.process.LocalTestProcessContext;

/**
 * Wait strategy for local process log message pattern verification.
 */
public class LogStreamMessageWaitStrategy implements WaitStrategy {

    private Pattern logMessagePattern;

    public LogStreamMessageWaitStrategy() {
    }

    /**
     * Provide path to wait for health check.
     *
     * @param regEx the regEx pattern to match
     * @return this
     */
    public LogStreamMessageWaitStrategy withRegEx(final String regEx) {
        this.logMessagePattern = Pattern.compile(regEx);
        return this;
    }

    @Override
    public boolean hasCompleted(final LocalTestProcessContext localTestProcessContext) {
        final Iterator<String> it = localTestProcessContext.getLogOutput().iterator();
        while (it.hasNext()) {
            final String line = it.next();
            if (StringUtils.isBlank(line)) {
                continue;
            }
            final Matcher m = this.logMessagePattern.matcher(line);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("WaitStrategy for log pattern : %s %n", this.logMessagePattern);
    }
}
