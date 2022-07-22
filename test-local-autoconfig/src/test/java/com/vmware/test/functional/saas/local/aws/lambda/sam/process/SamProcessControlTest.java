/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link SamProcessControl}.
 */
public class SamProcessControlTest extends AbstractTestNGSpringContextTests {

    @Value("${lambda.code.uri}")
    private String lambdaCodeUri;

    @Test
    public void startSAMProcessControl() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void startSAMWithoutLambdaFunctionSpecs() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .build();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void startSAMWithoutLambdaEndpoint() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .build()))
                .build();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
    }

    @Test
    public void verifyTemplateFileIsCreated() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .lambdaCodeDir("/some/code/dir")
                        .functionName("TestLambda")
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .environmentSupplier(() -> Map.of("var1", "value1"))
                        .timeoutInSeconds(5)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("template.yaml file was not created.", new File(samProcessControl.getTemplateFile()).isFile());
        samProcessControl.stop();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void startLambdaFunctionControlWithoutFunctionName() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .build()))
                .build();
        samProcessControl.start();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void startLambdaFunctionControlWithoutCodeDir() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .build()))
                .build();
        samProcessControl.start();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void startLambdaFunctionControlWithoutHandler() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .build()))
                .build();
        samProcessControl.start();
    }

    @Test
    public void stopSAMProcessControl() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName("test")
                        .handlerClass("handler")
                        .lambdaCodeDir("/some/code/dir")
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test
    public void stopSAMProcessControlTwice() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName("test")
                        .handlerClass("handler")
                        .lambdaCodeDir("/some/code/dir")
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
        samProcessControl.stop();
        samProcessControl.stop();
        assertThat("Stopped", !samProcessControl.isRunning());
    }

    @Test
    public void startLambdaFunctionControlWithoutEnvironment() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("template.yaml file was not created.", new File(samProcessControl.getTemplateFile()).isFile());
        assertThat("Running", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test
    public void startLambdaFunctionControlWithoutRuntime() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("template.yaml file was not created.", new File(samProcessControl.getTemplateFile()).isFile());
        assertThat("Running", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test
    public void startLambdaFunctionControlWithoutTimeout() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("template.yaml file was not created.", new File(samProcessControl.getTemplateFile()).isFile());
        assertThat("Running", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test
    public void startLambdaFunctionControlWithoutMemorySize() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .runtime(TestConstants.TEST_LAMBDA_RUNTIME)
                        .timeoutInSeconds(5)
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("template.yaml file was not created.", new File(samProcessControl.getTemplateFile()).isFile());
        assertThat("Running", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startSAMProcessControlWithJava8Runtime() {
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .runtime(Runtime.JAVA8.toString())
                        .build()))
                .build();
        samProcessControl.start();
        assertThat("SamProcessControl: has not started", samProcessControl.isRunning());
        samProcessControl.stop();
    }

    @Test
    public void samProcessControlDoesNotStartWhenSamExecutableFails() {
        final String exceptionMessage = "Simulating sam executable not working";
        final SamProcessControl samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .build()))
                .build();
        samProcessControl.setExecutorSupplier(() -> {
            throw new RuntimeException(exceptionMessage);
        });
        try {
            samProcessControl.start();
        } catch (final RuntimeException e) {
            assertThat("SamProcessControl threw wrong exception", exceptionMessage.equals(e.getMessage()));
        }
        assertThat("SamProcessControl: has started", !samProcessControl.isRunning());
        samProcessControl.stop();
    }
}
