/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.process.wait.strategy.WaitStrategyBuilder;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests to verify port allocations.
 */
@FunctionalTest
@ContextConfiguration(classes = TestPortAllocationTest.TestContext.class)
public class TestPortAllocationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LocalTestProcess testProcessToStart;

    @Test
    public void ensureProcessPortIsCalculatedJustBeforeTheProcessStarted() {
        assertThat("Running", this.testProcessToStart.isRunning());
    }

    @Configuration
    @Slf4j
    public static class TestContext {

        private volatile boolean ready;

        @Bean
        @Lazy
        public LocalServiceEndpoint serviceEndpoint() {
            if (!this.ready) {
                throw new RuntimeException();
            }
            log.info("allocating port");
            return new LocalServiceEndpoint("http");
        }

        @Bean
        LocalTestProcess testProcessToStart(@Lazy final LocalServiceEndpoint serviceEndpoint) {
            return LocalTestProcess.builder()
                    .lifecycleDelegate(LocalTestProcessCtl.builder()
                            .command(() -> TestCommand.createCommand(serviceEndpoint.getPort()))
                            .environmentSupplier(() -> buildEnv(serviceEndpoint))
                            .preStartCallback(this::preStartCallback)
                            .waitingFor(new WaitStrategyBuilder().forHttp(serviceEndpoint::getEndpoint).build())
                            .build())
                    .build();
        }

        private void preStartCallback() {
            this.ready = true;
        }

        private HashMap<String, String> buildEnv(final LocalServiceEndpoint serviceEndpoint) {
            final HashMap<String, String> env = new HashMap<>();
            env.put("TEST_ENDPOINT_VAR", "" + serviceEndpoint.getEndpoint());
            return env;
        }
    }
}
