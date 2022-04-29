/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.testcontainers.containers.GenericContainer;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.es.ElasticsearchResourceCreator;
import com.aw.dpa.test.aws.local.es.JestClientFactory;
import com.aw.dpa.test.aws.local.pg.PostgresDatabaseCreator;
import com.aw.dpa.test.aws.local.pg.PostgresDatabaseInitializer;
import com.aw.dpa.test.aws.local.pg.PostgresDbSettings;
import com.aw.dpa.test.aws.local.presto.PrestoCatalogCreator;
import com.aw.dpa.test.aws.local.redis.RedisTemplateFactory;
import com.aw.dpa.test.aws.local.redshift.RedshiftDbSettings;
import com.aw.dpa.test.aws.local.service.ConditionalOnService;
import com.aw.dpa.test.aws.local.service.DockerContainersConfiguration;
import com.aw.dpa.test.aws.local.service.Service;
import com.aw.dpa.test.aws.local.utils.PrestoCatalogUtils;
import com.aw.dpa.test.functional.aws.es.ElasticsearchResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.presto.PrestoCatalogAwaitingInitializer;
import com.aw.dpa.test.functional.aws.presto.PrestoCatalogSettings;
import com.aw.dpa.test.functional.aws.presto.PrestoCatalogSpecs;

import io.searchbox.client.JestClient;
import io.trino.jdbc.TrinoDriver;

/**
 * Local Services AutoConfiguration. To be used by {@code FunctionalTest}.
 */
@Configuration
@Import(DockerContainersConfiguration.class)
@AutoConfigureOrder(Integer.MAX_VALUE)
@EnableConfigurationProperties(AwsSettings.class)
@PropertySource("classpath:aws-local.properties")
public class LocalServicesAutoConfiguration {

    @Bean
    @ConditionalOnService(Service.ELASTICSEARCH)
    @Lazy
    JestClientFactory jestClient(@Lazy final LocalServiceEndpoint elasticsearchEndpoint,
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
            final LocalServiceEndpoint postgresEndpoint) {
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
    @ConditionalOnService(value = Service.REDSHIFT, search = SearchStrategy.ALL)
    @Lazy
    PostgresDatabaseCreator<RedshiftDbSettings> redshiftDatabaseCreator(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final LocalServiceEndpoint redshiftEndpoint) {
        return new PostgresDatabaseCreator<>(functionalTestExecutionSettings,
                redshiftEndpoint, RedshiftDbSettings.class);
    }

    @Bean
    @ConditionalOnService(value = Service.PRESTO)
    @Lazy
    JdbcTemplate trinoJdbcTemplate(final LocalServiceEndpoint prestoEndpoint) {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(TrinoDriver.class);
        dataSource.setUrl("jdbc:trino://localhost:" + prestoEndpoint.getPort());
        dataSource.setUsername("test");

        return new JdbcTemplate(dataSource);
    }

    /**
     * Presto Elasticsearch catalog spec.
     *
     * @param elasticsearchEndpoint endpoint spec for the elasticsearch service
     * @return {@link PrestoCatalogSpecs}
     */
    @Bean
    @ConditionalOnService({ Service.PRESTO, Service.ELASTICSEARCH })
    @Lazy
    public PrestoCatalogSpecs prestoCatalogSpecs(@Lazy final LocalServiceEndpoint elasticsearchEndpoint) {
        return PrestoCatalogSpecs.builder()
                .catalog(PrestoCatalogSettings.builder()
                        .name("elasticsearch")
                        .properties(PrestoCatalogUtils.elasticsearchCatalog(elasticsearchEndpoint))
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnService(Service.PRESTO)
    @Lazy
    PrestoCatalogCreator prestoCatalogCreator(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            @Autowired(required = false) @Lazy final List<PrestoCatalogSpecs> catalogSpecs,
            @Autowired @Lazy final LocalServiceEndpoint prestoEndpoint) {
        return new PrestoCatalogCreator(functionalTestExecutionSettings, catalogSpecs, prestoEndpoint);
    }

    @Bean
    @ConditionalOnService(value = Service.PRESTO, search = SearchStrategy.ALL)
    @Lazy
    PrestoCatalogAwaitingInitializer prestoCatalogAwaitingInitializer(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final JdbcTemplate trinoJdbcTemplate) {
        return new PrestoCatalogAwaitingInitializer(functionalTestExecutionSettings, trinoJdbcTemplate);
    }

    @Bean
    @ConditionalOnService(Service.REDIS)
    @Lazy
    RedisTemplateFactory redisTemplateFactory(@Lazy final LocalServiceEndpoint redisEndpoint) {
        return new RedisTemplateFactory(redisEndpoint);
    }
}
