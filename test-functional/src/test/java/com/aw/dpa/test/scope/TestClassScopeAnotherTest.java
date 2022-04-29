/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.scope;

import org.hamcrest.MatcherAssert;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Test(groups = "parallelTestClasses")
public class TestClassScopeAnotherTest extends AbstractFunctionalTests {

    @Autowired
    private TestScopedBean testClassScopedBean;

    public void testThatWorksWithAnotherOrg() {
        log.info("Uses an org id: [{}]", this.testClassScopedBean.getOrgId());
        MatcherAssert.assertThat("New orgId has been added to orgIds",
                TestOrgState.getSingletonInstance().addOrgId(this.testClassScopedBean.getOrgId()));
    }

    @AfterSuite(groups = "parallelTestClasses")
    public void validateBothTestsWorkWithDifferentOrgIds() {
        log.info("available orgs after suite: [{}]", TestOrgState.getSingletonInstance());
        // the second orgId is generated in other TestClassScopeTest
        assertThat("Each test class works with a different org", TestOrgState.getSingletonInstance().getOrgIdsSize(), is(equalTo(2)));
        assertThat("Cleanup after each class", TestOrgState.getSingletonInstance().allClear());
        // clean TestOrgState
        TestOrgState.getSingletonInstance().clearAll();
    }
}
