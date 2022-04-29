/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.lambda;

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
