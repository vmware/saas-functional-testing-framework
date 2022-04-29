/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.aw.dpa.test.process.DpaTestApp;
import com.aw.dpa.test.process.DpaTestAppDebug;
import com.aw.dpa.test.process.TestProcessGenericRunner;
import com.aw.dpa.test.process.TestProcessLifecycle;

import static com.aw.dpa.test.TestScope.SCOPE_TEST_CLASS;

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
