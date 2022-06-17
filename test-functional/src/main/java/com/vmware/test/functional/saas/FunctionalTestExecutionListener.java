/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Test Execution Listener used by Functional Tests.
 */
@Component
public class FunctionalTestExecutionListener extends AbstractTestExecutionListener {

    @Autowired(required = false)
    TestExecutionListenerMapping testExecutionListenerMapping;

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).beforeTestClass(testContext);
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).afterTestClass(testContext);
    }

    @Override
    public void afterTestExecution(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).afterTestExecution(testContext);
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).afterTestMethod(testContext);
    }

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).prepareTestInstance(testContext);
    }

    @Override
    public void beforeTestExecution(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).beforeTestExecution(testContext);
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        getTestExecutionListener(testContext).beforeTestMethod(testContext);
    }

    private TestExecutionListener getTestExecutionListener(final TestContext testContext) {
        // if no test execution listener mapping is provided, create empty mapping instance and do not fail
        if (this.testExecutionListenerMapping == null) {
            return TestExecutionListenerMapping.builder()
                    .testExecutionListenerByTestClass(Collections.emptyMap())
                    .build()
                    .getTestExecutionListener(testContext.getTestClass());
        }
        return this.testExecutionListenerMapping.getTestExecutionListener(testContext.getTestClass());
    }
}
