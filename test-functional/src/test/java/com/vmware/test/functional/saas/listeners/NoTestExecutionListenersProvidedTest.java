/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.listeners;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.FunctionalTestExecutionListener;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link FunctionalTestExecutionListener}.
 * Test verifies that if no test execution listeners mappings are provided, test configuration should not fail.
 */
@FunctionalTest
@ContextConfiguration(classes = NoTestExecutionListenersProvidedTest.TestContext.class)
@TestExecutionListeners(value = FunctionalTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class NoTestExecutionListenersProvidedTest extends AbstractTestNGSpringContextTests {

    @Configuration
    public static class TestContext {
        // no TestExecutionListenerMapping provided
    }

    @Test
    public void doNotFailIfNoListenersProvided() {
        // dummy test that will always pass. Purpose is to check configuration will be successfully configured and the test is run.
        assertThat("Always return true.", true);
    }
}
