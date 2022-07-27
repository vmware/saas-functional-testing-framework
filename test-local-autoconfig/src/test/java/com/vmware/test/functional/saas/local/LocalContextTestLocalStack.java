/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.AliasListEntry;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.local.aws.config.DockerContainersConfiguration;
import com.vmware.test.functional.saas.local.aws.kms.KmsHealthHelper;
import com.vmware.test.functional.saas.local.utils.ServiceDependenciesHealthHelper;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@link DockerContainersConfiguration}.
 */
@ContextConfiguration(classes = LocalContextTestLocalStack.TestContext.class)
@FunctionalTest
@TestPropertySource(properties = "services.provided.by.localstack=DYNAMO_DB,secretsmanager")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Test(groups = "explicitOrderTestClass")
public class LocalContextTestLocalStack extends AbstractTestNGSpringContextTests {

    @ServiceDependencies({Service.REDIS, Service.KMS, Service.DYNAMO_DB})
    public static class TestContext {

    }

    @Autowired
    private KmsClient kmsClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private LocalStackContainer localStackContainer;

    @Test
    public void localKMSDockerContainerProvided() {
        final String testAliasName = "alias/testing";
        await().until(() -> KmsHealthHelper.checkHealth(this.kmsClient, testAliasName));

        final List<AliasListEntry> aliases = this.kmsClient.listAliases().aliases();

        final Optional<AliasListEntry> foundAlias = aliases.stream()
                .filter(a -> a.aliasName().equals(testAliasName))
                .findAny();
        assertThat(foundAlias.isPresent(), is(true));
    }

    @Test
    public void localRedisAutoconfiguration() {
        assertThat("Redis service is running OK.", ServiceDependenciesHealthHelper
                .isRedisHealthy(this.redisTemplate));    }

    @Test
    public void localStackProvidedServices() {
        final String services = this.localStackContainer.getEnv().stream().filter(s -> s.startsWith("SERVICES=")).findFirst().orElse(null);
        assertThat(services, notNullValue());
        assertThat(services, is("SERVICES=dynamodb"));
    }
}
