/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local;

import org.testng.annotations.Test;

import com.aw.dpa.test.AbstractFunctionalTests;

import lombok.extern.slf4j.Slf4j;

/**
 * This test verifies that if a AWS service (i.e. Service.KINESIS) is NOT specified in a test context configuration,
 * then the required client needed for working with the service is NOT created.
 */
@Slf4j
public class TestTest extends AbstractFunctionalTests {

    @Test
    public void test() {
        log.info("lalala");
    }
}
