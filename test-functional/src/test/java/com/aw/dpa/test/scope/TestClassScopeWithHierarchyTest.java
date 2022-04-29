/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.aw.dpa.test.AbstractFunctionalTests;
import com.aw.dpa.test.TestScope;

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
