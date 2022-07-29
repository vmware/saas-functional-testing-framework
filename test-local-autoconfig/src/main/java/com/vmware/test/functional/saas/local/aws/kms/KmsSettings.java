/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
