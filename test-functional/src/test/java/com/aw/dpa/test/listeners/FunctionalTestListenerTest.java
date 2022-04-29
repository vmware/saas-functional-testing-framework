/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.listeners;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.FunctionalTestExecutionListener;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link FunctionalTestExecutionListener}.
 */
@FunctionalTest
@ContextConfiguration(classes = FunctionalTestListenerTestContext.class)
@TestExecutionListeners(value = FunctionalTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Slf4j
public class FunctionalTestListenerTest extends AbstractTestNGSpringContextTests {

    public static FunctionalTestListenerTestContext.LocalTestDataProcessor testEventProcessor;

    @Test
    public void testEvents() {
        log.info("Assert FunctionalTestListenerTestContext Before Test Class");
        assertThat("Before Test Class is invoked", FunctionalTestListenerTest.testEventProcessor.beforeTestClassInvoked);
        log.info("Assert FunctionalTestListenerTestContext Prepare Test Instance");
        assertThat("Prepare test instance is invoked", FunctionalTestListenerTest.testEventProcessor.prepareTestInstanceInvoked);
        log.info("Assert FunctionalTestListenerTestContext Before Test Execution");
        assertThat("Before Test execution is invoked", FunctionalTestListenerTest.testEventProcessor.beforeTestExecutionInvoked);
        log.info("Assert FunctionalTestListenerTestContext Before Test Method");
        assertThat("Before Test Method is invoked", FunctionalTestListenerTest.testEventProcessor.beforeTestMethodInvoked);
    }

    @AfterSuite(alwaysRun = true)
    public void assertAfterPhases() {
        log.info("Assert FunctionalTestListenerTestContext After Test Class");
        assertThat("After Test Class is invoked", FunctionalTestListenerTest.testEventProcessor.afterTestClassInvoked);

        log.info("Assert FunctionalTestListenerTestContext After Test Method");
        assertThat("After Test Method is invoked", FunctionalTestListenerTest.testEventProcessor.afterTestMethodInvoked);

        log.info("Assert FunctionalTestListenerTestContext After Test Execution");
        assertThat("After Test Execution is invoked", FunctionalTestListenerTest.testEventProcessor.afterTestExecutionInvoked);
    }
}
