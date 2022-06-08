/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.kinesis;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.aws.local.AwsSettings;

/**
 * Kinesis Factory.
 * Provides local {@link KinesisClient}. To be used by Functional tests.
 */
public class KinesisFactory implements FactoryBean<KinesisClient> {

    private final AwsSettings awsSettings;
    private final ServiceEndpoint kinesisEndpoint;

    public KinesisFactory(final ServiceEndpoint kinesisEndpoint, final AwsSettings awsSettings) {
        this.awsSettings = awsSettings;
        this.kinesisEndpoint = kinesisEndpoint;
    }

    @Override
    public KinesisClient getObject() {
        final AwsCredentials credentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        return KinesisClient.builder()
                .endpointOverride(URI.create(this.kinesisEndpoint.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                // Kinesalite configured with ssl and TrustAllCertificates needs to be true
                .httpClient(
                        new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap
                                .builder()
                                .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true)
                                .build()))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return KinesisClient.class;
    }
}
