/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.aws.local.es.ElasticsearchResourceCreator;
import com.vmware.test.functional.saas.aws.local.pg.PostgresDatabaseCreator;
import com.vmware.test.functional.saas.aws.local.presto.PrestoCatalogCreator;
import com.vmware.test.functional.saas.aws.es.ElasticsearchResourceAwaitingInitializer;

import io.searchbox.client.JestClient;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test verifies that if a service (i.e. Service.Presto) is specified in a test context configuration,
 * then the required client needed for working with the service is created.
 */
public class LocalServicesAutoConfigurationConditionMatchTest extends AbstractFullContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void beanConditionsMatch() {
        // The following are specified as service dependencies for this test...and should be created
        assertThat("Elasticsearch client has NOT been created - unexpected.", this.context.getBeansOfType(JestClient.class).size() == 1);
        assertThat("ElasticsearchResourceCreator has NOT been created - unexpected.",
                this.context.getBeansOfType(ElasticsearchResourceCreator.class).size() == 1);
        assertThat("ElasticsearchResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                ElasticsearchResourceAwaitingInitializer.class).size() == 1);
        assertThat("PostgresDatabaseCreator has NOT been created - unexpected",
                this.context.getBeansOfType(PostgresDatabaseCreator.class).size() == 2);
        assertThat("PrestoCatalogCreator has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                PrestoCatalogCreator.class).size() == 1);
        assertThat("RedisTemplate has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                RedisTemplate.class).size() == 1);
    }
}