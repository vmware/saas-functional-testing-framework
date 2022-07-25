/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link SamProcessControlHelper}.
 */
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
}
