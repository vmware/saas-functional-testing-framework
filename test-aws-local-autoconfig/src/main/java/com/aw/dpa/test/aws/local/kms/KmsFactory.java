/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.kms;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AwsSettings;

/**
 * KMS Factory.
 * Provides local {@link KmsClient}. To be used by Functional tests.
 */
public class KmsFactory implements FactoryBean<KmsClient> {

    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint kmsEndpoint;

    public KmsFactory(final LocalServiceEndpoint kmsEndpoint, final AwsSettings awsSettings) {
        this.kmsEndpoint = kmsEndpoint;
        this.awsSettings = awsSettings;
    }

    @Override
    public KmsClient getObject() {
        final AwsCredentials awsCredentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return KmsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.kmsEndpoint.getEndpoint()))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return KmsClient.class;
    }
}
