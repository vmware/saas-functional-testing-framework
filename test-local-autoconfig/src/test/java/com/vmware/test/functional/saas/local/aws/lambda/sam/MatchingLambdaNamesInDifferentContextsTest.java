/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.lambda.sam.process.SamProcessControl;
import com.vmware.test.functional.saas.aws.lambda.LambdaRequestContextFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@FunctionalTest
@ContextConfiguration(classes = SamLambdaRequestContextFactoryTestConfigurations.TestContext.class)
@TestPropertySource(properties = "sam.lambda.context.initialization.timeout.seconds=1")
public class MatchingLambdaNamesInDifferentContextsTest extends AbstractTestNGSpringContextTests {

    @Autowired
    LambdaRequestContextFactory lambdaRequestContextFactory;

    @Autowired
    SamProcessControl samProcessControl;

    private ConfigurableApplicationContext otherApplicationContext;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Create a different application context which has LambdaFunctionSpecs for a lambda with the same name.
        this.otherApplicationContext = new AnnotationConfigApplicationContext(
                SamAutoConfiguration.class,
                SamLambdaRequestContextFactoryTestConfigurations.OtherTestContext.class);
        this.otherApplicationContext.registerShutdownHook();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        this.otherApplicationContext.close();
    }

    @Test
    public void verifyOtherTestContextDoesntAffectCurrentTestContext() {
        try (SamLambdaRequestContext samLambdaRequestContext = (SamLambdaRequestContext)this.lambdaRequestContextFactory.newRequestContext(
              SamLambdaRequestContextFactoryTestConfigurations.TEST_LAMBDA_NAME)) {
            assertThat(samLambdaRequestContext.functionName, Matchers.is(
                  SamLambdaRequestContextFactoryTestConfigurations.TEST_LAMBDA_NAME));
            assertThat(samLambdaRequestContext.lambdaLogFile, is(this.samProcessControl.getLambdaLogFile()));
        }
    }

}
