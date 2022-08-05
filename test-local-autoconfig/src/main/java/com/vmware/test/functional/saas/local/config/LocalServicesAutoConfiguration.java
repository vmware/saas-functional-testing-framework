/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.testcontainers.containers.GenericContainer;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ConditionalOnService;
import com.vmware.test.functional.saas.local.es.ElasticsearchResourceCreator;
import com.vmware.test.functional.saas.local.es.JestClientFactory;
import com.vmware.test.functional.saas.local.pg.PostgresDatabaseCreator;
import com.vmware.test.functional.saas.local.pg.PostgresDatabaseInitializer;
import com.vmware.test.functional.saas.local.pg.PostgresDbSettings;
import com.vmware.test.functional.saas.local.trino.TrinoCatalogCreator;
import com.vmware.test.functional.saas.local.redis.RedisTemplateFactory;
import com.vmware.test.functional.saas.local.trino.TrinoCatalogUtils;
import com.vmware.test.functional.saas.es.ElasticsearchResourceAwaitingInitializer;
import com.vmware.test.functional.saas.trino.TrinoCatalogAwaitingInitializer;
import com.vmware.test.functional.saas.trino.TrinoCatalogSettings;
import com.vmware.test.functional.saas.trino.TrinoCatalogSpecs;

import io.searchbox.client.JestClient;
import io.trino.jdbc.TrinoDriver;

/**
 * Local Services AutoConfiguration. To be used by {@code FunctionalTest}.
 */
@Configuration
@Import(DockerContainersConfiguration.class)
@AutoConfigureOrder(Integer.MAX_VALUE)
@PropertySource("classpath:aws-local.properties")
public class LocalServicesAutoConfiguration {

    @Bean
    @ConditionalOnService(Service.ELASTICSEARCH)
    @Lazy
    JestClientFactory jestClient(@Lazy final ServiceEndpoint elasticsearchEndpoint,
            final ConfigurableEnvironment env) {
        return new JestClientFactory(elasticsearchEndpoint, env);
    }

    @Bean
    @ConditionalOnService(value = Service.ELASTICSEARCH, search = SearchStrategy.ALL, conditionalOnMissingBean = true)
    @Lazy
    ElasticsearchResourceCreator elasticsearchResourceCreator(@Lazy final JestClient jestClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new ElasticsearchResourceCreator(jestClient, functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.ELASTICSEARCH, search = SearchStrategy.ALL)
    @Lazy
    ElasticsearchResourceAwaitingInitializer elasticsearchResourceAwaitingInitializer(@Lazy final JestClient jestClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new ElasticsearchResourceAwaitingInitializer(jestClient,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.POSTGRES, search = SearchStrategy.ALL)
    @Lazy
    PostgresDatabaseCreator<PostgresDbSettings> postgresDatabaseCreator(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final ServiceEndpoint postgresEndpoint) {
        return new PostgresDatabaseCreator<>(functionalTestExecutionSettings,
                postgresEndpoint, PostgresDbSettings.class);
    }

    @Bean
    @ConditionalOnService(value = Service.POSTGRES)
    @Lazy
    PostgresDatabaseInitializer postgresDatabaseInitializer(@Lazy final GenericContainer<?> postgresContainer) {
        return new PostgresDatabaseInitializer(postgresContainer.getContainerId());
    }

    @Bean
    @ConditionalOnService(value = Service.TRINO)
    @Lazy
    JdbcTemplate trinoJdbcTemplate(final ServiceEndpoint trinoEndpoint) {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(TrinoDriver.class);
        dataSource.setUrl("jdbc:trino://localhost:" + trinoEndpoint.getPort());
        dataSource.setUsername("test");

        return new JdbcTemplate(dataSource);
    }

    /**
     * Trino Elasticsearch catalog spec.
     *
     * @param elasticsearchEndpoint endpoint spec for the elasticsearch service
     * @return {@link TrinoCatalogSpecs}
     */
    @Bean
    @ConditionalOnService({ Service.TRINO, Service.ELASTICSEARCH })
    @Lazy
    public TrinoCatalogSpecs trinoCatalogSpecs(@Lazy final ServiceEndpoint elasticsearchEndpoint) {
        return TrinoCatalogSpecs.builder()
                .catalog(TrinoCatalogSettings.builder()
                        .name("elasticsearch")
                        .properties(TrinoCatalogUtils.elasticsearchCatalog(elasticsearchEndpoint))
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnService(Service.TRINO)
    @Lazy
    TrinoCatalogCreator trinoCatalogCreator(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            @Autowired(required = false) @Lazy final List<TrinoCatalogSpecs> catalogSpecs,
            @Autowired @Lazy final ServiceEndpoint trinoEndpoint) {
        return new TrinoCatalogCreator(functionalTestExecutionSettings, catalogSpecs, trinoEndpoint);
    }

    @Bean
    @ConditionalOnService(value = Service.TRINO, search = SearchStrategy.ALL)
    @Lazy
    TrinoCatalogAwaitingInitializer trinoCatalogAwaitingInitializer(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final JdbcTemplate trinoJdbcTemplate) {
        return new TrinoCatalogAwaitingInitializer(functionalTestExecutionSettings, trinoJdbcTemplate);
    }

    @Bean
    @ConditionalOnService(Service.REDIS)
    @Lazy
    RedisTemplateFactory redisTemplateFactory(@Lazy final ServiceEndpoint redisEndpoint) {
        return new RedisTemplateFactory(redisEndpoint);
    }
}
