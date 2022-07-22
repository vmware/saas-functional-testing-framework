/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeyType;

import java.util.Collection;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Local DynamoDb table configuration settings.
 */
@Builder
@Data
public class DynamoDbTableSettings {

    private String tableName;
    private AttributeDefinition key;
    private KeyType keyType;
    private Long provisionedThroughputReadCapacity;
    private Long provisionedThroughputWriteCapacity;
    private List<GlobalSecondaryIndex> globalSecondaryIndexes;
    private Collection<AttributeDefinition> attributeDefinitions;
}
