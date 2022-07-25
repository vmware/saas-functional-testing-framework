/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local.aws.kms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Local KMS configuration properties.
 */
@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "kms")
public class KmsSettings {

    private String masterKeyArn;
}
