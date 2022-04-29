/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.process;

import org.springframework.context.SmartLifecycle;

/**
 * TestProcess lifecycle used for starting local processes used in tests.
 */
public interface TestProcessLifecycle extends SmartLifecycle {

    @Override
    default boolean isAutoStartup() {
        return false;
    }
}
