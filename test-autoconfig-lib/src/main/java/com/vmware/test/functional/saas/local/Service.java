/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import lombok.Getter;

import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.DYNAMODB_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.ELASTICSEARCH_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.KINESIS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.KMS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.LAMBDA_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.POSTGRES_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.PRESTO_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.REDIS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.REDSHIFT_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.S3_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SES_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SNS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SQS_ENDPOINT;


/**
 * Enumerated class representing the type of services to be started locally. To be used by declaring functional tests service dependencies.
 */
// legacy model
// move to test-functional
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
