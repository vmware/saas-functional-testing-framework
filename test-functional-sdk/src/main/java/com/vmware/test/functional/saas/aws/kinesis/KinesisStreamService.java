/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.util.List;

/**
 * Simplified operations for interacting with the AWS Kinesis Streams.
 */
public interface KinesisStreamService {

    /**
     * Puts multiple data records into a Kinesis stream in a single call.
     *
     * @param streamName   The stream name.
     * @param partitionKey Partition key. (cannot be null or empty)
     * @param recordsDataList List of records byte[] data.
     * @return PutRecordsResponse PutRecords results.
     */
    PutRecordsResponse addRecords(String streamName, String partitionKey, List<byte[]> recordsDataList);

}
