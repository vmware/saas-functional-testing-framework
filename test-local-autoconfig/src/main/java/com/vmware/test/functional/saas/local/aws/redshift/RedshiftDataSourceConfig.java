/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.redshift;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * Simple DataSource config to be used by {@link RedshiftDataSourceFactory}.
 */
@Builder
@Data
public class RedshiftDataSourceConfig {

    private String driverClassName;
    private String jdbcUrlFormat;
    private Map<String, Object> additionalDatasourceConfig;
}
