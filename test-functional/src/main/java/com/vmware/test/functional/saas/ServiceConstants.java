/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Service Constants.
 */
// legacy model
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServiceConstants {
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
