/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.environment.operations;

/**
 * To be implemented by operations that want to release resources on destruction.
 */
public interface Disposable {

    /**
     * Cleans up all resources created by this object.
     */
    void teardown();
}
