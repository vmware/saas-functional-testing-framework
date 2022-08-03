/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.util.Optional;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for redshift {@code COPY} commands unit tests.
 */
public class CopyCommandTest {

    @DataProvider
    Object[][] copyCommands() {
        return new Object[][] {
                { "COPY table FROM 's3://a/b' IAM_ROLE 'arn:aws:iam::a:role/a' format as json 'auto' GZIP manifest",
                        new CopyCommand(null,
                            "table",
                            "s3://a/b",
                            true,
                            true,
                            true),
                },
                { "copy table FROM 's3://a/b' IAM_ROLE 'arn:aws:iam::a:role/a' FORMAT as JSON 'auto' gzip Manifest",
                        new CopyCommand(null,
                                "table",
                                "s3://a/b",
                                true,
                                true,
                                true),
                },
                { "COPY table FROM 's3://a/b' IAM_ROLE 'arn:aws:iam::a:role/a' GZIP format as json 'auto'",
                        new CopyCommand(null,
                                "table",
                                "s3://a/b",
                                true,
                                true,
                                false),
                },
        };
    }

    @Test(dataProvider = "copyCommands")
    void verifyParse(final String copyCommandStr, final CopyCommand copyCommand) {
        final Optional<CopyCommand> result = CopyCommand.parse(copyCommandStr, null);
        assertThat("result is present", result.isPresent());
        assertThat(copyCommand.isJsonFormatRequired(), is(result.get().isJsonFormatRequired()));
        assertThat(copyCommand.isGzipCompressionRequired(), is(result.get().isGzipCompressionRequired()));
        assertThat(copyCommand.isManifestRequired(), is(result.get().isManifestRequired()));
    }
}
