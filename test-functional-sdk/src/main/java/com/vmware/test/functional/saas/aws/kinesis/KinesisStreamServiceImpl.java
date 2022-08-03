/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Implementation of {@link KinesisStreamService}.
 */
public class KinesisStreamServiceImpl implements KinesisStreamService {

    private final KinesisClient kinesisClient;

    public KinesisStreamServiceImpl(final KinesisClient kinesisClient) {
        Preconditions.checkNotNull(kinesisClient, "Specified Kinesis client cannot be null");
        this.kinesisClient = kinesisClient;
    }

    @Override
    public PutRecordsResponse addRecords(final String streamName, final String partitionKey, final List<byte[]> recordsDataList) {
        final List<PutRecordsRequestEntry> recordsRequestEntries = recordsDataList.stream().map(record -> PutRecordsRequestEntry
                .builder()
                .data(SdkBytes.fromByteArray(record))
                .partitionKey(partitionKey)
                .build())
                .collect(Collectors.toList());
        final PutRecordsResponse putRecordResponse = this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(streamName)
                .records(recordsRequestEntries)
                .build());

        return putRecordResponse;
    }
}
