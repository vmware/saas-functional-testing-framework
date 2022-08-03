/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import static com.vmware.test.functional.saas.local.aws.jdbc.TestRedshiftDriver.SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS;

/**
 * Exception handlers for jdbc methods invocations.
 */
public final class ExceptionUtils {

    private static final Pattern UNRECOGNIZED_CONFIG_OPTION_MESSAGE_PATTERN =
            Pattern.compile("unrecognized configuration parameter \\Q\"\\E(?<configOption>.+?)\\Q\"\\E");

    private ExceptionUtils() {
    }

    static boolean handleSqlException(final InvocationTargetException e, final Properties info) throws InvocationTargetException, SQLException {
        final List<String> configOptionsToSkip = Stream.of(info.getProperty(SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS, "").split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        if (e.getTargetException() instanceof PSQLException && !configOptionsToSkip.isEmpty()) {
            final ServerErrorMessage errorMessage = ((PSQLException)e.getTargetException()).getServerErrorMessage();
            if (errorMessage != null && "set_config_option".equals(errorMessage.getRoutine()) && errorMessage.getMessage() != null) {
                final Matcher m = UNRECOGNIZED_CONFIG_OPTION_MESSAGE_PATTERN.matcher(errorMessage.getMessage());
                if (m.find() && configOptionsToSkip.contains(m.group("configOption"))) {
                    return true;
                }
            }
        }
        if (e.getTargetException() instanceof SQLException) {
            throw (SQLException)e.getTargetException();
        }
        throw e;
    }
}
