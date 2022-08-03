/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.aws.kinesis.test.KinesisStreamTestData;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link FilteringKinesisStreamReaderImpl}.
 */
public class FilteringKinesisStreamReaderTest extends KinesisStreamBaseTest {

    private static final String POLL_TIMEOUT_EXCEPTION_REGEX = "^Condition with alias 'Polling for records from stream \\[.*]' didn't complete within.*";

    private FilteringKinesisStreamReader<byte[]> kinesisStreamReader;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createStream(KinesisStreamTestData.STREAM_NAME, 1);
        this.kinesisStreamReader = new FilteringKinesisStreamReaderImpl<>(
                this.kinesisClient,
                KinesisStreamTestData.STREAM_NAME,
                ByteBuffer::array);
    }

    @AfterClass(alwaysRun = true)
    private void deleteStream() {
        deleteStream(KinesisStreamTestData.STREAM_NAME);
    }

    @Test
    public void getRecordsWithFilterMatchesOneOfOne() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        final List<KinesisClientRecord> records = this.kinesisStreamReader.getRecords(
                recordWrapper -> Arrays.equals(KinesisStreamTestData.RECORD_DATA_1, recordWrapper.getTransformedData())
        );
        assertThat(records, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                records.stream()
                        .allMatch(
                                record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()).equals(record.data())));
    }

    @Test
    public void getRecordsWithFilterMatchesOneOfTwo() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> records = this.kinesisStreamReader.getRecords(
                recordWrapper -> Arrays.equals(KinesisStreamTestData.RECORD_DATA_1, recordWrapper.getTransformedData())
        );
        assertThat(records, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                records.stream()
                        .allMatch(
                                record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()).equals(record.data())));
    }

    @Test
    public void getRecordsWithFilterReturnsEmptyWhenNoRecordsInStream() {
        final List<KinesisClientRecord> recordList = this.kinesisStreamReader.getRecords(rw -> true);
        assertThat(recordList, hasSize(0));
    }

    @Test
    public void getRecordsWithFilterReturnsEmptyWhenNoRecordsMatch() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        final List<KinesisClientRecord> records = this.kinesisStreamReader.getRecords(
                recordWrapper -> Arrays.equals(KinesisStreamTestData.RECORD_DATA_2, recordWrapper.getTransformedData())
        );
        assertThat(records, hasSize(0));
    }

    @Test
    public void getRecordsWithFilterReturnsMultipleRecord() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader
                .getRecords(rw -> true);
        assertThat(recordList, hasSize(2));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> Arrays
                                .asList(ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()),
                                        ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()))
                                .contains(record.data())));
    }

    @Test
    public void getRecordsWithFilterContinuously() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader
                .getRecords(rw -> true);
        assertThat(recordList, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()).equals(record.data())));
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList2 = this.kinesisStreamReader
                .getRecords(rw -> true);
        assertThat(recordList2, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList2.stream()
                        .allMatch(record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()).equals(record.data())));
    }

    @Test
    public void pollForRecordsMatchesOneOfTwoCreatedBeforePoll() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2)
                .build());

        final List<KinesisClientRecord> recordList = this.kinesisStreamReader.pollForRecordsUntil(
                recordWrapper -> Arrays.equals(KinesisStreamTestData.RECORD_DATA_1, recordWrapper.getTransformedData()),
                rws -> rws.size() == 1);
        assertThat(recordList, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_1.data().asByteArray()).equals(record.data())));
    }

    @Test
    public void pollForRecordsMatchesOneOfTwoCreatedAfterPoll() {
        // start polling before writing to stream
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final Future<List<KinesisClientRecord>> future = executorService.submit(() -> this.kinesisStreamReader.pollForRecordsUntil(
                recordWrapper -> Arrays.equals(KinesisStreamTestData.RECORD_DATA_2, recordWrapper.getTransformedData()),
                rws -> rws.size() == 1)
        );

        final List<PutRecordsRequestEntry> putRecordsRequestEntries = Arrays.asList(
                KinesisStreamTestData.RECORD_1, KinesisStreamTestData.RECORD_2);
        putRecordsRequestEntries.forEach(entry ->
                this.kinesisClient.putRecords(PutRecordsRequest.builder()
                    .streamName(KinesisStreamTestData.STREAM_NAME)
                    .records(entry)
                    .build())
        );

        List<KinesisClientRecord> recordList = null;
        try {
            recordList = future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }

        assertThat(recordList, hasSize(1));
        assertThat("Returned records from kinesis stream does not match expected ones.",
                recordList.stream()
                        .allMatch(record -> ByteBuffer.wrap(KinesisStreamTestData.RECORD_2.data().asByteArray()).equals(record.data())));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = POLL_TIMEOUT_EXCEPTION_REGEX)
    public void pollForRecordsTimesOutWhenNoRecordsInStream() {
        this.kinesisStreamReader.pollForRecordsUntil(
                Duration.ofMillis(25),
                Duration.ofMillis(200),
                rw -> true,
                rws -> rws.size() == 1);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = POLL_TIMEOUT_EXCEPTION_REGEX)
    public void pollForRecordsTimesOutWhenReturnConditionNotSatisfied() {
        this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(KinesisStreamTestData.STREAM_NAME)
                .records(KinesisStreamTestData.RECORD_1)
                .build());

        this.kinesisStreamReader.pollForRecordsUntil(
                Duration.ofMillis(25),
                Duration.ofMillis(200),
                rw -> true,
                rws -> rws.size() == 2);
    }
}
