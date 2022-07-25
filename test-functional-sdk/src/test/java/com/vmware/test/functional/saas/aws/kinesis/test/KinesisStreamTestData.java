/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.kinesis.test;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.util.UUID;

/**
 * Kinesis Stream Test Data.
 */
public final class KinesisStreamTestData {

    public static final String STREAM_NAME = "test_stream_1";
    public static final String STREAM_NAME_2 = "test_stream_2";

    public static final byte[] RECORD_DATA_1 = "kinesis test record data 1 ".getBytes();
    public static final byte[] RECORD_DATA_2 = "kinesis test record data 2".getBytes();

    public static final String PARTITION_KEY_1 = "foo_" + UUID.randomUUID();
    public static final String PARTITION_KEY_2 = "bar_" + UUID.randomUUID();

    public static final PutRecordsRequestEntry RECORD_1 = PutRecordsRequestEntry.builder()
            .partitionKey(KinesisStreamTestData.PARTITION_KEY_1)
            .data(SdkBytes.fromByteArray(RECORD_DATA_1))
            .build();

    public static final PutRecordsRequestEntry RECORD_2 = PutRecordsRequestEntry.builder()
            .partitionKey(KinesisStreamTestData.PARTITION_KEY_2)
            .data(SdkBytes.fromByteArray(RECORD_DATA_2))
            .build();

    private KinesisStreamTestData() {
    }

}
