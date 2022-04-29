/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.redshift;

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
