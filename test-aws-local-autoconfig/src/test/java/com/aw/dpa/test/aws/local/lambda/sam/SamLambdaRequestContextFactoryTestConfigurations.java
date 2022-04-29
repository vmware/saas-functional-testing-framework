/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda.sam;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.lambda.constants.TestConstants;
import com.aw.dpa.test.aws.local.service.Service;
import com.aw.dpa.test.aws.local.service.ServiceDependencies;
import com.aw.dpa.test.functional.aws.lambda.LambdaFunctionSpecs;

import static com.aw.dpa.test.aws.local.constants.LocalServiceConstants.Components.LAMBDA_ENDPOINT;

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
        LocalServiceEndpoint lambdaServiceEndpoint() {
            return new LocalServiceEndpoint("http", "localhost");
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
