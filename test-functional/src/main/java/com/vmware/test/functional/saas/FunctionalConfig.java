/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.vmware.test.functional.saas.process.DpaTestApp;
import com.vmware.test.functional.saas.process.DpaTestAppDebug;
import com.vmware.test.functional.saas.process.TestProcessGenericRunner;
import com.vmware.test.functional.saas.process.TestProcessLifecycle;

import static com.vmware.test.functional.saas.TestScope.SCOPE_TEST_CLASS;

/**
 * Defines generic beans used in spring.factories.
 */
public class FunctionalConfig {

    @Bean
    @ConditionalOnMissingBean
    TestProcessGenericRunner testProcessGenericRunner(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            @Autowired(required = false) final List<TestProcessLifecycle> testProcessList) {
        return new TestProcessGenericRunner(functionalTestExecutionSettings, testProcessList);
    }

    @Bean
    FunctionalTestExecutionSettings functionalTestExecutionSettings() {
        return new FunctionalTestExecutionSettings();
    }

    @Bean
    DpaTestApp defaultDpaTestApp() {
        return new DpaTestApp();
    }

    @Bean
    DpaTestAppDebug defaultDpaTestAppDebug() {
        return new DpaTestAppDebug();
    }

    @Bean
    BeanFactoryPostProcessor testScopeBeanFactoryPostProcessor() {
        return this::registerScopes;
    }

    private void registerScopes(final ConfigurableListableBeanFactory beanFactory) {
        beanFactory.registerScope(SCOPE_TEST_CLASS, new TestScope());
    }
}
