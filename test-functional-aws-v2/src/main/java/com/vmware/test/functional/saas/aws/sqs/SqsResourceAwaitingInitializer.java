/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.common.AbstractAwsResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies SQS queues, provided by {@link SqsQueuesSpec SQS queues},
 * exist when started.
 */
@Slf4j
public class SqsResourceAwaitingInitializer extends AbstractAwsResourceAwaitingInitializer {

    private final SqsClient sqsClient;

    public SqsResourceAwaitingInitializer(final SqsClient sqsClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.sqsClient = sqsClient;
    }

    @Override
    public void doStart() {
        final List<SqsQueuesSpec> sqsQueuesSpecs = new ArrayList<>(getContext().getBeansOfType(SqsQueuesSpec.class).values());
        if (!sqsQueuesSpecs.isEmpty()) {
            log.debug("Verifying SQS queues exist from {}", sqsQueuesSpecs);
            sqsQueuesSpecs.stream()
                    .map(SqsQueuesSpec::getQueuesToCreate)
                    .flatMap(Collection::stream)
                    .forEach(this::verifyQueue);
        }
    }

    private void verifyQueue(final String queueName) {
        await().until(() -> SqsHealthHelper.checkHealth(this.sqsClient, queueName));
        log.info("Verified SQS Queue [{}] exists", queueName);
    }
}
