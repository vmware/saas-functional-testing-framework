/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sqs;

/**
 * Simplified Sqs operations.
 */
public interface SqsService {

    /**
     * Get the SQS Queue url.
     * @param queueName the queue name
     * @return Queue url as {@link String}
     */
    String getSqsQueueUrl(String queueName);

}
