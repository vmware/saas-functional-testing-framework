/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.vmware.test.functional.saas.local.aws.kinesis.KinesisSettings;
import com.vmware.test.functional.saas.local.aws.kms.KmsSettings;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local AWS configuration properties.
 */
@Data
@NoArgsConstructor
@ConfigurationProperties (prefix = "aws")
public class AwsSettings {

    private String testAccessKey;
    private String testSecretKey;
    private String testDefaultRegion;
    private String testAccountId;
    private KinesisSettings kinesis;
    private KmsSettings kms;
}
