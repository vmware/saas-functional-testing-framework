/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
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
    PRESTO,
    REDIS,
    REDSHIFT,
    S3,
    SES,
    SNS,
    SQS
}
