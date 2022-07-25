/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.service;

import software.amazon.awssdk.services.kms.KmsClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.SharedConfig;
import com.vmware.test.functional.saas.local.utils.ServiceDependenciesHealthHelper;

import static org.hamcrest.MatcherAssert.*;

/**
 * {@link ServiceDependencies} Context Hierarchy tests.
 * Test verifies service dependencies specified in context hierarchy(listed in parent and child contexts) are correctly loaded
 * and all service dependencies are started in docker containers.
 * Services listed both in parent and child contexts should be started ONLY once.
 * Current test verifies Redis container is started only once in the parent context and is not started again when child context
 * is configured.
 * AutoConfiguration is specified in test/resources/META-INF/spring.factories which wires
 * SharedConfig.java to two different test contexts annotated with {@link ServiceDependencies}
 * Extending AbstractFunctionalTests configures {@link SharedConfig} with autoconfigured test contexts to be loaded
 * as parent context for this test.
 */
@ContextHierarchy(@ContextConfiguration(classes = ServiceDependenciesContextHierarchyTest.StartRedisChildContext.class))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ServiceDependenciesContextHierarchyTest extends AbstractFunctionalTests {

    /**
     * Child Context which specifies Redis as a Service Dependency.
     */
    @Configuration
    @ServiceDependencies(Service.REDIS)
    public static class StartRedisChildContext {

    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private KmsClient awskmsClient;

    @Test
    public void listServiceDependenciesInContextHierarchy() {
        assertThat("kms is running ok", ServiceDependenciesHealthHelper.isKmsHealthy(this.awskmsClient));
        assertThat("Redis service is running OK.", ServiceDependenciesHealthHelper.isRedisHealthy(this.redisTemplate));
    }
}
