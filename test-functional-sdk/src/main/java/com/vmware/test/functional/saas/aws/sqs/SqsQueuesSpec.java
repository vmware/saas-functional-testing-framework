/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sqs;

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

