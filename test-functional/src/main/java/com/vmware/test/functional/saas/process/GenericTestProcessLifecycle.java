/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic implementation of {@code TestProcessLifecycle}. It allows the caller to provide custom start and stop behaviour.
 * @param <T> start callback may return a generic value of type {@code T} associated with the execution result. The caller
 *           can access the value using {@code getStartResult}
 */
@Builder
@Slf4j
public class GenericTestProcessLifecycle<T> implements TestProcessLifecycle {

    private final Supplier<T> startFunction;
    private final Runnable stopFunction;
    @Builder.Default
    private final AtomicBoolean running = new AtomicBoolean();
    @Getter
    private T startResult;

    @Override
    public void start() {
        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            throw new RuntimeException("ALREADY started");
        }
        if (this.startFunction != null) {
            this.startResult = this.startFunction.get();
        }
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            log.warn("ALREADY stopped");
            return;
        }
        if (this.stopFunction != null) {
            this.stopFunction.run();
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }
}
