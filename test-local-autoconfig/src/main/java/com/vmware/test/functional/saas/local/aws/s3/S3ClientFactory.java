/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.config.AwsSettings;

/**
 * S3 Factory.
 * Provides local {@link S3Client}. To be used by Functional tests.
 */
public final class S3ClientFactory implements FactoryBean<S3Client> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint s3Endpoint;

    public S3ClientFactory(final ServiceEndpoint s3Endpoint, final AwsSettings awsSettings) {
        this.s3Endpoint = s3Endpoint;
        this.awsSettings = awsSettings;
    }

    @Override
    public S3Client getObject() {
        final AwsCredentials awsCredentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.s3Endpoint.getEndpoint()))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return S3Client.class;
    }
}
