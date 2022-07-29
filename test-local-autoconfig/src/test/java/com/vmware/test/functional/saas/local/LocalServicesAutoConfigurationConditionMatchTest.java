/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.AbstractFullContextTest;
import com.vmware.test.functional.saas.local.es.ElasticsearchResourceCreator;
import com.vmware.test.functional.saas.local.pg.PostgresDatabaseCreator;
import com.vmware.test.functional.saas.local.trino.TrinoCatalogCreator;
import com.vmware.test.functional.saas.es.ElasticsearchResourceAwaitingInitializer;

import io.searchbox.client.JestClient;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test verifies that if a service (i.e. Service.Trino) is specified in a test context configuration,
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
        assertThat("TrinoCatalogCreator has NOT been created - unexpected based on condition.",
              this.context.getBeansOfType(
                TrinoCatalogCreator.class).size() == 1);
        assertThat("RedisTemplate has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                RedisTemplate.class).size() == 1);
    }
}
