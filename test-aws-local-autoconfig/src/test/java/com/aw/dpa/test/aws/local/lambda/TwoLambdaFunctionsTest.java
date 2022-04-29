/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda;

import software.amazon.awssdk.core.SdkBytes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.aws.local.lambda.constants.TestConstants;
import com.aw.dpa.test.aws.local.lambda.constants.TestData;
import com.aw.dpa.test.aws.local.lambda.context.TestContext;
import com.aw.dpa.test.functional.aws.lambda.LambdaService;
import com.aw.dpa.test.functional.aws.lambda.LambdaServiceHelper;

import static org.hamcrest.MatcherAssert.*;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.TwoLambdaFunctionsContext.class))
@FunctionalTest
public class TwoLambdaFunctionsTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LambdaService<SdkBytes, String> lambdaService;

    @Test
    public void isExpectedLambdaResponseWhenTwoFunctionsInvoked() {
        final SdkBytes firstFunctionResult = this.lambdaService.invoke(TestConstants.TEST_LAMBDA_FUNCTION_NAME, TestData.INVOKE_LAMBDA_REQUEST_INPUT);
        assertThat("Lambda produces expected result", LambdaServiceHelper.matchesExpectedResultMessage(TestData.LAMBDA_EXPECTED_RESULT,
                LambdaServiceHelper.extractSdkBytesPayloadToString(firstFunctionResult)));

        final SdkBytes secondFunctionResult = this.lambdaService.invoke(TestConstants.TEST_LAMBDA_FUNCTION_NAME_2, TestData.INVOKE_LAMBDA_REQUEST_INPUT);
        assertThat("Lambda produces expected result", LambdaServiceHelper.matchesExpectedResultMessage(TestData.LAMBDA_EXPECTED_RESULT,
                LambdaServiceHelper.extractSdkBytesPayloadToString(secondFunctionResult)));
    }
}
