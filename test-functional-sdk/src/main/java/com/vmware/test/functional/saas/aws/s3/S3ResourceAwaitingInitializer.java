/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.s3;

import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.common.AbstractResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies S3 buckets, provided by {@link S3BucketSpecs},
 * exist when started.
 */
@Slf4j
public class S3ResourceAwaitingInitializer extends AbstractResourceAwaitingInitializer {

    private final S3Client s3Client;

    public S3ResourceAwaitingInitializer(final S3Client s3Client,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.s3Client = s3Client;
    }

    @Override
    public void doStart() {
        final List<S3BucketSpecs> s3BucketSpecs = new ArrayList<>(getContext().getBeansOfType(S3BucketSpecs.class).values());
        if (!s3BucketSpecs.isEmpty()) {
            log.debug("Verifying S3 buckets exist from {}", s3BucketSpecs);
            s3BucketSpecs.stream()
                    .map(S3BucketSpecs::getBuckets)
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(this::verifyBucket);
        }
    }

    private void verifyBucket(final S3BucketSettings bucketSettings) {
        await().until(() -> S3HealthHelper.checkHealth(this.s3Client, bucketSettings.getName()));
        log.info("Verified S3 bucket [{}] exists", bucketSettings.getName());
    }

}
