/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas;

import lombok.Getter;

import static com.vmware.test.functional.saas.ServiceConstants.Components.DYNAMODB_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.ELASTICSEARCH_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.KINESIS_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.KMS_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.LAMBDA_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.POSTGRES_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.REDIS_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.REDSHIFT_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.S3_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.SES_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.SNS_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.SQS_ENDPOINT;
import static com.vmware.test.functional.saas.ServiceConstants.Components.TRINO_ENDPOINT;

/**
 * Enumerated class representing the type of services to be started locally. To be used by declaring functional tests service dependencies.
 */
// legacy model
@Getter
public enum Service {

    DYNAMO_DB(DYNAMODB_ENDPOINT),
    ELASTICSEARCH(ELASTICSEARCH_ENDPOINT),
    KINESIS(KINESIS_ENDPOINT),
    KMS(KMS_ENDPOINT),
    LAMBDA(LAMBDA_ENDPOINT),
    POSTGRES(POSTGRES_ENDPOINT),
    TRINO(TRINO_ENDPOINT),
    REDIS(REDIS_ENDPOINT),
    REDSHIFT(REDSHIFT_ENDPOINT),
    S3(S3_ENDPOINT),
    SES(SES_ENDPOINT),
    SNS(SNS_ENDPOINT),
    SQS(SQS_ENDPOINT);

    private final String endpointName;

    Service(String endpointName) {
        this.endpointName = endpointName;
    }
}
