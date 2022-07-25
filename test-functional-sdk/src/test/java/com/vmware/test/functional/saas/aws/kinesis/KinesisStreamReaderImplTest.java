/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.aws.kinesis.test.KinesisStreamTestData;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link KinesisStreamReaderImpl}.
 */
public class KinesisStreamReaderImplTest extends KinesisStreamBaseTest {

    private KinesisStreamReader kinesisStreamReader;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createStream(KinesisStreamTestData.STREAM_NAME, 1);
        this.kinesisStreamReader = new KinesisStreamReaderImpl(this.kinesisClient, KinesisStreamTestData.STREAM_NAME);
    }

    @AfterClass(alwaysRun = true)
    private void deleteStream() {
        deleteStream(KinesisStreamTestData.STREAM_NAME);
    }

    @Test
    public void getSingleRecordFromKinesisStream() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        final List<KinesisClientRecord> records = this.kinesisStreamReader.getRecords();
        assertThat(records, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                records.stream()
                        .allMatch(
                                record -> Objects.equals(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()), record.data())));
    }

    @Test
    public void getRecordsFromKinesisStreamWithNoRecords() {
        final List<KinesisClientRecord> recordList = this.kinesisStreamReader.getRecords();
        assertThat(recordList, hasSize(0));
    }

    @Test
    public void getTwoRecordsFromKinesisStream() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader
                .getRecords();
        assertThat(recordList, hasSize(2));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> Arrays
                                .asList(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()),
                                        ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()))
                                .contains(record.data())));
    }

    @Test
    public void getRecordsContinuously() {
        // put record in a stream
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader
                .getRecords();
        assertThat(recordList, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> Objects.equals(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()), record.data())));
        // put record in a stream
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList2 = this.kinesisStreamReader
                .getRecords();
        assertThat(recordList2, hasSize(2));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList2.stream()
                        .allMatch(record -> Arrays
                                .asList(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()),
                                        ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()))
                                .contains(record.data())));
    }
}
