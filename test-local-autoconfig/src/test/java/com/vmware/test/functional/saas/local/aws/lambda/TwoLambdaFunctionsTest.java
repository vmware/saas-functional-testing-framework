/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda;

import software.amazon.awssdk.core.SdkBytes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestData;
import com.vmware.test.functional.saas.local.aws.lambda.context.TestContext;
import com.vmware.test.functional.saas.aws.lambda.LambdaService;
import com.vmware.test.functional.saas.aws.lambda.LambdaServiceHelper;

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
