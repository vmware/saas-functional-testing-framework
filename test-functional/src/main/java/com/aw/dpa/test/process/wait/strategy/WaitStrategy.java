/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process.wait.strategy;

import com.aw.dpa.test.process.LocalTestProcessContext;

/**
 * Wait strategy abstraction used for local process start verification.
 */
public interface WaitStrategy {

    /**
     * Waits until process is verified.
     *
     * @param localTestProcessContext the local process context
     * @return True if Wait Strategy condition completed.
     */
    boolean hasCompleted(LocalTestProcessContext localTestProcessContext);

}
