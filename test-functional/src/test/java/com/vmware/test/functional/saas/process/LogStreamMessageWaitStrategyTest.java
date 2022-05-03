/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process;

import java.util.concurrent.LinkedBlockingDeque;

import org.testng.annotations.Test;

import com.vmware.test.functional.saas.process.wait.strategy.LogStreamMessageWaitStrategy;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link LogStreamMessageWaitStrategy}.
 */
@Test
public class LogStreamMessageWaitStrategyTest {

    @Test
    public void logPatternMatched() {
        final LocalTestProcessContext localTestProcessContext = LocalTestProcessContext.builder()
                .logOutput(new LinkedBlockingDeque<String>() {
                    private static final long serialVersionUID = 1L;

                    {
                        add("Test");
                        add("Test1");
                    }
                })
                .build();
        assertThat("Log pattern matched", new LogStreamMessageWaitStrategy()
                .withRegEx("Test")
                .hasCompleted(localTestProcessContext));
    }

    @Test
    public void logPatternNotMatched() {
        final LocalTestProcessContext localTestProcessContext = LocalTestProcessContext.builder()
                .logOutput(new LinkedBlockingDeque<String>() {
                    private static final long serialVersionUID = 1L;

                    {
                        add("Test");
                        add("Test1");
                    }
                })
                .build();
        assertThat("Log pattern not matched", !new LogStreamMessageWaitStrategy()
                .withRegEx("Test123")
                .hasCompleted(localTestProcessContext));
    }

    @Test
    public void noLogOutput() {
        final LocalTestProcessContext localTestProcessContext = LocalTestProcessContext.builder()
                .logOutput(new LinkedBlockingDeque<String>())
                .build();
        assertThat("Log pattern not matched", !new LogStreamMessageWaitStrategy()
                .withRegEx("Test")
                .hasCompleted(localTestProcessContext));
    }

}
