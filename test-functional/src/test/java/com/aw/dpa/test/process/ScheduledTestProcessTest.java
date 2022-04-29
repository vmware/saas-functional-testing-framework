/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.process.wait.strategy.WaitStrategyBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link TestProcessGenericRunner}.
 */
@FunctionalTest
@ContextConfiguration(classes = ScheduledTestProcessTest.TestContext.class)
public class ScheduledTestProcessTest extends AbstractTestNGSpringContextTests {

    @Configuration
    public static class TestContext {

        @Bean
        GenericTestProcessLifecycle<List<Boolean>> processToSchedule() {
            return GenericTestProcessLifecycle.<List<Boolean>>builder()
                    .startFunction(() ->
                            /* We create a periodic schedule to run the process once per second
                             * - "interval(...)" function infinitely emits values every second.
                             * then we cancel after the second run  - "take(2)" will take just the
                             * the first two values and than will complete the flux
                             * by sending cancel signal. Combining with "block()" terminal action,
                             * we will block the startup of GenericTestProcessLifecycle component for
                             * 2 seconds (2 iteration of the periodic schedule).
                             */
                            Flux.interval(Duration.of(1, SECONDS))
                                    .take(2)
                                    .map(this::buildNextLocalProcess)
                                    .doOnNext(LocalTestProcessCtl::start)
                                    .map(LocalTestProcessCtl::isRunning)
                                    // collect the result for all executions
                                    .collectList()
                                    // block until all iterations are executed
                                    .block())
                    .build();
        }

        @Bean
        GenericTestProcessLifecycle<Mono<Boolean>> faultyProcessToSchedule() {
            // the expected error is "process has exited successfully but wait strategies did not complete"
            // We don't consume the stream yet, but allow the caller (test method) to materialize it and so
            // asserts the underlying exception.
            return GenericTestProcessLifecycle.<Mono<Boolean>>builder()
                    .startFunction(() -> Mono.just(buildFaultyProcess())
                            .doOnNext(LocalTestProcessCtl::start)
                            .map(LocalTestProcessCtl::isRunning))
                    .build();
        }

        LocalTestProcessCtl buildNextLocalProcess(final long token) {
            return LocalTestProcessCtl.builder()
                    .command(TestCommand::createCommand)
                    .environmentSupplier(
                            () -> Map.of(
                                    TestApp.TEST_ENV_VAR_1, Long.toString(token),
                                    TestApp.IMMEDIATE, Boolean.toString(true)))
                    .waitingFor(new WaitStrategyBuilder()
                            .forLogMessagePattern(String.format("Test App Log Line : %s", token))
                            .build())
                    .build();
        }

        LocalTestProcessCtl buildFaultyProcess() {
            return LocalTestProcessCtl.builder()
                    .command(TestCommand::createCommand)
                    .environmentSupplier(
                            () -> Map.of(
                                    TestApp.IMMEDIATE, Boolean.toString(true)))
                    .waitingFor(new WaitStrategyBuilder()
                            .forLogMessagePattern("Test App Log Line : null_token")
                            .build())
                    .build();
        }
    }

    @Autowired
    private GenericTestProcessLifecycle<List<Boolean>> processToSchedule;
    @Autowired
    private GenericTestProcessLifecycle<Mono<Boolean>> faultyProcessToSchedule;

    @Test
    public void testScheduledProcessesExitsSuccessfully() {
        final List<Boolean> executions = this.processToSchedule.getStartResult();
        assertThat(executions, notNullValue());
        assertThat(executions.size(), is(2));
        assertThat("First execution is ok", executions.get(0));
        assertThat("Second execution is ok", executions.get(1));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testScheduledProcessesExitsWithError() {
        this.faultyProcessToSchedule.getStartResult().block();
    }
}
