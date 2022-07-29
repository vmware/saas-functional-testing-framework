/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;

import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.LAMBDA_ENDPOINT;

public final class SamLambdaRequestContextFactoryTestConfigurations {

    public static final String TEST_LAMBDA_NAME = "SamLambdaRequestContextLambda";

    private SamLambdaRequestContextFactoryTestConfigurations() { }

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class TestContext {

        @Value("${lambda.code.uri}")
        private String lambdaCodeDir;

        @Bean
        LambdaFunctionSpecs lambdaFunctionSpecs() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TEST_LAMBDA_NAME)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .build();
        }
    }

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class OtherTestContext {

        @Value("${lambda.code.uri}")
        private String lambdaCodeDir;

        @Bean
        @Qualifier(value = LAMBDA_ENDPOINT)
        ServiceEndpoint lambdaServiceEndpoint() {
            return new ServiceEndpoint("http", "localhost");
        }

        @Bean
        FunctionalTestExecutionSettings functionalTestExecutionSettings() {
            return new FunctionalTestExecutionSettings();
        }

        @Bean
        LambdaFunctionSpecs lambdaFunctionSpecs() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TEST_LAMBDA_NAME)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .build();
        }
    }

}
