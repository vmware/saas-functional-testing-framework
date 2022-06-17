/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.dbms;

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
