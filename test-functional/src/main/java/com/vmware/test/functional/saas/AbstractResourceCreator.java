/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Parent class for all resource initializers.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractResourceCreator implements SmartLifecycle, ApplicationContextAware {

    @Getter
    private ApplicationContext context;
    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;

    private volatile boolean running;

    @Override
    public final void start() {
        if (this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip creating resources");
            return;
        }
        this.running = true;
        doStart();
    }

    protected abstract void doStart();

    @Override
    public final void stop() {
        this.running = false;
    }

    @Override
    public final boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return SmartLifecyclePhases.RESOURCE_CREATORS.getPhase();
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        this.context = context;
    }
}
