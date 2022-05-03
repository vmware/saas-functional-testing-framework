/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.sqs;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

import java.net.URI;
import java.util.Objects;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.LocalServiceEndpoint;
import com.vmware.test.functional.saas.aws.local.AwsSettings;

/**
 * SQS Factory.
 * Provides a local {@link SqsClient}. To be used by Functional tests.
 */
public class SQSFactory implements FactoryBean<SqsClient> {

    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint sqsEndpoint;

    public SQSFactory(final LocalServiceEndpoint sqsEndpoint, final AwsSettings awsSettings) {
        this.sqsEndpoint = sqsEndpoint;
        this.awsSettings = awsSettings;
    }

    @Override
    public SqsClient getObject() {
        final AwsCredentials awsCredentials = AwsBasicCredentials
                .create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return SqsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.sqsEndpoint.getEndpoint()))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return SqsClient.class;
    }

    /**
     * Create a SQS queue URL by interrogating the SQS instance.
     *
     * @param queueName The queue name (which must be first created by the method {@code sqsQueuesSpec()}).
     * @return SQS queue URL.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Spring 5.x - handle new @Nullable annotation on FactoryBean")
    public String buildSqsQueueUrl(final String queueName) {
        final GetQueueUrlRequest req = GetQueueUrlRequest.builder().queueName(queueName).build();
        return Objects.requireNonNull(getObject()).getQueueUrl(req).queueUrl();
    }
}
