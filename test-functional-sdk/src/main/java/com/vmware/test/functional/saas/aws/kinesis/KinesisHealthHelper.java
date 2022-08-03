/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kinesis.model.StreamStatus;

/**
 * Kinesis Health Helper.
 */
public final class KinesisHealthHelper {

    private KinesisHealthHelper() {

    }

    /**
     * Kinesis Health Helper - verifying the stream status.
     *
     * @param kinesisClient {@link KinesisClient}.
     * @param streamName The Kinesis stream name.
     * @return {@code true} if the stream is {@code ACTIVE}, else {@code false}.
     */
    public static boolean checkHealth(final KinesisClient kinesisClient, final String streamName) {
        try {
            final DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();

            return kinesisClient.describeStream(describeStreamRequest).streamDescription().streamStatus().equals(StreamStatus.ACTIVE);
        } catch (final ResourceNotFoundException e) {
            return false;
        }
    }
}
