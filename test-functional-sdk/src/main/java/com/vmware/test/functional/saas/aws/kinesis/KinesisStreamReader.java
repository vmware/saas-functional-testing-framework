/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.util.List;

/**
 * Simplified operations for reading/retrieving records from AWS Kinesis Streams.
 *
 * Create {@link KinesisStreamReader} for a test stream we expect some records will be added
 * Call getRecords() to see records published to the stream as many times as is necessary to get the expected result, or until we are not seeing any more records
 * {@link KinesisStreamReader} should be created per one test and not get re-used by other tests.
 */
public interface KinesisStreamReader {

    /**
     * Reads data records from AWS Kinesis Stream sharditeratorList.
     *
     * @return The LATEST records in the stream, if any.
     */
    List<KinesisClientRecord> getRecords();
}
