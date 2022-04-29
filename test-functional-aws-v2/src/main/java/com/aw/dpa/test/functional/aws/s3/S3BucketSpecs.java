/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.s3;

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
