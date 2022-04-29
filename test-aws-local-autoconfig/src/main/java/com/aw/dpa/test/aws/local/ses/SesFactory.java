/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.ses;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AwsSettings;

/**
 * SES Factory.
 * Provides local {@link SesClient}. To be used by Functional tests.
 */
public final class SesFactory implements FactoryBean<SesClient> {

    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint sesEndpoint;

    public SesFactory(final LocalServiceEndpoint sesEndpoint, final AwsSettings awsSettings) {
        this.sesEndpoint = sesEndpoint;
        this.awsSettings = awsSettings;
    }

    @Override
    public SesClient getObject() {
        final AwsCredentials awsCredentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return SesClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.sesEndpoint.getEndpoint()))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return SesClient.class;
    }
}
