/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.service;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.LocalServiceEndpoint;
import com.vmware.test.functional.saas.SharedConfig;
import com.vmware.test.functional.saas.aws.local.utils.ServiceDependenciesHealthHelper;
import com.vmware.test.functional.saas.aws.s3.S3BucketSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSpecs;
import com.vmware.test.functional.saas.aws.s3.S3HealthHelper;
import com.vmware.test.functional.saas.aws.sqs.SqsHealthHelper;
import com.vmware.test.functional.saas.aws.sqs.SqsQueuesSpec;

import static org.hamcrest.MatcherAssert.*;

/**
 * {@link ServiceDependencies} Context Hierarchy tests.
 * Test verifies service dependencies specified in context hierarchy(listed in 3 levels of parent and child contexts) are correctly loaded
 * and all service dependencies are started in docker containers.
 * Current test verifies that S3 and SQS, which are started in different contexts, are properly started in different localStack containers
 * and have the correct {@code InternalContainerServiceConfig} set for their respective {@link LocalServiceEndpoint}s.
 * AutoConfiguration is specified in test/resources/META-INF/spring.factories which wires
 * SharedConfig.java to a single test contexts annotated with {@link ServiceDependencies} declaring KMS as a required service.
 * Extending AbstractFunctionalTests configures {@link SharedConfig} with autoconfigured test contexts to be loaded
 * as parent context for this test.
 */
@ContextHierarchy({
        @ContextConfiguration(classes = LocalStackServiceDependenciesContextHierarchyTest.StartS3ChildContext.class),
        @ContextConfiguration(classes = LocalStackServiceDependenciesContextHierarchyTest.StartSqsChildContext.class)
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LocalStackServiceDependenciesContextHierarchyTest extends AbstractFunctionalTests {

    /**
     * Child Context which specifies S3 as a Service Dependency.
     */
    @Configuration
    @ServiceDependencies(Service.S3)
    public static class StartS3ChildContext {

        @Bean
        LocalServiceEndpoint s3ContextLocalStackEndpoint(final LocalServiceEndpoint localStackEndpoint) {
            return localStackEndpoint;
        }

        @Bean
        S3BucketSettings s3BucketSettings() {
            return S3BucketSettings.builder()
                    .name("test-bucket")
                    .build();
        }

        @Bean
        S3BucketSpecs s3BucketSpecs(final S3BucketSettings s3BucketSettings) {
            return S3BucketSpecs.builder()
                    .bucket(s3BucketSettings)
                    .build();
        }
    }

    /**
     * Child Context which specifies SQS as a Service Dependency.
     */
    @Configuration
    @ServiceDependencies(Service.SQS)
    public static class StartSqsChildContext {

        @Bean
        SqsQueuesSpec sqsQueuesSpec() {
            return SqsQueuesSpec.builder()
                    .queuesToCreate(List.of("test-queue")).build();
        }

    }

    @Autowired
    LocalServiceEndpoint s3Endpoint;

    @Autowired
    LocalServiceEndpoint sqsEndpoint;

    @Autowired
    LocalServiceEndpoint localStackEndpoint;

    @Autowired
    LocalServiceEndpoint s3ContextLocalStackEndpoint;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private KmsClient awskmsClient;

    @Test
    public void listServiceDependenciesInContextHierarchy() {
        assertThat("kms is running ok", ServiceDependenciesHealthHelper.isKmsHealthy(this.awskmsClient));
        assertThat("SQS service is running OK.", SqsHealthHelper.checkHealth(this.sqsClient, "test-queue"));
        assertThat("S3 service is running OK.", S3HealthHelper.checkHealth(this.s3Client, "test-bucket"));
        assertThat("sqsEndpoint points to correct localStackEndpoint instance",
                this.sqsEndpoint.getInternalDockerEndpoint().equals(this.localStackEndpoint.getInternalDockerEndpoint()));
        assertThat("s3Endpoint points to correct localStackEndpoint instance",
                this.s3Endpoint.getInternalDockerEndpoint().equals(this.s3ContextLocalStackEndpoint.getInternalDockerEndpoint()));
        assertThat("sqsEndpoint and s3Endpoint point to different localStack instances",
                !this.sqsEndpoint.getInternalDockerEndpoint().equals(this.s3ContextLocalStackEndpoint.getInternalDockerEndpoint()));
    }
}
