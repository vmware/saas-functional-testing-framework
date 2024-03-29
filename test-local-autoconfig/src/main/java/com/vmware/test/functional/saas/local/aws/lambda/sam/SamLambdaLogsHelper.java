/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.lambda.sam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class with common Sam lambda log related methods.
 */
final class SamLambdaLogsHelper {

    public static final String TEST_AWS_LOCAL_REQUEST_ID = "requestId";
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile("\\Q[\\E(?<" + TEST_AWS_LOCAL_REQUEST_ID + ">.+?)\\Q]\\E");

    private SamLambdaLogsHelper() { }

    public static boolean isFromRequest(final String logLine, final String requestId) {
        final Matcher matcher = LOG_LINE_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return requestId.equals(matcher.group(TEST_AWS_LOCAL_REQUEST_ID));
        }
        return false;
    }

}
