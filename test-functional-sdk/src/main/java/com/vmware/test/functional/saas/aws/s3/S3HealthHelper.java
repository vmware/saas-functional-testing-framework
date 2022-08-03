/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.s3;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Kinesis Health Helper.
 */
public final class S3HealthHelper {

    private S3HealthHelper() {

    }

    /**
     * S3 Health Helper - verifying a bucket exists and is accessible to current user.
     * @param s3Client the {@link S3Client}.
     * @param bucketName the bucket name.
     * @return {@code True} if the bucket exists and is accessible, {@code False} otherwise
     */
    public static boolean checkHealth(final S3Client s3Client, final String bucketName) {
        try {
            final HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
            return s3Client.headBucket(headBucketRequest).sdkHttpResponse().isSuccessful();
        } catch (final NoSuchBucketException e) {
            return false;
        }
    }
}
