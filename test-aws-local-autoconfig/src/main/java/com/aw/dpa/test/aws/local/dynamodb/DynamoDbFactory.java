/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.dynamodb;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AwsSettings;

/**
 * DynamoDb Factory.
 * Provides local {@link DynamoDbClient}. To be used by Functional tests.
 */
public class DynamoDbFactory implements FactoryBean<DynamoDbClient> {

    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint dynamoDbEndpoint;

    public DynamoDbFactory(final LocalServiceEndpoint dynamoDbEndpoint, final AwsSettings awsSettings) {
        this.awsSettings = awsSettings;
        this.dynamoDbEndpoint = dynamoDbEndpoint;
    }

    @Override
    public DynamoDbClient getObject() {
        final AwsCredentials credentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return  DynamoDbClient.builder()
                .endpointOverride(URI.create(this.dynamoDbEndpoint.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return DynamoDbClient.class;
    }
}
