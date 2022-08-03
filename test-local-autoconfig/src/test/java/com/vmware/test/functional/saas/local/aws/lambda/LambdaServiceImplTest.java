/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda;

import software.amazon.awssdk.core.SdkBytes;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestData;
import com.vmware.test.functional.saas.aws.lambda.LambdaService;
import com.vmware.test.functional.saas.aws.lambda.LambdaServiceHelper;
import com.vmware.test.functional.saas.aws.lambda.LambdaServiceImpl;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link LambdaServiceImpl}.
 */
public class LambdaServiceImplTest extends AbstractFullContextTest {

    @Autowired
    private LambdaService<SdkBytes, String> lambdaService;

    @Autowired
    private LambdaService<String, String> lambdaServiceLogExtractor;

    @Test
    public void isExpectedLambdaResponseWhenInvoked() {
        final SdkBytes result = this.lambdaService.invoke(TestConstants.TEST_LAMBDA_FUNCTION_NAME, TestData.INVOKE_LAMBDA_REQUEST_INPUT);
        assertThat("Lambda produces expected result", LambdaServiceHelper.matchesExpectedResultMessage(TestData.LAMBDA_EXPECTED_RESULT,
                LambdaServiceHelper.extractSdkBytesPayloadToString(result)));
    }

    @Test
    public void isExpectedLambdaResponseWhenInvokedWithResponseConsumer() {
        this.lambdaService.invoke(TestConstants.TEST_LAMBDA_FUNCTION_NAME, TestData.INVOKE_LAMBDA_REQUEST_INPUT,
                x -> assertThat("Lambda produces expected result",
                        LambdaServiceHelper.matchesExpectedResultMessage(TestData.LAMBDA_EXPECTED_RESULT, LambdaServiceHelper.extractSdkBytesPayloadToString(x))));
    }

    @Test
    public void invokeLocalLambdaLog() {
        final String result = this.lambdaServiceLogExtractor.invoke(
                TestConstants.TEST_LAMBDA_FUNCTION_NAME,
                TestData.INVOKE_LAMBDA_REQUEST_INPUT);
        assertThat("Lambda produces expected log", LambdaServiceHelper.matchesExpectedResultMessage(TestData.LAMBDA_EXPECTED_LOG, result));
    }
}
