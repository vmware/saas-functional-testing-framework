/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple Test Lambda function used for testing local lambda autoconfiguration.
 * Lambda simply accepts a string and makes it uppercase.
 */
@Slf4j
public class TestLambda {

    /**
     * Handles Lambda requests.
     *
     * @param input Request input param
     * @return The values of the input map as array.
     */
    public Object handleRequest(final Map<String, Object> input) {
        log.info("std out sample");
        log.error("std error sample");
        log.info("Received input: {}", input);
        return input.values().toArray();
    }

}
