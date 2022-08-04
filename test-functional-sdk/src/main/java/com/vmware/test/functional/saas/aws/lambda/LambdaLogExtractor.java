/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.lambda;

/**
 * Lambda log extractor.
 */
@FunctionalInterface
public interface LambdaLogExtractor {

    /**
     * Method for extracting lambda logs for a given {@link LambdaRequestContext}.
     *
     * @param requestContext RequestContext to filter logs by.
     * @return Aggregated lambda log result.
     */
    String getLambdaLogsForRequestContext(LambdaRequestContext requestContext);
}
