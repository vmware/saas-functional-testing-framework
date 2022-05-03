/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.service;

import software.amazon.awssdk.services.kms.KmsClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.aws.local.context.TestContext;
import com.vmware.test.functional.saas.aws.local.utils.ServiceDependenciesHealthHelper;

import static org.hamcrest.MatcherAssert.*;

/**
 * Service dependencies Context tests.
 * Test verifies service dependencies specified in two different configurations are correctly loaded
 * and all service dependencies are started in docker containers.
 * {@link com.vmware.test.functional.saas.aws.local.service.ServiceDependencies} annotation is read for both test configurations and listed
 * service dependencies are started in docker containers.
 * AutoConfiguration is specified in test/resources/META-INF/spring.factories which wires
 * SharedConfig.java to a test configuration annotated with {@link com.vmware.test.functional.saas.aws.local.service.ServiceDependencies}
 * by extending AbstractFunctionalTests.
 * and another test configuration is specified explicitly by using @ContextHierarchy(@ContextConfiguration()).
 */
@ContextHierarchy(@ContextConfiguration(classes = TestContext.StartRedisServiceContext.class))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ServiceDependenciesContextTest extends AbstractFunctionalTests {

    @Autowired
    private KmsClient awskmsClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void listServiceDependenciesInTwoContexts() {
        assertThat("kms is running ok", ServiceDependenciesHealthHelper.isKmsHealthy(this.awskmsClient));
        assertThat("Redis service is running OK.", ServiceDependenciesHealthHelper.isRedisHealthy(this.redisTemplate));
    }

}
