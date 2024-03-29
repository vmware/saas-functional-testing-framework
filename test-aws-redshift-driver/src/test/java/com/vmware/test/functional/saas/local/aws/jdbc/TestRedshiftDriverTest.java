/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link TestRedshiftDriver}.
 */
public class TestRedshiftDriverTest {

    @Test
    public void registerDriver() {
        // Driver is initially registered because it is automatically done when class is loaded
        assertThat(TestRedshiftDriver.class.getName() + " driver was not successfully registered but was expected to.",
                TestRedshiftDriver.isRegistered(), is(true));

        final ArrayList<Driver> drivers = Collections.list(DriverManager.getDrivers());
        int registeredRedshiftDriversCount = 0;
        for (java.sql.Driver driver : drivers) {
            if (driver instanceof TestRedshiftDriver) {
                registeredRedshiftDriversCount++;
            }
        }
        assertThat("Unexpected number of " + TestRedshiftDriver.class.getName() + " drivers are registered.",
                registeredRedshiftDriversCount, is(1));
    }

}
