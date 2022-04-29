/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.lambda.runtime.events.kinesis;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Configuration for local AWS Kinesis event record.
 */
@Builder
@Data
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
public class KinesisEventRecordSpec {

    private static final String EVENT_SOURCE_KINESIS = "aws:kinesis";
    private static final String EVENT_SOURCE_ARN_FORMAT = "arn%s:%s:%s:stream/";
    private static final String EVENT_ID_FORMAT = "shardId-%s:%s";
    private static final String EVENT_NAME_KINESIS_RECORD = "aws:kinesis:record";
    private static final String INVOKE_IDENTITY_ARN_FORMAT = "arn:%s:iam::%s";
    private static final String SHARD_ID = "000000000000";
    private static final String IDENTITY = "SAMPLE";

    @Builder.Default
    private String eventSource = EVENT_SOURCE_KINESIS;
    @Builder.Default
    private String eventID = String.format(EVENT_ID_FORMAT, SHARD_ID, RandomUtils.nextLong());
    @Builder.Default
    private String invokeEntityArn = String.format(INVOKE_IDENTITY_ARN_FORMAT, KinesisRecordSpec.PARTIITON_KEY, IDENTITY);
    @Builder.Default
    private String eventName = EVENT_NAME_KINESIS_RECORD;

    private String eventVersion;
    private String eventSourceArn;
    @NonNull
    private String awsRegion;

    /**
     * Gets specified {@code eventSourceArn} or its default value.
     * @return  specified {@code eventSourceArn} or its default value
     */
    public String getEventSourceArn() {
        if (StringUtils.isBlank(this.eventSourceArn)) {
            return String.format(EVENT_SOURCE_ARN_FORMAT, EVENT_SOURCE_KINESIS, this.awsRegion, SHARD_ID);
        }
        return this.eventSourceArn;
    }
}
