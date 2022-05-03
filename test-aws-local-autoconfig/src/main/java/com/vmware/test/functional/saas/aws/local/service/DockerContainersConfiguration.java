/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.MountableFile;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.LocalServiceEndpoint;
import com.vmware.test.functional.saas.aws.local.AwsSettings;
import com.vmware.test.functional.saas.aws.local.constants.DockerConfig;
import com.vmware.test.functional.saas.aws.local.constants.DockerContainerConstants;
import com.vmware.test.functional.saas.aws.local.presto.PrestoContainerFactory;

import static com.vmware.test.functional.saas.aws.local.service.CustomDockerContainer.DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT;
import static com.vmware.test.functional.saas.aws.local.service.CustomDockerContainer.DEFAULT_WAIT_STRATEGY_TIMEOUT;
import static com.vmware.test.functional.saas.aws.local.service.CustomDockerContainer.createDockerContainer;

/**
 * Docker container configuration.
 * Define containers to be deployed and endpoint for respective services.
 */
@Configuration
@PropertySource("classpath:docker-container.properties")
@EnableConfigurationProperties(DockerConfig.class)
public class DockerContainersConfiguration {

    @Autowired
    private AwsSettings awsSettings;

    @Value("seed.yaml")
    private ClassPathResource seedFile;

    @Bean
    GenericRunner genericRunner(final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new GenericRunner(functionalTestExecutionSettings);
    }

    /**
     * Kinesis container. To be used by auto config.
     *
     * @param kinesisEndpoint endpoint spec for kinesis service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.KinesisContainerCondition.class)
    @Lazy
    public Startable kinesisContainer(@Lazy final LocalServiceEndpoint kinesisEndpoint) {
        return createDockerContainer(kinesisEndpoint,
                Wait.forListeningPort())
                        .withCommand(DockerContainerConstants.Command.SSL, Boolean.TRUE.toString());
    }

    /**
     * DynamoDb container. To be used by auto config.
     *
     * @param dynamoDbEndpoint endpoint spec for dynamodb service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.DynamodbContainerCondition.class)
    @Lazy
    public Startable dynamodbContainer(@Lazy final LocalServiceEndpoint dynamoDbEndpoint) {
        return createDockerContainer(dynamoDbEndpoint,
                Wait.forListeningPort());
    }

    /**
     * KMS container. To be used by auto config.
     *
     * @param kmsEndpoint endpoint spec for kms service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.KmsContainerCondition.class)
    @Lazy
    public Startable kmsContainer(@Lazy final LocalServiceEndpoint kmsEndpoint) {
        final String logWaitRegex = "(.*)(Local KMS started on)(.*)";

        return createDockerContainer(kmsEndpoint,
                new LogMessageWaitStrategy()
                        .withRegEx(logWaitRegex)
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT))
                        .withEnv(DockerContainerConstants.Environment.KMS_ACCOUNT_ID, this.awsSettings.getTestAccountId())
                        .withEnv(DockerContainerConstants.Environment.KMS_REGION, this.awsSettings.getTestDefaultRegion())
                        .withCopyFileToContainer(MountableFile.forClasspathResource(this.seedFile.getPath()), "/init/seed.yaml")
                        .withExposedPorts(kmsEndpoint.getContainerConfig().getPort());
    }

    /**
     * Redis container. To be used by auto config.
     *
     * @param redisEndpoint endpoint spec for the redis service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.RedisContainerCondition.class)
    @Lazy
    public Startable redisContainer(@Lazy final LocalServiceEndpoint redisEndpoint) {
        return createDockerContainer(redisEndpoint,
                Wait.forListeningPort());
    }

    /**
     * Redshift container. To be used by auto config.
     *
     * @param redshiftEndpoint endpoint spec for the redis service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.RedshiftContainerCondition.class)
    @Lazy
    public Startable redshiftContainer(@Lazy final LocalServiceEndpoint redshiftEndpoint) {
        final String logWaitRegex = "(.*)(database system is ready to accept connections)(.*)";

        // For Redshift a simple postgres container is started and used
        return createDockerContainer(redshiftEndpoint,
                new LogMessageWaitStrategy()
                        .withRegEx(logWaitRegex)
                        .withTimes(2)
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT));
    }

    /**
     * Presto container factory. To be used by auto config.
     *
     * @param prestoEndpoint endpoint spec for the presto dpa service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.PrestoContainerCondition.class)
    @Lazy
    public PrestoContainerFactory prestoContainer(@Lazy final LocalServiceEndpoint prestoEndpoint) {
        return new PrestoContainerFactory(prestoEndpoint, container -> { });
    }

    /**
     * Postgres container. To be used by auto config.
     *
     * @param postgresEndpoint endpoint spec for the postgres service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(ContainerCondition.PostgresContainerCondition.class)
    @Lazy
    public Startable postgresContainer(@Lazy final LocalServiceEndpoint postgresEndpoint) {
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
    @Conditional(ContainerCondition.ElasticsearchContainerCondition.class)
    @Lazy
    public Startable elasticsearchContainer(@Lazy final LocalServiceEndpoint elasticsearchEndpoint) {
        return createDockerContainer(elasticsearchEndpoint,
                new HttpWaitStrategy()
                        .forPath("/_cat/health")
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT))
                .withEnv(Map.of(DockerContainerConstants.Environment.ELASTICSEARCH_CLUSTER_DISK_THRESHOLD_ENABLED, Boolean.FALSE.toString()));
    }

    @Bean
    @Conditional(ContainerCondition.LocalStackContainerCondition.class)
    LocalStackFactory localStackContainer(@Autowired(required = false)
            final List<LocalStackContainer.Service> localstackServices, final ConfigurableListableBeanFactory listableBeanFactory,
            final LocalServiceEndpoint localStackEndpoint) {
        return new LocalStackFactory(listableBeanFactory,
                localStackEndpoint,
                localstackServices,
                modifyLocalStackContainer(localStackEndpoint));
    }

    private Consumer<LocalStackContainer> modifyLocalStackContainer(final LocalServiceEndpoint localStackEndpoint) {
        return container -> {
            container.setNetwork(localStackEndpoint.getContainerConfig().getNetworkInfo().getNetwork());
            container.setEnv(List.of(DockerContainerConstants.Environment.LOCALSTACK_REGION + "=" + this.awsSettings.getTestDefaultRegion()));
            container.withCreateContainerCmdModifier(cmd -> cmd.withName(localStackEndpoint.getContainerConfig().getName()));
            container.withStartupCheckStrategy(new IsRunningStartupCheckStrategy().withTimeout(DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT));
        };
    }
}
