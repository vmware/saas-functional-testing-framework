/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.aw.dpa.test.AbstractFunctionalTests;
import com.aw.dpa.test.TestScope;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@link TestScope}.
 */
@ContextConfiguration(classes = TestClassScopeWithHierarchyTest.TestContext.class)
@Slf4j
public class TestClassScopeAnotherWithHierarchyTest extends AbstractFunctionalTests {

    @Autowired
    private TestScopedBean testClassScopedBean;

    @Configuration
    public static class TestContext {

    }

    @Test(groups = "testScopeHierarchyClasses")
    public void testThatWorksWithAnotherOrg() {
        log.info("Uses an org id: [{}]", this.testClassScopedBean.getOrgId());
        assertThat("New orgId has been added to orgIds",
                TestOrgState.getSingletonInstance().addOrgId(this.testClassScopedBean.getOrgId()));
    }

    @AfterSuite(groups = "testScopeHierarchyClasses")
    public void validateBothTestsWorkWithDifferentOrgIds() {
        log.info("available orgs after suite: [{}]", TestOrgState.getSingletonInstance());
        // the second orgId is generated in other TestClassScopeWithHierarchyTest
        assertThat("Each test class works with a different org", TestOrgState.getSingletonInstance().getOrgIdsSize(), is(equalTo(2)));
        assertThat("Cleanup after each class", TestOrgState.getSingletonInstance().allClear());
        // clear TestOrgState
        TestOrgState.getSingletonInstance().clearAll();
    }
}
