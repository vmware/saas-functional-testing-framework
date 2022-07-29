/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.s3;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;
import com.vmware.test.functional.saas.local.aws.AwsSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSpecs;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link S3BucketSpecs} buckets
 * when started.
 */
@Slf4j
public class S3ResourceCreator extends AbstractResourceCreator {

    private final AwsSettings awsSettings;
    private final S3Client s3Client;

    public S3ResourceCreator(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final AwsSettings awsSettings,
            final S3Client s3Client) {
        super(functionalTestExecutionSettings);
        this.awsSettings = awsSettings;
        this.s3Client = s3Client;
    }

    @Override
    protected void doStart() {
        final List<S3BucketSpecs> s3BucketSpecs = new ArrayList<>(getContext().getBeansOfType(S3BucketSpecs.class).values());
        if (!s3BucketSpecs.isEmpty()) {
            initializeBuckets(s3BucketSpecs);
        }
    }

    private void initializeBuckets(final List<S3BucketSpecs> s3BucketSpecs) {
        final List<String> bucketsNames = this.s3Client.listBuckets().buckets().stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
        s3BucketSpecs.stream()
                .map(S3BucketSpecs::getBuckets)
                .flatMap(Collection::stream)
                .distinct()
                .filter(s3Bucket -> !bucketsNames.contains(s3Bucket.getName()))
                .forEach(this::create);
    }

    private void create(final S3BucketSettings bucket) {
        final Region region = Region.of(this.awsSettings.getTestDefaultRegion());
        // Create bucket
        final CreateBucketRequest createBucketRequest = CreateBucketRequest
                .builder()
                .bucket(bucket.getName())
                .createBucketConfiguration(CreateBucketConfiguration.builder()
                        .locationConstraint(region.id())
                        .build())
                .build();
        this.s3Client.createBucket(createBucketRequest);
        log.info("Bucket [{}] created", bucket.getName());
    }

}
