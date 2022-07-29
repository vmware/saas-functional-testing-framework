/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.aws.kinesis.test.KinesisStreamTestData;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link KinesisStreamReaderImpl} for a stream with multiple shards.
 */
public class KinesisStreamReaderMultipleShardsTest extends KinesisStreamBaseTest {

    private KinesisStreamReader kinesisStreamReader;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        // create stream with two shards
        createStream(KinesisStreamTestData.STREAM_NAME_2, 2);
        this.kinesisStreamReader = new KinesisStreamReaderImpl(this.kinesisClient, KinesisStreamTestData.STREAM_NAME_2);
    }

    @AfterClass(alwaysRun = true)
    public void deleteStream() {
        deleteStream(KinesisStreamTestData.STREAM_NAME_2);
    }

    @Test
    public void getRecordsFromStreamWithTwoShards() {
        // put records with different partitionKeys in a stream
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME_2)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader.getRecords();
        assertThat(recordList, hasSize(2));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> Arrays
                                .asList(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()),
                                        ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()))
                                .contains(record.data())));
    }

    @Test
    public void getOneRecordFromEachShardInStream() {
        final List<Shard> shards = this.kinesisClient.describeStream(
                DescribeStreamRequest
                        .builder()
                        .streamName(KinesisStreamTestData.STREAM_NAME_2)
                        .build()).streamDescription().shards();

        /*
         * Save shard HashKeyRange startingHashKey used for overriding partitionKeys in PutRecordsRequestEntry in order to
         * make sure records are added to different shards
         */
        final List<String> shardExplicitHashKeys = shards.stream().map(shard -> shard.hashKeyRange().startingHashKey()).collect(Collectors.toList());
        assertThat(shardExplicitHashKeys, hasSize(2));

        // put records with explicit hash keys to make sure records are put in different shards
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME_2)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader.getRecords();
        assertThat(recordList, hasSize(2));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> Arrays
                                .asList(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()),
                                        ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()))
                                .contains(record.data())));
    }
}
