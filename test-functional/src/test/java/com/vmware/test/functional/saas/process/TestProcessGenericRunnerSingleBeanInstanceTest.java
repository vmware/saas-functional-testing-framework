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
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test for validating {@link TestProcessGenericRunner} is reused in child contexts.
 */
@FunctionalTest
@ContextHierarchy({
        @ContextConfiguration(classes = TestProcessGenericRunnerSingleBeanInstanceTest.TestContext.class),
        @ContextConfiguration(classes = TestProcessGenericRunnerSingleBeanInstanceTest.ChildContext.class)
})
public class TestProcessGenericRunnerSingleBeanInstanceTest extends AbstractTestNGSpringContextTests {

    @Configuration
    public static class TestContext {

        private CommandLine command = TestCommand.createCommand();

        @Bean
        LocalTestProcess testProcessToStart() {
            return LocalTestProcess.builder()
                    .lifecycleDelegate(LocalTestProcessCtl.builder()
                            .command(() -> this.command)
                            .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("Test App Log Line : 1").build())
                            .build())
                    .build();
        }
    }

    @Configuration
    public static class ChildContext {

    }

    @Autowired
    private LocalTestProcess testProcessToStart;

    @Test
    public void testProcessesStarted() {
        assertThat("Running", this.testProcessToStart.isRunning());
    }

}
