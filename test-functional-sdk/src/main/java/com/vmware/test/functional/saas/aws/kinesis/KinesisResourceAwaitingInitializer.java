/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.common.AbstractResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies kinesis streams, provided by {@link KinesisStreamsSpec},
 * exist when started.
 */
@Slf4j
public class KinesisResourceAwaitingInitializer extends AbstractResourceAwaitingInitializer {

    private final KinesisClient kinesisClient;

    public KinesisResourceAwaitingInitializer(final KinesisClient kinesisClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.kinesisClient = kinesisClient;
    }

    @Override
    public void doStart() {
        final List<KinesisStreamsSpec> kinesisStreamsSpecs = new ArrayList<>(getContext().getBeansOfType(KinesisStreamsSpec.class).values());
        if (!kinesisStreamsSpecs.isEmpty()) {
            log.debug("Verifying Kinesis streams exist from {}", kinesisStreamsSpecs);
            kinesisStreamsSpecs.stream()
                    .map(KinesisStreamsSpec::getStreamsToCreate)
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(this::verifyStream);
        }
    }

    private void verifyStream(final String streamName) {
        await().until(() -> KinesisHealthHelper.checkHealth(this.kinesisClient, streamName));
        log.info("Verified Kinesis stream [{}] exists", streamName);
    }
}
