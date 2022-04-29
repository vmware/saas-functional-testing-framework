/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.SmartLifecycle;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.SmartLifecyclePhases;

import lombok.extern.slf4j.Slf4j;

/**
 * Local Test Process Runner.
 * Receives {@link TestProcessLifecycle} list from Spring context and maintains their lifecycle.
 */
@Slf4j
public class TestProcessGenericRunner implements SmartLifecycle {

    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;
    private final List<TestProcessLifecycle> testProcessList;
    private final AtomicReference<Boolean> running = new AtomicReference<>(Boolean.FALSE);

    public TestProcessGenericRunner(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final List<TestProcessLifecycle> testProcessList) {
        this.testProcessList = testProcessList;
        this.functionalTestExecutionSettings = functionalTestExecutionSettings;
    }

    @Override
    public void start() {
        if (this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip process startup");
            return;
        }
        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            log.warn("Test Processes ALREADY started");
            return;
        }

        log.info("Starting Test Processes");
        if (this.testProcessList != null && !this.testProcessList.isEmpty()) {
            this.testProcessList.parallelStream().forEach(TestProcessLifecycle::start);
        }
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            log.warn("Test Processes ALREADY stopped");
            return;
        }

        log.info("Stopping Test Processes");

        if (this.testProcessList != null && !this.testProcessList.isEmpty()) {
            this.testProcessList.parallelStream().forEach(TestProcessLifecycle::stop);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return SmartLifecyclePhases.TEST_PROCESS_GENERIC_RUNNER.getPhase();
    }
}
