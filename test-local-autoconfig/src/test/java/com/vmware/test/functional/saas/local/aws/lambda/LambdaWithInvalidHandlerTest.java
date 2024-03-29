/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestData;
import com.vmware.test.functional.saas.local.aws.lambda.context.TestContext;
import com.vmware.test.functional.saas.local.aws.lambda.utils.LambdaResponseUtils;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@FunctionalTest
@ContextConfiguration(classes = TestContext.LambdaWithInvalidHandlerContext.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LambdaWithInvalidHandlerTest extends AbstractTestNGSpringContextTests {

    private static final String INVALID_LAMBDA_HANDLER_ERROR_MESSAGE = "Class not found: " + TestData.TEST_INVALID_LAMBDA_HANDLER_CLASS_NAME;
    @Autowired
    private LambdaClient lambdaClient;

    @Test
    public void invokeLambdaWithInvalidHandler() {
        final InvokeResponse lambdaResponse = this.lambdaClient.invoke(InvokeRequest.builder()
                .payload(SdkBytes.fromUtf8String(TestData.INVOKE_LAMBDA_REQUEST_INPUT))
                .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                .invocationType("RequestResponse")
                .build());

        final String errorMessage = LambdaResponseUtils.getErrorMessageOrNull(lambdaResponse);
        assertThat("Lambda invocation did not return function error but was expected to.", errorMessage, notNullValue());
        assertThat("Lambda error message is not as expected.", errorMessage, is(INVALID_LAMBDA_HANDLER_ERROR_MESSAGE));
    }
}
