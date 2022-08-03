/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.process;

import java.time.Duration;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link LocalTestProcessCtl}.
 */
@Test
public class LocalTestProcessCtlTest {
    private boolean isPreStartCallbackExecuted;
    private CommandLine command;

    @BeforeClass(alwaysRun = true)
    public void setUpCommand() {
        this.command = TestCommand.createCommand();
    }

    @Test
    public void startProcess() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessTwice() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .build();
        testProcessCtl.start();
        testProcessCtl.start();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessImmediateWithWaitStrategy() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .environmentSupplier(() -> Map.of(TestApp.IMMEDIATE, Boolean.toString(true)))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("Test App Log Line : 2")
                        .build())
                .build();
        testProcessCtl.start();
    }

    @Test
    public void startProcessWithPreStartCallback() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .preStartCallback(this::preStartCallbackHandler)
                .build();
        testProcessCtl.start();
        assertThat("Pre start runnable is executed.", this.isPreStartCallbackExecuted);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithFailingWaitStrategy() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder().forHttp("invalid url").build())
                .build();
        testProcessCtl.start();
    }

    @Test
    public void stopLocalProcess() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .build();
        testProcessCtl.start();
        testProcessCtl.stop();
        assertThat("Stopped", !testProcessCtl.isRunning());
    }

    @Test
    public void stopLocalProcessTwice() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .build();
        testProcessCtl.start();
        testProcessCtl.stop();
        testProcessCtl.stop();
        assertThat("Stopped", !testProcessCtl.isRunning());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithNoCommandPassed() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .build();
        testProcessCtl.start();
    }

    @Test
    public void startProcessWithWaitStrategy() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("^Test App Log Line : 1$").build())
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test
    public void startProcessWithTwoWaitStrategies() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("^Test App Log Line : 2$")
                        .forLogMessagePattern("^Test App Log Line : 3$")
                        .build())
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithTwoWaitStrategiesOneFailing() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("Test App Log Line : 2")
                        .forLogMessagePattern("invalid log line")
                        .build())
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithTwoFailingWaitStrategies() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> this.command)
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("invalid log line 1")
                        .forLogMessagePattern("invalid log line 2")
                        .build())
                .build();
        testProcessCtl.start();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithInvalidCommandArgument() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> new CommandLine("java")
                        .addArgument("-invalid"))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("invalid log line 1")
                        .build())
                .build();
        testProcessCtl.start();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startProcessWithInvalidExecutableCommand() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> new CommandLine("invalid executable"))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("log line")
                        .build())
                .build();
        testProcessCtl.start();
    }

    private void preStartCallbackHandler() {
        this.isPreStartCallbackExecuted = true;
    }
}
