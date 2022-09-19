/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.dynamodb;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.config.AwsSettings;

/**
 * DynamoDb Factory.
 * Provides local {@link DynamoDbClient}. To be used by Functional tests.
 */
public class DynamoDbClientFactory implements FactoryBean<DynamoDbClient> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint dynamoDbEndpoint;

    public DynamoDbClientFactory(final ServiceEndpoint dynamoDbEndpoint, final AwsSettings awsSettings) {
        this.awsSettings = awsSettings;
        this.dynamoDbEndpoint = dynamoDbEndpoint;
    }

    @Override
    public DynamoDbClient getObject() {
        final AwsCredentials credentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return  DynamoDbClient.builder()
                .endpointOverride(URI.create(this.dynamoDbEndpoint.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return DynamoDbClient.class;
    }
}
