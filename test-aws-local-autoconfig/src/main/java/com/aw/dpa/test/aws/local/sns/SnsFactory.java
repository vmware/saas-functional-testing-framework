/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.sns;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AwsSettings;

/**
 * SNS Factory.
 * Provides local {@link SnsClient}. To be used by Functional tests.
 */
public class SnsFactory implements FactoryBean<SnsClient> {

    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint snsEndpoint;

    public SnsFactory(final LocalServiceEndpoint snsEndpoint, final AwsSettings awsSettings) {
        this.snsEndpoint = snsEndpoint;
        this.awsSettings = awsSettings;
    }

    @Override
    public SnsClient getObject() {
        final AwsCredentials awsCredentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return SnsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.snsEndpoint.getEndpoint()))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return SnsClient.class;
    }
}
