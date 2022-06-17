/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Local Service Constants.
 */
public final class LocalServiceConstants {

    private LocalServiceConstants() {
    }

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
        public static final String PRESTO_ENDPOINT = "prestoEndpoint";
        public static final String REDIS_ENDPOINT = "redisEndpoint";
        public static final String REDSHIFT_ENDPOINT = "redshiftEndpoint";
        public static final String S3_ENDPOINT = "s3Endpoint";
        public static final String SES_ENDPOINT = "sesEndpoint";
        public static final String SNS_ENDPOINT = "snsEndpoint";
        public static final String SQS_ENDPOINT = "sqsEndpoint";
    }
}
