/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.pg;

import com.vmware.test.functional.saas.aws.local.dbms.GenericDbmsSettings;

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
