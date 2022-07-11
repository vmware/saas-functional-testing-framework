/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.lambda.AbstractFullContextTest;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test for validating lambda function started is reused in child contexts and is not started again.
 */
@ContextHierarchy({
        @ContextConfiguration(classes = SamProcessSingleBeanInstanceTest.ChildContext.class)
})
public class SamProcessSingleBeanInstanceTest extends AbstractFullContextTest {

    @Configuration
    public static class ChildContext {

    }

    @Autowired
    private SamProcessControl samProcess;

    @Test
    public void testProcessesStarted() {
        assertThat("Running", this.samProcess.isRunning());
    }

}
