/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.kinesis.event;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.amazonaws.services.lambda.runtime.events.CognitoEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.vmware.test.functional.saas.aws.lambda.runtime.events.AWSEventHelper;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link AWSEventHelper}.
 */
public class AWSEventHelperTest {

    @Test
    void eventToSdkBytesConstructsTheCorrectSdkBytesForSQSEvent() {
        final SQSEvent sqsEvent = new SQSEvent();
        final SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("body");
        message.setAttributes(Collections.emptyMap());
        message.setMessageAttributes(Collections.emptyMap());
        sqsEvent.setRecords(Collections.singletonList(message));

        final String sqsEventAsString = AWSEventHelper.eventToSdkBytes(sqsEvent).asUtf8String();
        final SQSEvent serializedSQSEvent = LambdaEventSerializers.serializerFor(SQSEvent.class, getClass().getClassLoader())
                .fromJson(sqsEventAsString);
        assertThat("Prepared SQS event is null - unexpected.", sqsEventAsString, notNullValue());
        assertThat("Serialized SQS event is null - unexpected.", serializedSQSEvent, notNullValue());

        final String resultData = serializedSQSEvent.getRecords().stream()
                .map(SQSEvent.SQSMessage::getBody)
                .findFirst()
                .orElseThrow();
        assertThat("Expected event is not matching the original.", resultData, is("body"));
    }

    @Test
    void eventToSdkBytesConstructsTheCorrectSdkBytesForEvent() {
        final CognitoEvent cognitoEvent = new CognitoEvent();
        final CognitoEvent.DatasetRecord datasetRecord = new CognitoEvent.DatasetRecord();
        datasetRecord.setNewValue("newValue");
        datasetRecord.setOldValue("oldValue");
        final Map<String, CognitoEvent.DatasetRecord> data = Map.of("SampleKey2", datasetRecord);
        cognitoEvent.setDatasetRecords(data);

        final String cognitoEventString = AWSEventHelper.eventToSdkBytes(cognitoEvent).asUtf8String();
        final CognitoEvent serializedCognitoEvent = LambdaEventSerializers.serializerFor(CognitoEvent.class, getClass().getClassLoader())
                .fromJson(cognitoEventString);
        assertThat("Prepared Cognito event is null - unexpected.", cognitoEventString, notNullValue());
        assertThat("Serialized Cognito event is null - unexpected.", serializedCognitoEvent, notNullValue());

        final Map<String, CognitoEvent.DatasetRecord> resultData = serializedCognitoEvent.getDatasetRecords();
        assertThat("Expected event is not matching the original.", resultData, is(data));
    }
}
