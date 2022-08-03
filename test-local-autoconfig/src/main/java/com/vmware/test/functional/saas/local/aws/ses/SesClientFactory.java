/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.ses;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.AwsSettings;

/**
 * SES Factory.
 * Provides local {@link SesClient}. To be used by Functional tests.
 */
public final class SesClientFactory implements FactoryBean<SesClient> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint sesEndpoint;

    public SesClientFactory(final ServiceEndpoint sesEndpoint, final AwsSettings awsSettings) {
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
