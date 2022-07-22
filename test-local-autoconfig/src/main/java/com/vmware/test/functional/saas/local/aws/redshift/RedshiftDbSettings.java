/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.redshift;

import com.vmware.test.functional.saas.local.GenericDbmsSettings;

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
