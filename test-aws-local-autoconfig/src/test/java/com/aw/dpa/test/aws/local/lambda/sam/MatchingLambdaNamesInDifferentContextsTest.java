/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda.sam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.aws.local.lambda.sam.process.SamProcessControl;
import com.aw.dpa.test.functional.aws.lambda.LambdaRequestContextFactory;

import static com.aw.dpa.test.aws.local.lambda.sam.SamLambdaRequestContextFactoryTestConfigurations.TEST_LAMBDA_NAME;
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
        try (SamLambdaRequestContext samLambdaRequestContext = (SamLambdaRequestContext)this.lambdaRequestContextFactory.newRequestContext(TEST_LAMBDA_NAME)) {
            assertThat(samLambdaRequestContext.functionName, is(TEST_LAMBDA_NAME));
            assertThat(samLambdaRequestContext.lambdaLogFile, is(this.samProcessControl.getLambdaLogFile()));
        }
    }

}
