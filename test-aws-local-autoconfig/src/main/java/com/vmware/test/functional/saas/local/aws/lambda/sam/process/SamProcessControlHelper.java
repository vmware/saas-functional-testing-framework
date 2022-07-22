/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * SamProcessControl helper class.
 */
@Slf4j
final class SamProcessControlHelper {

    static final Pattern SAM_VERSION_PATTER = Pattern.compile("SAM CLI, version (?<version>\\d+.\\d+.\\d+)");
    static final String EXPECTED_SAM_VERSION = "1.32.0";
    static final String DIFFERENT_VERSIONS_MESSAGE_FORMAT = "Expected Sam version is [" + EXPECTED_SAM_VERSION + "], actual version is [%s]."
            + " This may lead to unpredictable behaviour.";
    static final String OUTPUT_MISMATCH_MESSAGE_FORMAT = "Expected output format: [" + SAM_VERSION_PATTER.pattern() + "], actual output was [%s]."
            + " Verify that your sam version is [" + EXPECTED_SAM_VERSION + "].";

    private SamProcessControlHelper() {
    }

    static void verifySamVersion(final String samCommandOutput) {
        log.info("Verifying Sam version");
        final Matcher matcher = SAM_VERSION_PATTER.matcher(samCommandOutput);
        final boolean outputMatches = matcher.find();
        if (outputMatches && !EXPECTED_SAM_VERSION.equals(matcher.group("version"))) {
            log.warn(String.format(DIFFERENT_VERSIONS_MESSAGE_FORMAT, matcher.group("version")));
        } else if (!outputMatches) {
            throw new RuntimeException(
                    String.format(OUTPUT_MISMATCH_MESSAGE_FORMAT,
                            samCommandOutput));
        }
    }

}
