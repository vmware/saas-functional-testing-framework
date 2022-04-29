/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.lambda.sam;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.aw.dpa.test.functional.aws.lambda.LambdaLogExtractor;
import com.aw.dpa.test.functional.aws.lambda.LambdaRequestContext;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.aw.dpa.test.aws.local.lambda.sam.SamLambdaLogsHelper.isFromRequest;

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
                    .filter(line -> isFromRequest(line, requestContext.getRequestId()))
                    .forEach(fLine -> lambdaLog.append(fLine).append("\n"));
        } catch (final IOException e) {
            throw new RuntimeException("Encountered IOException while working with lambda log file", e);
        }
        final String lambdaResultLog = lambdaLog.toString();
        log.debug("Lambda invocation result logs: {}", lambdaResultLog);
        return lambdaResultLog;
    }
}
