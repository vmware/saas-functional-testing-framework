/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
