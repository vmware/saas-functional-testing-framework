/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.s3;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Configuration for local s3 buckets creation.
 */
@Builder
@Data
public class S3BucketSpecs {

    @Singular
    private List<S3BucketSettings> buckets;
}
