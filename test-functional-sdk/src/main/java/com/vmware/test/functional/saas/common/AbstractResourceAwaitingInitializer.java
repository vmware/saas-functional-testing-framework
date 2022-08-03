/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.SmartLifecyclePhases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Parent class for all AWS Post initializers.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractResourceAwaitingInitializer implements SmartLifecycle, ApplicationContextAware {

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
