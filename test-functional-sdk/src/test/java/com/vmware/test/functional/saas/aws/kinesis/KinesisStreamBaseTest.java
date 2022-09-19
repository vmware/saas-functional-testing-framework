/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DeleteStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;
import java.util.Collections;

import org.awaitility.Awaitility;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static com.vmware.test.functional.saas.aws.LocalTestConstants.KINESALITE_DOCKER_IMAGE;
import static com.vmware.test.functional.saas.aws.LocalTestConstants.KINESALITE_HOST_PORT;
import static com.vmware.test.functional.saas.aws.LocalTestConstants.KINESALITE_PORT;
import static com.vmware.test.functional.saas.aws.LocalTestConstants.TEST_ACCESS_KEY_ID;
import static com.vmware.test.functional.saas.aws.LocalTestConstants.TEST_SECRET_KEY_ID;

/**
 * Class used for starting kinesalite docker container and configuring
 * {@link KinesisClient} used in kinesis streams tests.
 */
public class KinesisStreamBaseTest {

    KinesisClient kinesisClient;
    private GenericContainer<?> kinesaliteContainer;

    @BeforeClass(alwaysRun = true)
    public void configureLocalKinesis() {
        this.kinesaliteContainer =
                new GenericContainer<>(KINESALITE_DOCKER_IMAGE)
                        .withCommand("--ssl", "true");

        this.kinesaliteContainer
                .setPortBindings(Collections.singletonList(String.format("%d:%d/%s", KINESALITE_HOST_PORT, KINESALITE_PORT, InternetProtocol.TCP)));

        this.kinesaliteContainer.start();

        System.setProperty(SdkSystemSetting.CBOR_ENABLED.property(), Boolean.FALSE.toString());

        this.kinesisClient = KinesisClient.builder()
                .endpointOverride(URI.create(getKinesaliteEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials
                        .create(TEST_ACCESS_KEY_ID, TEST_SECRET_KEY_ID)))
                .region(Region.US_WEST_1)
                // Kinesalite configured with ssl and TrustAllCertificates needs to be true
                .httpClient(ApacheHttpClient.builder()
                        .buildWithDefaults(AttributeMap.builder()
                                .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true)
                                .build()))
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void stopKinesalite() {
        this.kinesaliteContainer.stop();
    }

    void createStream(final String streamName, final int shardCount) {
        final CreateStreamRequest createStreamRequest = CreateStreamRequest.builder()
                .streamName(streamName)
                .shardCount(shardCount)
                .build();
        this.kinesisClient.createStream(createStreamRequest);
        final DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();
        Awaitility.await().until(() -> this.kinesisClient.describeStream(describeStreamRequest).streamDescription().streamStatus().equals(StreamStatus.ACTIVE));
    }

    void deleteStream(final String streamName) {
        this.kinesisClient.deleteStream(DeleteStreamRequest.builder().streamName(streamName).build());
        Awaitility.await().until(() -> verifyDeleted(streamName));
    }

    private boolean verifyDeleted(final String streamName) {
        try {
            final DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();
            this.kinesisClient.describeStream(describeStreamRequest);
            return false;
        } catch (final ResourceNotFoundException e) {
            return true;
        }
    }

    private String getKinesaliteEndpoint() {
        return String.format("https://localhost:%s", KINESALITE_HOST_PORT);
    }
}
