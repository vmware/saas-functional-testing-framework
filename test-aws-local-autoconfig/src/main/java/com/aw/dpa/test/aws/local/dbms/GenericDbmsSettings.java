/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.dbms;

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
