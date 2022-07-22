/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas;

import org.springframework.context.SmartLifecycle;

import lombok.Getter;

/**
 * Enum class holding phase configuration of different SmartLifecycle controls
 * in the framework.
 */
public enum SmartLifecyclePhases {

    TRINO_CATALOG_CREATOR(Integer.MIN_VALUE),
    SAM_PROCESS_CONTROL(Integer.MIN_VALUE),
    GENERIC_RUNNER(-200),
    RESOURCE_CREATORS(1),
    RESOURCE_AWAITING_INITIALIZERS(2),
    TEST_PROCESS_GENERIC_RUNNER(SmartLifecycle.DEFAULT_PHASE);

    @Getter
    final int phase;

    SmartLifecyclePhases(final int phase) {
        this.phase = phase;
    }
}
