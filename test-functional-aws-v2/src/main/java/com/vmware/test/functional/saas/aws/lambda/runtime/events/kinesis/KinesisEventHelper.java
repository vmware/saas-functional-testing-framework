/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.lambda.runtime.events.kinesis;

import software.amazon.awssdk.core.SdkBytes;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.vmware.test.functional.saas.aws.lambda.runtime.events.AWSEventHelper;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for an AWS Kinesis Event. To be used for the invocation of Lambda function(s).
 */
@Slf4j
public class KinesisEventHelper {
    private final KinesisRecordSpec recordSpec;
    private final KinesisEventRecordSpec eventRecordSpec;

    /**
     * Constructor for {@code KinesisEventHelper}.
     *
     * @param recordSpec Kinesis record spec.
     * @param eventRecordSpec Kinesis event record spec.
     */
    public KinesisEventHelper(final KinesisRecordSpec recordSpec, final KinesisEventRecordSpec eventRecordSpec) {
        Preconditions.checkNotNull(recordSpec, "recordSpec must not be null");
        Preconditions.checkNotNull(eventRecordSpec, "eventRecordSpec must not be null");

        this.recordSpec = recordSpec;
        this.eventRecordSpec = eventRecordSpec;
    }

    /**
     * Method used to construct a {@code KinesisEvent} using the supplied {@code ByteBuffer} for the data portion.
     * The method then serializes the constructed {@code KinesisEvent} into the {@code SdkBytes} wrapper.
     *
     * @param byteBuffer Data for the Kinesis event record.
     * @return {@code SdkBytes} formatted Kinesis event.
     */
    public SdkBytes prepareKinesisEvent(final ByteBuffer byteBuffer) {
        Preconditions.checkNotNull(byteBuffer, "byteBuffer must not be null");

        final KinesisEvent.Record record = new KinesisEvent.Record();
        record.setData(byteBuffer);
        record.setKinesisSchemaVersion(this.recordSpec.getKinesisSchemaVersion());
        record.setSequenceNumber(this.recordSpec.getSequenceNumber());
        record.setPartitionKey(this.recordSpec.getPartitionKey());
        record.withEncryptionType(this.recordSpec.getEncryptionType());
        record.setApproximateArrivalTimestamp(new Date());
        log.info("Created Kinesis record: [{}]", record);

        final KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
        kinesisEventRecord.setEventSource(this.eventRecordSpec.getEventSource());
        kinesisEventRecord.setKinesis(record);
        kinesisEventRecord.setEventID(this.eventRecordSpec.getEventID());
        kinesisEventRecord.setInvokeIdentityArn(this.eventRecordSpec.getInvokeEntityArn());
        kinesisEventRecord.setEventName(this.eventRecordSpec.getEventName());
        kinesisEventRecord.setEventVersion(this.eventRecordSpec.getEventVersion());
        kinesisEventRecord.setEventSourceARN(this.eventRecordSpec.getEventSourceArn());
        kinesisEventRecord.setAwsRegion(this.eventRecordSpec.getAwsRegion());

        final List<KinesisEvent.KinesisEventRecord> kinesisEventRecords = Collections.singletonList(kinesisEventRecord);

        final KinesisEvent kinesisEvent = new KinesisEvent();
        kinesisEvent.setRecords(kinesisEventRecords);

        return AWSEventHelper.eventToSdkBytes(kinesisEvent);
    }
}
