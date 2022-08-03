/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;
import com.vmware.test.functional.saas.aws.sqs.SqsQueuesSpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link SqsQueuesSpec SQS queues}
 * when started.
 */
@Slf4j
public class SqsResourceCreator extends AbstractResourceCreator {

    private final SqsClient sqsClient;

    public SqsResourceCreator(final SQSClientFactory sqsClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.sqsClient = sqsClientFactory.getObject();
    }

    @Override
    public void doStart() {
        final List<SqsQueuesSpec> sqsQueuesSpecs = new ArrayList<>(getContext().getBeansOfType(SqsQueuesSpec.class).values());
        if (!sqsQueuesSpecs.isEmpty()) {
            initializeQueues(sqsQueuesSpecs);
        }
    }

    private void initializeQueues(final List<SqsQueuesSpec> sqsQueuesSpecs) {
        if (log.isDebugEnabled()) {
            log.debug("Creating queues {}", sqsQueuesSpecs.stream()
                    .map(SqsQueuesSpec::getQueuesToCreate)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }
        final List<String> queues = this.sqsClient.listQueues().queueUrls();
        sqsQueuesSpecs.stream()
                .map(SqsQueuesSpec::getQueuesToCreate)
                .flatMap(Collection::stream)
                .distinct()
                .filter(queueName -> queues.stream().noneMatch(t -> StringUtils.endsWith(t, queueName)))
                .forEach(this::createQueue);
    }

    private void createQueue(final String queueName) {
        final CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName).build();

        this.sqsClient.createQueue(createQueueRequest);
        log.info("Queue [{}] created", queueName);
    }
}

