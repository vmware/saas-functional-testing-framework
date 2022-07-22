/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.testcontainers.lifecycle.Startable;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.SmartLifecyclePhases;

import lombok.extern.slf4j.Slf4j;

/**
 * Docker container deployer.
 * Will receive container list from Spring context, and will maintain their lifecycle.
 */
@Slf4j
public class GenericRunner implements SmartLifecycle, ApplicationContextAware {

    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;
    private ApplicationContext context;

    private List<Startable> containerList;
    private final AtomicReference<Boolean> running = new AtomicReference<>(Boolean.FALSE);

    public GenericRunner(final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        this.functionalTestExecutionSettings = functionalTestExecutionSettings;
    }

    @Override
    public void start() {
        if (this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip containers startup");
            return;
        }
        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            log.warn("Docker containers ALREADY started");
            return;
        }

        this.containerList = new ArrayList<>(this.context.getBeansOfType(Startable.class).values());
        if (!this.containerList.isEmpty()) {
            log.info("Starting Docker containers");
            this.containerList.parallelStream()
                    .peek(this::logStarting)
                    .forEach(Startable::start);
        }
    }

    private void logStarting(final Startable s) {
        log.info("starting container {}", System.identityHashCode(s));
    }

    private void logStopping(final Startable s) {
        log.info("stopping container {}", System.identityHashCode(s));
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            log.warn("Docker containers ALREADY stopped");
            return;
        }

        log.info("Stopping Docker containers");
        if (this.containerList != null && !this.containerList.isEmpty()) {
            this.containerList.parallelStream()
                    .peek(this::logStopping)
                    .forEach(Startable::close);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return SmartLifecyclePhases.GENERIC_RUNNER.getPhase();
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
