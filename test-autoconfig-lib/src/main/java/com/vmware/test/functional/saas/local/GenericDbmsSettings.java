/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

/**
 * Generic data base management system configuration.
 */
public interface GenericDbmsSettings {

    /**
     * Database name.
     *
     * @return database name.
     */
    String getDbName();
}
