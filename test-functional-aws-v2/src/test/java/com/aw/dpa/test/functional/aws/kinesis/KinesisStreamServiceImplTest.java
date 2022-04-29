/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.kinesis;

import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aw.dpa.test.functional.aws.kinesis.test.KinesisStreamTestData;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link KinesisStreamServiceImpl}.
 */
public class KinesisStreamServiceImplTest extends KinesisStreamBaseTest {

    private KinesisStreamService kinesisStreamService;

    @BeforeMethod(alwaysRun = true)
    private void createStream() {
        createStream(KinesisStreamTestData.STREAM_NAME, 1);
        this.kinesisStreamService = new KinesisStreamServiceImpl(this.kinesisClient);
    }

    @AfterMethod(alwaysRun = true)
    private void deleteStream() {
        deleteStream(KinesisStreamTestData.STREAM_NAME);
    }

    @Test
    public void addOneRecordToStream() {
        final PutRecordsResponse putRecordsResponse = this.kinesisStreamService
                .addRecords(KinesisStreamTestData.STREAM_NAME, KinesisStreamTestData.PARTITION_KEY_1,
                        Collections.singletonList(KinesisStreamTestData.RECORD_DATA_1));

        assertThat(putRecordsResponse.records(), hasSize(1));
        assertThat(putRecordsResponse.failedRecordCount(), equalTo(0));
    }

    @Test
    public void addMultipleRecordsToStream() {
        final PutRecordsResponse putRecordsResponse = this.kinesisStreamService
                .addRecords(KinesisStreamTestData.STREAM_NAME, KinesisStreamTestData.PARTITION_KEY_1,
                        Arrays.asList(KinesisStreamTestData.RECORD_DATA_1, KinesisStreamTestData.RECORD_DATA_2));

        assertThat(putRecordsResponse.records(), hasSize(2));
        assertThat(putRecordsResponse.failedRecordCount(), equalTo(0));
    }
}
