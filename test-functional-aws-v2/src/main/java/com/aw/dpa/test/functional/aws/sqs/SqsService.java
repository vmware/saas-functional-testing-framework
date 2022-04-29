/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.sqs;

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
