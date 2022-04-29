/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.redshift;

import com.aw.dpa.test.aws.local.dbms.GenericDbmsSettings;

import lombok.Builder;
import lombok.Data;

/**
 * Local Redshift Db configuration settings.
 */
@Builder
@Data
public class RedshiftDbSettings implements GenericDbmsSettings {

    private String dbName;
}
