/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.scope.context;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.aw.dpa.test.scope.TestScopedBean;

import static com.aw.dpa.test.TestScope.SCOPE_TEST_CLASS;

@Configuration
public class TestClassScopeParentContext {

    @Bean
    @Scope(scopeName = SCOPE_TEST_CLASS, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TestScopedBean testClassScopedBean() {
        return new TestScopedBean(UUID.randomUUID().toString());
    }
}
