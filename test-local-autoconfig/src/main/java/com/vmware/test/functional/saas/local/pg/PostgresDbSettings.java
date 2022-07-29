/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.pg;

import com.vmware.test.functional.saas.local.GenericDbmsSettings;

import lombok.Builder;
import lombok.Data;

/**
 * Local Postgres Db configuration settings.
 */
@Builder
@Data
public class PostgresDbSettings implements GenericDbmsSettings {

    private String dbName;
}
