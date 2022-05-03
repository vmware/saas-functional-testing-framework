/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process.wait.strategy;

import com.vmware.test.functional.saas.process.LocalTestProcessContext;

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
