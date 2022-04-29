/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda.context;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aw.dpa.test.aws.local.lambda.constants.TestConstants;
import com.aw.dpa.test.aws.local.lambda.constants.TestData;
import com.aw.dpa.test.aws.local.service.Service;
import com.aw.dpa.test.aws.local.service.ServiceDependencies;
import com.aw.dpa.test.functional.aws.lambda.LambdaFunctionSpecs;
import com.aw.dpa.test.functional.aws.lambda.LambdaService;
import com.aw.dpa.test.functional.aws.lambda.LambdaServiceHelper;
import com.aw.dpa.test.functional.aws.lambda.LambdaServiceImpl;

public class TestContext {

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class FullContext {

        @Value("${lambda.code.uri}")
        private String lambdaCodeDir;

        @Value("${test.lambda.timeout:5}")
        private int functionTimeout;

        @Autowired
        private LambdaClient lambdaClient;

        @Bean
        LambdaService<SdkBytes, String> lambdaService() {
            return LambdaServiceImpl.<SdkBytes, String>builder()
                    .lambdaClient(this.lambdaClient)
                    .recordMapper(LambdaServiceHelper::mapStringRecord)
                    .invokeResponseMapper(LambdaServiceHelper::extractResultPayload)
                    .build();
        }

        @Bean
        LambdaService<String, String> lambdaServiceLogExtractor() {
            return LambdaServiceImpl.<String, String>builder()
                    .lambdaClient(this.lambdaClient)
                    .recordMapper(LambdaServiceHelper::mapStringRecord)
                    .invokeResponseMapper(LambdaServiceHelper::extractLogResult)
                    .build();
        }

        @Bean
        LambdaFunctionSpecs lambdaFunctionSpecs() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .timeoutInSeconds(this.functionTimeout)
                    .build();
        }
    }

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class LambdaWithInvalidJarContext {

        @Bean
        LambdaFunctionSpecs lambdaFunctionSpecs() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir("some invalid code dir")
                    .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .build();
        }
    }

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class LambdaWithInvalidHandlerContext {

        @Value("${lambda.code.uri}")
        private String lambdaCodeDir;

        @Value("${test.lambda.timeout:10}")
        private int functionTimeout;

        @Bean
        LambdaFunctionSpecs localLambdaFunction() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                    .handlerClass(TestData.TEST_INVALID_LAMBDA_HANDLER_CLASS_NAME)
                    .timeoutInSeconds(this.functionTimeout)
                    .build();
        }
    }

    @Configuration
    @ServiceDependencies(Service.LAMBDA)
    public static class TwoLambdaFunctionsContext {

        @Value("${lambda.code.uri}")
        private String lambdaCodeDir;

        @Value("${test.lambda.timeout:10}")
        private int functionTimeout;

        @Autowired
        private LambdaClient lambdaClient;

        @Bean
        LambdaFunctionSpecs firstLambdaFunction() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .timeoutInSeconds(this.functionTimeout)
                    .build();
        }

        @Bean
        LambdaFunctionSpecs secondLambdaFunction() {
            return LambdaFunctionSpecs
                    .builder()
                    .lambdaCodeDir(this.lambdaCodeDir)
                    .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME_2)
                    .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                    .timeoutInSeconds(this.functionTimeout)
                    .build();
        }

        @Bean
        LambdaService<SdkBytes, String> lambdaService() {
            return LambdaServiceImpl.<SdkBytes, String>builder()
                    .lambdaClient(this.lambdaClient)
                    .recordMapper(LambdaServiceHelper::mapStringRecord)
                    .invokeResponseMapper(LambdaServiceHelper::extractResultPayload)
                    .build();
        }
    }
}