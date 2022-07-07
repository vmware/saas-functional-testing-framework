/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.DockerContainersConfiguration;
import com.vmware.test.functional.saas.local.GenericRunner;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test for {@link DockerContainersConfiguration} and {@link GenericRunner}.
 * Test verifies the successful application context setup when a test
 * is configured with context where no {@link com.vmware.test.functional.saas.ServiceDependencies} are provided.
 */
@ContextConfiguration(classes = NoServiceDependenciesProvidedTest.TestContext.class)
@FunctionalTest
public class NoServiceDependenciesProvidedTest extends AbstractTestNGSpringContextTests {

    public static class TestContext {

    }

    @Autowired
    GenericRunner genericRunner;

    @Test
    public void noServiceDependenciesProvided() {
        assertThat("Generic Runner has not been configured - unexpected based on configuration.", this.genericRunner.isRunning());
    }
}
