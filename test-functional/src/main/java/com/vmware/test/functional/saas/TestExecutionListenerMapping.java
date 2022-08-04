/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas;

import java.util.Map;

import org.springframework.test.context.TestExecutionListener;

import lombok.Builder;
import lombok.Getter;

/**
 * This is a generic listener, that contains autowired processors for specific test classes.
 * If test processor is defined for certain class its events handlers are invoked when events arise.
 */
@Builder
@Getter
public class TestExecutionListenerMapping {

    private static final TestExecutionListener NO_OP_LISTENER = new TestExecutionListener() {
    };

    private final Map<Class<?>, TestExecutionListener> testExecutionListenerByTestClass;

    TestExecutionListener getTestExecutionListener(final Class<?> clazz) {
        return this.testExecutionListenerByTestClass.getOrDefault(clazz, NO_OP_LISTENER);
    }
}
