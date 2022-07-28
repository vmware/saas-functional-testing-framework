/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import static com.vmware.test.functional.saas.TestScope.SCOPE_TEST_CLASS;

/**
 * Execution Listener for the TestClass scope.
 */
@Order
public class TestClassScopeExecutionListener implements TestExecutionListener {

    @Override
    public void afterTestClass(final TestContext testContext) {
        removeTestScopeBeansFromAllBeanFactory(((ConfigurableApplicationContext)testContext.getApplicationContext()).getBeanFactory(), SCOPE_TEST_CLASS);
    }

    private void removeTestScopeBeansFromAllBeanFactory(final ConfigurableListableBeanFactory beanFactory, final String scopeName) {
        if (beanFactory == null) {
            return;
        }
        final TestScope scope = (TestScope)beanFactory.getRegisteredScope(scopeName);

        if (scope == null) {
            throw new IllegalStateException(String.format("Scope %s not found.", scopeName));
        }
        scope.teardownAll();
        scope.removeAll();
        removeTestScopeBeansFromAllBeanFactory((ConfigurableListableBeanFactory)beanFactory.getParentBeanFactory(), scopeName);
    }
}
