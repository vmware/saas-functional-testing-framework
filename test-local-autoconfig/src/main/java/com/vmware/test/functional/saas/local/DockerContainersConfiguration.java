/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.local.trino.TrinoCatalogCreator;
import com.vmware.test.functional.saas.local.trino.TrinoContainerFactory;

import static com.vmware.test.functional.saas.local.CustomDockerContainer.DEFAULT_WAIT_STRATEGY_TIMEOUT;
import static com.vmware.test.functional.saas.local.CustomDockerContainer.createDockerContainer;

/**
 * Docker container configuration.
 * Define containers to be deployed and endpoint for respective services.
 */
@Configuration
@PropertySource("classpath:docker-container.properties")
@EnableConfigurationProperties(DockerConfig.class)
public class DockerContainersConfiguration {

    /*
     * Elasticsearch goes into read-only mode when you have less than 5% of free disk space.
     * Setting this to false allows us to disable that disk allocation decider.
     * See https://www.elastic.co/guide/en/elasticsearch/reference/6.8/disk-allocator.html
     */
    public static final String ELASTICSEARCH_CLUSTER_DISK_THRESHOLD_ENABLED = "cluster.routing.allocation.disk.threshold_enabled";


    @Bean
    GenericRunner genericRunner(final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new GenericRunner(functionalTestExecutionSettings);
    }

    /**
     * Redis container. To be used by auto config.
     *
     * @param redisEndpoint endpoint spec for the redis service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.REDIS)
    @Lazy
    public Startable redisContainer(@Lazy final ServiceEndpoint redisEndpoint) {
        return createDockerContainer(redisEndpoint,
                Wait.forListeningPort());
    }

    /**
     * Trino container factory. To be used by auto config.
     *
     * @param trinoEndpoint endpoint spec for the trino dpa service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.TRINO)
    @Lazy
    public TrinoContainerFactory trinoContainer(@Lazy final ServiceEndpoint trinoEndpoint,
          TrinoCatalogCreator trinoCatalogCreator) {
        return new TrinoContainerFactory(trinoEndpoint, trinoCatalogCreator, container -> { });
    }

    /**
     * Postgres container. To be used by auto config.
     *
     * @param postgresEndpoint endpoint spec for the postgres service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.POSTGRES)
    @Lazy
    public Startable postgresContainer(@Lazy final ServiceEndpoint postgresEndpoint) {
        final String logWaitRegex = "(.*)(database system is ready to accept connections)(.*)";

        return createDockerContainer(postgresEndpoint,
                new LogMessageWaitStrategy()
                        .withRegEx(logWaitRegex)
                        .withTimes(2)
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT));
    }

    /**
     * Elasticsearch container. To be used by auto config.
     *
     * @param elasticsearchEndpoint endpoint spec for the elasticsearch service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.ELASTICSEARCH)
    @Lazy
    public Startable elasticsearchContainer(@Lazy final ServiceEndpoint elasticsearchEndpoint) {
        return createDockerContainer(elasticsearchEndpoint,
                new HttpWaitStrategy()
                        .forPath("/_cat/health")
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT))
                .withEnv(Map.of(ELASTICSEARCH_CLUSTER_DISK_THRESHOLD_ENABLED, Boolean.FALSE.toString()));
    }
}
