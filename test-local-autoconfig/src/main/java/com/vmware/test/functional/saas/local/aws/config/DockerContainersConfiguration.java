/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.MountableFile;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ConditionalOnService;
import com.vmware.test.functional.saas.local.ContainerCondition;
import com.vmware.test.functional.saas.local.ContainerNetworkManager;
import com.vmware.test.functional.saas.local.GenericRunner;
import com.vmware.test.functional.saas.local.config.DockerConfig;

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

    public static final String SSL_CLI_OPTION = "--ssl";

    public static final String KMS_REGION = "KMS_REGION";
    public static final String KMS_ACCOUNT_ID = "KMS_ACCOUNT_ID";

    @Autowired
    private AwsSettings awsSettings;

    @Autowired
    ContainerNetworkManager containerNetworkManager;

    @Value("seed.yaml")
    private ClassPathResource seedFile;

    @Bean
    GenericRunner genericRunnerAws(final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new GenericRunner(functionalTestExecutionSettings);
    }

    /**
     * Kinesis container. To be used by auto config.
     *
     * @param kinesisEndpoint endpoint spec for kinesis service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(KinesisContainerCondition.class)
    @Lazy
    public Startable kinesisContainer(@Lazy final ServiceEndpoint kinesisEndpoint) {
        return createDockerContainer(kinesisEndpoint,
                this.containerNetworkManager,
                Wait.forListeningPort())
                        .withCommand(SSL_CLI_OPTION, Boolean.TRUE.toString());
    }

    /**
     * DynamoDb container. To be used by auto config.
     *
     * @param dynamoDbEndpoint endpoint spec for dynamodb service
     * @return {@link Startable}
     */
    @Bean
    @Conditional(DynamodbContainerCondition.class)
    @Lazy
    public Startable dynamodbContainer(@Lazy final ServiceEndpoint dynamoDbEndpoint) {
        return createDockerContainer(dynamoDbEndpoint,
                this.containerNetworkManager,
                Wait.forListeningPort());
    }

    /**
     * KMS container. To be used by auto config.
     *
     * @param kmsEndpoint endpoint spec for kms service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.KMS)
    @Lazy
    public Startable kmsContainer(@Lazy final ServiceEndpoint kmsEndpoint) {
        final String logWaitRegex = "(.*)(Local KMS started on)(.*)";

        return createDockerContainer(kmsEndpoint,
                this.containerNetworkManager,
                new LogMessageWaitStrategy()
                        .withRegEx(logWaitRegex)
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT))
                        .withEnv(KMS_ACCOUNT_ID, this.awsSettings.getTestAccountId())
                        .withEnv(KMS_REGION, this.awsSettings.getTestDefaultRegion())
                        .withCopyFileToContainer(MountableFile.forClasspathResource(this.seedFile.getPath()), "/init/seed.yaml")
                        .withExposedPorts(kmsEndpoint.getContainerConfig().getPort());
    }

    /**
     * Redshift container. To be used by auto config.
     *
     * @param redshiftEndpoint endpoint spec for the redis service
     * @return {@link Startable}
     */
    @Bean
    @ConditionalOnService(Service.REDSHIFT)
    @Lazy
    public Startable redshiftContainer(@Lazy final ServiceEndpoint redshiftEndpoint) {
        final String logWaitRegex = "(.*)(database system is ready to accept connections)(.*)";

        // For Redshift a simple postgres container is started and used
        return createDockerContainer(redshiftEndpoint,
                this.containerNetworkManager,
                new LogMessageWaitStrategy()
                        .withRegEx(logWaitRegex)
                        .withTimes(2)
                        .withStartupTimeout(DEFAULT_WAIT_STRATEGY_TIMEOUT));
    }

    /**
     * Determine if Dynamodb service should be started locally.
     */
    static class DynamodbContainerCondition extends ContainerCondition.SimpleServiceCondition {

        @Override
        protected Service getService() {
            return Service.DYNAMO_DB;
        }
    }

    /**
     * Determine if Kinesis service should be started locally.
     */
    static class KinesisContainerCondition extends ContainerCondition.SimpleServiceCondition {

        @Override
        protected Service getService() {
            return Service.KINESIS;
        }
    }
}
