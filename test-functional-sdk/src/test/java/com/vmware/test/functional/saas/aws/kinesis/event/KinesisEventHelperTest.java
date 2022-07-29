/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.kinesis.event;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.models.kinesis.Record;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.vmware.test.functional.saas.aws.lambda.LambdaServiceHelper;
import com.vmware.test.functional.saas.aws.lambda.runtime.events.kinesis.KinesisEventHelper;
import com.vmware.test.functional.saas.aws.lambda.runtime.events.kinesis.KinesisEventRecordSpec;
import com.vmware.test.functional.saas.aws.lambda.runtime.events.kinesis.KinesisRecordSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link KinesisEventHelper}.
 */
public class KinesisEventHelperTest {

    private static final Map<String, String> TEST_DATA = Map.of("key", "value");

    @Test
    void verifyPrepareKinesisEvent() throws JsonProcessingException {
        final KinesisEventHelper kinesisEventHelper = new KinesisEventHelper(
                KinesisRecordSpec.builder().build(),
                KinesisEventRecordSpec.builder()
                        .awsRegion("us-west1")
                        .build());
        final String kinesisEventAsString = kinesisEventHelper
                .prepareKinesisEvent(LambdaServiceHelper.mapGenericRecord(TEST_DATA)).asUtf8String();

        assertThat("Prepared Kinesis event is null - unexpected.", kinesisEventAsString, notNullValue());

        final List<KinesisEvent.KinesisEventRecord> kinesisEventRecords =
                LambdaEventSerializers.serializerFor(KinesisEvent.class, getClass().getClassLoader())
                        .fromJson(kinesisEventAsString)
                        .getRecords();
        assertThat("Kinesis Event Records List is null - unexpected.", kinesisEventRecords, notNullValue());
        assertThat("Kinesis Event Records List size does not equal expected value of 1.", kinesisEventRecords.size(), equalTo(1));
        final Map<String, String> resultData = kinesisEventRecords.stream()
                .map(KinesisEvent.KinesisEventRecord::getKinesis)
                .map(Record::getData)
                .map(ByteBuffer::array)
                .map(this::getStringStringMap)
                .findFirst()
                .orElseThrow();
        assertThat("Result Data does not equal expected.", resultData, equalTo(TEST_DATA));
    }

    @SneakyThrows
    private Map<String, String> getStringStringMap(final byte[] bytes) {
        return new ObjectMapper().readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<Map<String, String>>() {

        });
    }
}
