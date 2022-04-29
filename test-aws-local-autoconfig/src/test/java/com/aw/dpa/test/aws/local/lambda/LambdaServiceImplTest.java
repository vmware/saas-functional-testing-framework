/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda;

import software.amazon.awssdk.core.SdkBytes;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.aw.dpa.test.aws.local.lambda.constants.TestConstants;
import com.aw.dpa.test.aws.local.lambda.constants.TestData;
import com.aw.dpa.test.functional.aws.lambda.LambdaService;
import com.aw.dpa.test.functional.aws.lambda.LambdaServiceHelper;
import com.aw.dpa.test.functional.aws.lambda.LambdaServiceImpl;

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
