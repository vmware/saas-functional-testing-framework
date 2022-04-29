/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.aws.local.AbstractAwsResourceCreator;
import com.aw.dpa.test.aws.local.AwsSettings;
import com.aw.dpa.test.functional.aws.kinesis.KinesisStreamsSpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link KinesisStreamsSpec kinesis streams}
 * when started.
 */
@Slf4j
public class KinesisResourceCreator extends AbstractAwsResourceCreator {

    private final KinesisClient kinesisClient;
    private final KinesisSettings kinesisSettings;

    public KinesisResourceCreator(final KinesisFactory kinesisFactory, final AwsSettings awsSettings,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.kinesisClient = kinesisFactory.getObject();
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
