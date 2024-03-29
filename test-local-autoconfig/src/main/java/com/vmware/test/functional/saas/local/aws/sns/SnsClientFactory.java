/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.sns;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.config.AwsSettings;

/**
 * SNS Factory.
 * Provides local {@link SnsClient}. To be used by Functional tests.
 */
public class SnsClientFactory implements FactoryBean<SnsClient> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint snsEndpoint;

    public SnsClientFactory(final ServiceEndpoint snsEndpoint, final AwsSettings awsSettings) {
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
