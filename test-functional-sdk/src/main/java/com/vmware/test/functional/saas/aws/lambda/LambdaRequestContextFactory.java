/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.lambda;

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
