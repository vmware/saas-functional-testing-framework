/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.vmware.test.functional.saas.FunctionalTest;

/**
 * Tests for {@link SamProcessControlHelper}.
 */
@FunctionalTest
@ContextHierarchy(@ContextConfiguration(classes = SamProcessControlHelperTest.LocalConfig.class))
public class SamProcessControlHelperTest extends AbstractTestNGSpringContextTests {

    @Test
    public void verifySamVersionDoesNotThrowWhenSameVersion() {
        SamProcessControlHelper.verifySamVersion("SAM CLI, version " + SamProcessControlHelper.EXPECTED_SAM_VERSION);
    }

    @Test
    public void verifySamVersionThrowsWhenDifferentVersion() {
        final String wrongVersion = "SAM CLI, version 0.0.0";
        try {
            SamProcessControlHelper.verifySamVersion(wrongVersion);
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage(), equalTo(String.format(SamProcessControlHelper.DIFFERENT_VERSIONS_MESSAGE_FORMAT, wrongVersion)));
        }
    }

    @Test
    public void verifySamVersionThrowsWhenWrongOutputFormat() {
        final String wrongVersion = "Sam version " + SamProcessControlHelper.EXPECTED_SAM_VERSION;
        try {
            SamProcessControlHelper.verifySamVersion(wrongVersion);
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage(), equalTo(String.format(SamProcessControlHelper.OUTPUT_MISMATCH_MESSAGE_FORMAT, wrongVersion)));
        }
    }
    static class LocalConfig {
    }
}
