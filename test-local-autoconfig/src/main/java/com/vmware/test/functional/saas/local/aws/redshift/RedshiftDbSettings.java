/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
