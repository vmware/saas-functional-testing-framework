/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.sqs;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local SQS queues creation.
 */
@Builder
@Data
public class SqsQueuesSpec {

    private List<String> queuesToCreate;
}

