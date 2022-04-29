/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.SmartLifecyclePhases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Parent class for all AWS Post initializers.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAwsResourceAwaitingInitializer implements SmartLifecycle, ApplicationContextAware {

    @Getter
    private ApplicationContext context;

    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;

    private volatile boolean running;

    @Override
    public final void start() {
        if (this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip awaiting resources");
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
    public final int getPhase() {
        return SmartLifecyclePhases.RESOURCE_AWAITING_INITIALIZERS.getPhase();
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
