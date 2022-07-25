/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.lambda;

import java.util.function.Consumer;

/**
 * Simplified operations for interacting with the AWS Lambda.
 */
public interface LambdaService<R, T> {

    /**
     * Invoke Lambda function.
     * @param functionName Name of the Lambda function.
     * @param payload Payload for the Lambda function.
     * @return R
     */
    R invoke(String functionName, T payload);

    /**
     * Invoke Lambda function.
     * @param functionName Name of the Lambda function.
     * @param payload Payload for the Lambda function.
     * @param responseConsumer Consumer, used for Lambda response validation in case of async execution.
     */
    void invoke(String functionName, T payload, Consumer<R> responseConsumer);
}
