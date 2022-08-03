/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas;

import lombok.Getter;

/**
 * Enumerated class representing the type of services to be started locally. To be used by declaring functional tests service dependencies.
 */
// legacy model
@Getter
public enum Service {

    DYNAMO_DB,
    ELASTICSEARCH,
    KINESIS,
    KMS,
    LAMBDA,
    POSTGRES,
    TRINO,
    REDIS,
    REDSHIFT,
    S3,
    SES,
    SNS,
    SQS
}
