/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.lambda;

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
