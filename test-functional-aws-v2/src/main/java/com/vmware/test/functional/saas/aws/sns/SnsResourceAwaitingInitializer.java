/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.sns;

import software.amazon.awssdk.services.sns.SnsClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.aws.AbstractAwsResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies SNS topics, provided by {@link SnsTopicsSpecs},
 * exist when started.
 */
@Slf4j
public class SnsResourceAwaitingInitializer extends AbstractAwsResourceAwaitingInitializer {

    private final SnsClient snsClient;

    public SnsResourceAwaitingInitializer(final SnsClient snsClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.snsClient = snsClient;
    }

    @Override
    public void doStart() {
        final List<SnsTopicsSpecs> snsTopicsSpecs = new ArrayList<>(getContext().getBeansOfType(SnsTopicsSpecs.class).values());
        if (!snsTopicsSpecs.isEmpty()) {
            log.debug("Verifying SNS topics exist from {}", snsTopicsSpecs);
            snsTopicsSpecs.stream()
                    .map(SnsTopicsSpecs::getTopicsToCreate)
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(this::verifyTopic);
        }
    }

    private void verifyTopic(final String topicName) {
        await().until(() -> SnsHealthHelper.checkHealth(this.snsClient, topicName));
        log.info("Verified SNS topic [{}] exists", topicName);
    }
}
