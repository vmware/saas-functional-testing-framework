/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Builder;
import lombok.Getter;

/**
 * Context for {@link LocalTestProcessCtl}.
 * Contains resources required by {@link LocalTestProcessCtl} and
 * by classes implementing {@link com.vmware.test.functional.saas.process.wait.strategy.WaitStrategy}
 */
@Builder
public final class LocalTestProcessContext {

    @Getter
    @Builder.Default
    private final BlockingDeque<String> logOutput = new LinkedBlockingDeque<>();

    @Getter
    @Builder.Default
    private AtomicReference<String> waitStrategiesLogResult = new AtomicReference<>();

}
