/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.process;

import org.springframework.context.Lifecycle;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * Implements TestProcessLifecycle contract for a given LocalTestProcessCtl.
 */
@Builder
public class TestProcessLifecycleDelegate implements TestProcessLifecycle {

    @Delegate
    @Getter(AccessLevel.PACKAGE)
    private final Lifecycle lifecycleDelegate;
}
