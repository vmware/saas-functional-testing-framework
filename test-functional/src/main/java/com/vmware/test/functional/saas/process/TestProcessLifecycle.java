/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.process;

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
