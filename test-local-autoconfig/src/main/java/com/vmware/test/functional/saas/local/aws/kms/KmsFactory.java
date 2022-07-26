/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.kms;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.config.AwsSettings;

/**
 * KMS Factory.
 * Provides local {@link KmsClient}. To be used by Functional tests.
 */
public class KmsFactory implements FactoryBean<KmsClient> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint kmsEndpoint;

    public KmsFactory(final ServiceEndpoint kmsEndpoint, final AwsSettings awsSettings) {
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
