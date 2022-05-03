/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.scope.context;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.vmware.test.functional.saas.scope.TestScopedBean;

import static com.vmware.test.functional.saas.TestScope.SCOPE_TEST_CLASS;

@Configuration
public class TestClassScopeParentContext {

    @Bean
    @Scope(scopeName = SCOPE_TEST_CLASS, proxyMode = ScopedProxyMode.TARGET_CLASS)
    TestScopedBean testClassScopedBean() {
        return new TestScopedBean(UUID.randomUUID().toString());
    }
}
