/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.lambda.sam.process;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.LocalServiceEndpoint;
import com.vmware.test.functional.saas.PortSupplier;
import com.vmware.test.functional.saas.aws.local.lambda.constants.TestConstants;
import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link SamProcessControl}.
 */
public class SamProcessControlInvalidStartTest extends AbstractTestNGSpringContextTests {

    @Value("${lambda.code.uri}")
    private String lambdaCodeUri;

    private SamProcessControl samProcessControl;

    // should fail while trying to check that sam process is listening on port 0
    @Test(expectedExceptions = RuntimeException.class)
    public void startSAMOnInvalidPort() {
        this.samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new LocalServiceEndpoint(0, LocalServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName(TestConstants.TEST_LAMBDA_FUNCTION_NAME)
                        .handlerClass(TestConstants.TEST_LAMBDA_HANDLER_CLASS_NAME)
                        .lambdaCodeDir(this.lambdaCodeUri)
                        .build()))
                .build();
        this.samProcessControl.start();
        assertThat("SamProcessControl: has not started", !this.samProcessControl.isRunning());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void startSAMTwiceOnTheSamePort() {
        final int port = new PortSupplier().getAsInt();
        this.samProcessControl = SamProcessControl.builder()
                .lambdaEndpoint(new LocalServiceEndpoint(port, LocalServiceEndpoint.DEFAULT_SCHEME))
                .lambdaFunctionSpecs(Collections.singletonList(LambdaFunctionSpecs.builder()
                        .functionName("test")
                        .handlerClass("handler")
                        .lambdaCodeDir("/some/code/dir")
                        .build()))
                .build();
        this.samProcessControl.start();
        assertThat("SamProcessControl: has not started", this.samProcessControl.isRunning());
        this.samProcessControl.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopSamProcess() {
        this.samProcessControl.stop();
    }
}
