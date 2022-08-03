/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sqs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

/**
 * SQS Health Helper.
 */
public final class SqsHealthHelper {

    private SqsHealthHelper() {

    }

    /**
     * SQS Health Helper - verifying created queues.
     *
     * @param sqsClient {@link SqsClient}.
     * @param queueName The SQS queue name.
     * @return {@code true} if the queue exists, else {@code false}.
     */
    public static boolean checkHealth(final SqsClient sqsClient, final String queueName) {
        try {

            final GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName).build();

            return sqsClient.getQueueUrl(getQueueUrlRequest).sdkHttpResponse().isSuccessful();
        } catch (final QueueDoesNotExistException e) {
            return false;
        }
    }
}
