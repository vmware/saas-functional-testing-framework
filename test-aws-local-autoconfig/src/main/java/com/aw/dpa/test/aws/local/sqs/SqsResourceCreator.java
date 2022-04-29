/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.aws.local.AbstractAwsResourceCreator;
import com.aw.dpa.test.functional.aws.sqs.SqsQueuesSpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link SqsQueuesSpec SQS queues}
 * when started.
 */
@Slf4j
public class SqsResourceCreator extends AbstractAwsResourceCreator {

    private final SqsClient sqsClient;

    public SqsResourceCreator(final SQSFactory sqsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.sqsClient = sqsFactory.getObject();
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

