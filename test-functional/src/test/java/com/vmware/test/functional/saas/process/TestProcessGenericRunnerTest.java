/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link TestProcessGenericRunner}.
 */
@FunctionalTest
@ContextConfiguration(classes = TestProcessGenericRunnerTest.TestContext.class)
public class TestProcessGenericRunnerTest extends AbstractTestNGSpringContextTests {

    @Configuration
    public static class TestContext {

        private CommandLine command = TestCommand.createCommand();

        @Bean
        SimpleProcessLifecycle firstTestProcessToStart() {
            return SimpleProcessLifecycle.builder()
                    .lifecycleDelegate(LocalTestProcessCtl.builder()
                            .command(() -> this.command)
                            .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("Test App Log Line : 1").build())
                            .build())
                    .build();
        }

        @Bean
        SimpleProcessLifecycle secondTestProcessToStart() {
            return SimpleProcessLifecycle.builder()
                    .lifecycleDelegate(LocalTestProcessCtl.builder()
                            .command(() -> this.command)
                            .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("Test App Log Line : 1").build())
                            .build())
                    .build();
        }
    }

    @Autowired
    private SimpleProcessLifecycle firstTestProcessToStart;

    @Autowired
    private SimpleProcessLifecycle secondTestProcessToStart;

    @Test
    public void twoTestProcessesStarted() {
        assertThat("Running", this.firstTestProcessToStart.isRunning());
        assertThat("Running", this.secondTestProcessToStart.isRunning());
    }
}
