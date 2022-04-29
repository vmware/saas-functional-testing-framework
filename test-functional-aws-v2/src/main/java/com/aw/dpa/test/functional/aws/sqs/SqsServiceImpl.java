/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;

/**
 * Implementation of {@link SqsService}.
 */
public class SqsServiceImpl implements SqsService {

    private final SqsClient sqsClient;

    public SqsServiceImpl(final SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public String getSqsQueueUrl(final String queueName) {
        final GetQueueUrlRequest req = GetQueueUrlRequest.builder().queueName(queueName).build();
        return this.sqsClient.getQueueUrl(req).queueUrl();
    }
}
