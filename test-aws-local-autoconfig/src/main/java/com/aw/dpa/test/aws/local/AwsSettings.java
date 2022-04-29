/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.aw.dpa.test.aws.local.kinesis.KinesisSettings;
import com.aw.dpa.test.aws.local.kms.KmsSettings;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local AWS configuration properties.
 */
@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties (prefix = "aws")
public class AwsSettings {

    private String testAccessKey;
    private String testSecretKey;
    private String testDefaultRegion;
    private String testAccountId;
    private KinesisSettings kinesis;
    private KmsSettings kms;
}
