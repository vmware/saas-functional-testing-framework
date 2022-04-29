/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.FactoryBean;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AwsSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * Lambda Factory.
 * Provides a local {@link LambdaClient}. To be used by Functional tests.
 */
@Slf4j
public class LambdaFactory implements FactoryBean<LambdaClient> {

    public static final int TIMEOUT_15_MINUTES = 15;
    private final AwsSettings awsSettings;
    private final LocalServiceEndpoint lambdaEndpoint;

    public LambdaFactory(final LocalServiceEndpoint lambdaEndpoint, final AwsSettings awsSettings) {
        this.awsSettings = awsSettings;
        this.lambdaEndpoint = lambdaEndpoint;
    }

    @Override
    public LambdaClient getObject() {
        final AwsCredentials credentials = AwsBasicCredentials.create(this.awsSettings.getTestAccessKey(), this.awsSettings.getTestSecretKey());

        final LambdaClient lambdaClient = LambdaClient.builder()
                .endpointOverride(URI.create(this.lambdaEndpoint.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(this.awsSettings.getTestDefaultRegion()))
                .httpClientBuilder(ApacheHttpClient.builder()
                        // There is a problem when debugging a lambda initiated with Request/Response option
                        // using the sync client (2.0 AWS apache-client). The problem is when the debugging session takes more time than
                        // the default read timout of the socket then the test harness will fail with an IOException and will
                        // leave an orphaned container instance. To avoid this we need to increase the socket timeout.
                        .socketTimeout(Duration.ofMinutes(TIMEOUT_15_MINUTES)))
                .build();
        log.info("Created LambdaClient [{}] with uri [{}]", System.identityHashCode(lambdaClient), this.lambdaEndpoint.getEndpoint());
        return lambdaClient;
    }

    @Override
    public Class<?> getObjectType() {
        return LambdaClient.class;
    }
}
