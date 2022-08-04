/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.lambda.runtime.events.kinesis;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.commons.lang3.RandomUtils;

import com.amazonaws.services.lambda.runtime.events.models.kinesis.EncryptionType;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for an AWS Kinesis record.
 */
@Builder
@Data
public class KinesisRecordSpec {

    static final String PARTIITON_KEY = "partitionKey-03";
    private static final String KINESIS_SCHEMA_VERSION = "1.0";

    @Builder.Default
    private String kinesisSchemaVersion = KINESIS_SCHEMA_VERSION;
    @Builder.Default
    private String sequenceNumber = String.valueOf(RandomUtils.nextLong());
    @Builder.Default
    private String partitionKey = PARTIITON_KEY;
    @Builder.Default
    private EncryptionType encryptionType = EncryptionType.NONE;
    @Builder.Default
    private Date approximateArrivalTimestamp = new Date();
    private ByteBuffer data;
}
