/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;
import com.vmware.test.functional.saas.local.aws.AwsSettings;
import com.vmware.test.functional.saas.aws.kinesis.KinesisStreamsSpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link KinesisStreamsSpec kinesis streams}
 * when started.
 */
@Slf4j
public class KinesisResourceCreator extends AbstractResourceCreator {

    private final KinesisClient kinesisClient;
    private final KinesisSettings kinesisSettings;

    public KinesisResourceCreator(final KinesisClientFactory kinesisClientFactory, final AwsSettings awsSettings,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.kinesisClient = kinesisClientFactory.getObject();
        this.kinesisSettings = awsSettings.getKinesis();
    }

    @Override
    protected void doStart() {
        final List<KinesisStreamsSpec> kinesisStreamsSpecs = new ArrayList<>(getContext().getBeansOfType(KinesisStreamsSpec.class).values());
        if (!kinesisStreamsSpecs.isEmpty()) {
            initializeStream(kinesisStreamsSpecs);
        }
    }

    private void initializeStream(final List<KinesisStreamsSpec> kinesisStreamsSpecs) {
        if (log.isDebugEnabled()) {
            log.debug("Creating streams {}", kinesisStreamsSpecs.stream()
                    .map(KinesisStreamsSpec::getStreamsToCreate)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        final List<String> activeStreams = this.kinesisClient.listStreams().streamNames();
        kinesisStreamsSpecs.stream()
                .map(KinesisStreamsSpec::getStreamsToCreate)
                .flatMap(Collection::stream)
                .distinct()
                .filter(((Predicate<String>)activeStreams::contains).negate())
                .forEach(this::createStream);
    }

    private void createStream(final String streamName) {
        final CreateStreamRequest createStreamRequest = CreateStreamRequest.builder()
                .streamName(streamName)
                .shardCount(this.kinesisSettings.getShardCount())
                .build();
        this.kinesisClient.createStream(createStreamRequest);
        log.info("Stream [{}] created", streamName);
    }
}
