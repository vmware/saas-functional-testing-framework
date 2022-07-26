/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Local Service Constants.
 */
// legacy model
public final class LocalServiceConstants {

    private LocalServiceConstants() {
    }
    public static final String LOCALSTACK_VERSION = "0.12.5";

    public static final String LOCALSTACK_IMAGE_NAME = String.format("localstack/localstack:%s", LOCALSTACK_VERSION);

    public static final int LOCALSTACK_DEFAULT_SERVICE_PORT = 4566;
    /**
     * Bean names.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Components {

        public static final String DYNAMODB_ENDPOINT = "dynamoDbEndpoint";
        public static final String ELASTICSEARCH_ENDPOINT = "elasticsearchEndpoint";
        public static final String KINESIS_ENDPOINT = "kinesisEndpoint";
        public static final String KMS_ENDPOINT = "kmsEndpoint";
        public static final String LAMBDA_ENDPOINT = "lambdaEndpoint";
        public static final String LOCALSTACK_ENDPOINT = "localStackEndpoint";
        public static final String POSTGRES_ENDPOINT = "postgresEndpoint";
        public static final String TRINO_ENDPOINT = "trinoEndpoint";
        public static final String REDIS_ENDPOINT = "redisEndpoint";
        public static final String REDSHIFT_ENDPOINT = "redshiftEndpoint";
        public static final String S3_ENDPOINT = "s3Endpoint";
        public static final String SES_ENDPOINT = "sesEndpoint";
        public static final String SNS_ENDPOINT = "snsEndpoint";
        public static final String SQS_ENDPOINT = "sqsEndpoint";
    }
}
