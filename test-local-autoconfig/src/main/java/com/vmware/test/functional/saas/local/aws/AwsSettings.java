/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.vmware.test.functional.saas.local.aws.kinesis.KinesisSettings;
import com.vmware.test.functional.saas.local.aws.kms.KmsSettings;

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
