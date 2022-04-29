/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.aws.local.lambda.constants.TestConstants;
import com.aw.dpa.test.aws.local.lambda.constants.TestData;
import com.aw.dpa.test.aws.local.lambda.context.TestContext;
import com.aw.dpa.test.aws.local.lambda.utils.LambdaResponseUtils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.LambdaWithInvalidJarContext.class))
@FunctionalTest
public class LambdaWithInvalidJarTest extends AbstractTestNGSpringContextTests {

    private static final String INVALID_LAMBDA_JAR_ERROR_MESSAGE = "Class not found: " + TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME;

    @Autowired
    private LambdaClient lambdaClient;

    @Test
    public void invokeLambdaWithInvalidJar() {
        final InvokeResponse lambdaResponse = this.lambdaClient.invoke(InvokeRequest.builder()
                .payload(SdkBytes.fromUtf8String(TestData.INVOKE_LAMBDA_REQUEST_INPUT))
                .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                .invocationType("RequestResponse")
                .build());

        final String errorMessage = LambdaResponseUtils.getErrorMessageOrNull(lambdaResponse);
        assertThat("Lambda invocation did not return function error but was expected to.", errorMessage, notNullValue());
        assertThat("Lambda error message is not as expected.", errorMessage, is(INVALID_LAMBDA_JAR_ERROR_MESSAGE));
    }
}
