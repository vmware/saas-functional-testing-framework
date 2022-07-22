/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.TestScope;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test for {@link TestScope}.
 */
@ContextConfiguration(classes = TestClassScopeWithHierarchyTest.TestContext.class)
@Slf4j
public class TestClassScopeWithHierarchyTest extends AbstractFunctionalTests {

    @Autowired
    private TestScopedBean testClassScopedBean;

    @Configuration
    public static class TestContext {

    }

    @Test(groups = "testScopeHierarchyClasses")
    public void test() {
        log.info("Uses an org id: [{}]", this.testClassScopedBean.getOrgId());
        assertThat("New orgId has been added to orgIds",
                TestOrgState.getSingletonInstance().addOrgId(this.testClassScopedBean.getOrgId()));
    }
}
