/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.aw.dpa.test.aws.local.jdbc.TestRedshiftDriver.SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@code ExceptionUtils}.
 */
public class ExceptionUtilsTest {

    @DataProvider
    Object[][] suppressedExceptions() {
        return new Object[][] {
                { new ServerErrorMessage("SERROR VERROR C42704 Munrecognized configuration parameter \"query_group\" Fguc.c L5968 Rset_config_option  "),
                        ConfigurationConverter.getProperties(new MapConfiguration(Map.of(
                                SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS, "query_group"
                        ))),
                },
        };
    }

    @DataProvider
    Object[][] propagatedExceptions() {
        return new Object[][] {
                { new ServerErrorMessage("SERROR VERROR C42704 Munrecognized configuration parameter \"query_group\" Fguc.c L5968 Rset_config_option  "),
                        new Properties(),
                },
                { new ServerErrorMessage("SERROR VERROR C42704 Munrecognized configuration parameter \"other_opt\" Fguc.c L5968 Rset_config_option  "),
                        ConfigurationConverter.getProperties(new MapConfiguration(Map.of(
                                SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS, "query_group"
                        ))),
                },
        };
    }

    @Test(dataProvider = "suppressedExceptions")
    void verifySuppressedExceptions(final ServerErrorMessage msg, final Properties info) throws InvocationTargetException, SQLException {
        final boolean result = ExceptionUtils.handleSqlException(new InvocationTargetException(new PSQLException(msg)), info);
        assertThat(result, is(true));
    }

    @Test(dataProvider = "propagatedExceptions", expectedExceptions = SQLException.class)
    void verifyPropagatedExceptions(final ServerErrorMessage msg, final Properties info) throws InvocationTargetException, SQLException {
        ExceptionUtils.handleSqlException(new InvocationTargetException(new PSQLException(msg)), info);
    }
}
