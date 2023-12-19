/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.es.ElasticsearchResourceCreator;
import com.vmware.test.functional.saas.local.pg.PostgresDatabaseCreator;
import com.vmware.test.functional.saas.local.trino.TrinoCatalogCreator;
import com.vmware.test.functional.saas.es.ElasticsearchResourceAwaitingInitializer;

import static org.hamcrest.MatcherAssert.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

/**
 * This test verifies that if a service (i.e. Service.Trino) is NOT specified in a test context configuration,
 * then the required client needed for working with the service is NOT created.
 */
@ContextHierarchy(@ContextConfiguration(classes = LocalServicesAutoConfigurationConditionDoNotMatchTest.TestContext.class))
@FunctionalTest
public class LocalServicesAutoConfigurationConditionDoNotMatchTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApplicationContext context;

    @Configuration
    public static class TestContext {

    }

    @Test
    public void beanConditionsDoNotMatch() {
        // The following are not specified as service dependencies for this test...and should not be created
        assertThat("Elasticsearch client has been created - unexpected based on condition.",
              this.context.getBeansOfType(ElasticsearchClient.class).size() == 0);
        assertThat("ElasticsearchResourceCreator has been created - unexpected based on condition.",
                this.context.getBeansOfType(ElasticsearchResourceCreator.class).size() == 0);
        assertThat("ElasticsearchResourceAwaitingInitializer has been created - unexpected based on condition.", this.context.getBeansOfType(
                ElasticsearchResourceAwaitingInitializer.class).size() == 0);
        assertThat("PostgresDatabaseCreator has been created - unexpected based on condition.",
                this.context.getBeansOfType(PostgresDatabaseCreator.class).size() == 0);
        assertThat("TrinoCatalogCreator has been created - unexpected based on condition.", this.context.getBeansOfType(
                TrinoCatalogCreator.class).size() == 0);
        assertThat("RedisTemplate has been created - unexpected based on condition.", this.context.getBeansOfType(RedisTemplate.class).size() == 0);
    }
}
