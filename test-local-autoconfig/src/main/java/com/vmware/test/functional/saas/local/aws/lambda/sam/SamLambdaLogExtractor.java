/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.lambda.sam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vmware.test.functional.saas.aws.lambda.LambdaLogExtractor;
import com.vmware.test.functional.saas.aws.lambda.LambdaRequestContext;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sam lambda log extractor.
 */
@Slf4j
@AllArgsConstructor
class SamLambdaLogExtractor implements LambdaLogExtractor {

    private final String logFile;

    @Override
    public String getLambdaLogsForRequestContext(final LambdaRequestContext requestContext) {
        final StringBuilder lambdaLog = new StringBuilder();
        log.info("Extracting logs for LambdaRequestContext [{}] from log file [{}]", requestContext.getRequestId(), this.logFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(this.logFile, StandardCharsets.UTF_8))) {
            reader.lines()
                    .filter(line -> SamLambdaLogsHelper.isFromRequest(line, requestContext.getRequestId()))
                    .forEach(fLine -> lambdaLog.append(fLine).append("\n"));
        } catch (final IOException e) {
            throw new RuntimeException("Encountered IOException while working with lambda log file", e);
        }
        final String lambdaResultLog = lambdaLog.toString();
        log.debug("Lambda invocation result logs: {}", lambdaResultLog);
        return lambdaResultLog;
    }
}
