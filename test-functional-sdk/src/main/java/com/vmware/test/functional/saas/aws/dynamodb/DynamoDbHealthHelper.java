/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * DynamoDb Health Helper.
 */
public final class DynamoDbHealthHelper {

    private DynamoDbHealthHelper() {

    }

    /**
     * DynamoDb Health Helper - verifying created queues.
     *
     * @param dynamoDbClient {@link software.amazon.awssdk.services.dynamodb.DynamoDbClient}.
     * @param tableName      The DynamoDb table name.
     * @return {@code true} if the table exists, else {@code false}.
     */
    public static boolean checkHealth(final DynamoDbClient dynamoDbClient, final String tableName) {
        try {
            final DescribeTableRequest table = DescribeTableRequest.builder().tableName(tableName).build();
            return dynamoDbClient.describeTable(table).sdkHttpResponse().isSuccessful();
        } catch (final ResourceNotFoundException e) {
            return false;
        }
    }
}
