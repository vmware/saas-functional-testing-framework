/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process;

import org.springframework.context.Lifecycle;

import lombok.Builder;

/**
 * Implements TestProcessLifecycle contract for an arbitrary Lifecycle object. Used for testing.
 */
@Builder
public class LocalTestProcess implements TestProcessLifecycle {

    Lifecycle lifecycleDelegate;

    @Override
    public void start() {
        this.lifecycleDelegate.start();
    }

    @Override
    public void stop() {
        this.lifecycleDelegate.stop();
    }

    @Override
    public boolean isRunning() {
        return this.lifecycleDelegate.isRunning();
    }

}
