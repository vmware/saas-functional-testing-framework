/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sqs;

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
