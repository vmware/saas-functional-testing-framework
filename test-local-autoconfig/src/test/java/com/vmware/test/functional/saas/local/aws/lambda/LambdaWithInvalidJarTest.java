/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestData;
import com.vmware.test.functional.saas.local.aws.lambda.context.TestContext;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.LambdaWithInvalidJarContext.class))
@FunctionalTest
public class LambdaWithInvalidJarTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LambdaClient lambdaClient;

    @Test(expectedExceptions = AwsServiceException.class)
    public void invokeLambdaWithInvalidJar() {
        this.lambdaClient.invoke(InvokeRequest.builder()
                .payload(SdkBytes.fromUtf8String(TestData.INVOKE_LAMBDA_REQUEST_INPUT))
                .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                .invocationType("RequestResponse")
                .build());
    }
}
