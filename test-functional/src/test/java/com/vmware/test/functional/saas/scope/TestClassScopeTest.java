/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.scope;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.TestScope;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@link TestScope}.
 */
@Slf4j
@Test(groups = "parallelTestClasses")
public class TestClassScopeTest extends AbstractFunctionalTests {

    @Autowired
    private TestScopedBean testClassScopedBean;

    private String anOrgId;
    private String theSameOrgId;

    public void testThatWorksWithAnOrg() {
        this.anOrgId = this.testClassScopedBean.getOrgId();
        final String orgIdSecondTime = this.testClassScopedBean.getOrgId();
        log.info("Uses an org id: [{}]", this.anOrgId);
        assertThat("Test orgId is the same.", this.anOrgId, is(equalTo(orgIdSecondTime)));
        TestOrgState.getSingletonInstance().addOrgId(orgIdSecondTime);
    }

    public void testThatWorksWithTheSameOrg() {
        this.theSameOrgId = this.testClassScopedBean.getOrgId();
        assertThat("Test theSameOrgId is set.", this.theSameOrgId, is(notNullValue()));
    }

    @AfterClass(groups = "parallelTestClasses")
    public void validateBothTestsWorkWithTheSameOrgIds() {
        log.info("anOrgId: [{}], theSameOrgId [{}]", this.anOrgId, this.theSameOrgId);
        assertThat("Each test method works with the same orgId", this.anOrgId, is(equalTo(this.theSameOrgId)));
    }
}
