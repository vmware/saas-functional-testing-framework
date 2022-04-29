/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.lambda;

/**
 * Lambda Request Context Factory.
 */
public interface LambdaRequestContextFactory {

    /**
     * Returns a new LambdaRequestContext for a given lambda function.
     *
     * @param lambdaName The lambda function name.
     *
     * @return {@link LambdaRequestContext}
     */
    LambdaRequestContext newRequestContext(String lambdaName);
}
