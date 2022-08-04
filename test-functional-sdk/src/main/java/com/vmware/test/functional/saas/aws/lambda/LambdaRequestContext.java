/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.lambda;

/**
 * Lambda request context.
 */
public interface LambdaRequestContext extends AutoCloseable {

    /**
     * Returns the contexts' RequestId.
     *
     * @return {@link String}.
     */
    String getRequestId();

    /**
     * Closes the context.
     */
    @Override
    void close();
}
