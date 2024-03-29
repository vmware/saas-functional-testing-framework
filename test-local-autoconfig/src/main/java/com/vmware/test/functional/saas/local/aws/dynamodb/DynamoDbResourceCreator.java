/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbTableSettings;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbTablesSpecs;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link DynamoDbTablesSpecs} DynamoDb tables
 * when started.
 */
@Slf4j
public class DynamoDbResourceCreator extends AbstractResourceCreator {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbResourceCreator(final DynamoDbClientFactory dynamoDbClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.dynamoDbClient = dynamoDbClientFactory.getObject();
    }

    @Override
    protected void doStart() {
        final List<DynamoDbTablesSpecs> dynamoDbTablesSpecs = new ArrayList<>(getContext().getBeansOfType(DynamoDbTablesSpecs.class).values());
        if (!dynamoDbTablesSpecs.isEmpty()) {
            initializeTables(dynamoDbTablesSpecs);
        }
    }

    private void initializeTables(final List<DynamoDbTablesSpecs> dynamoDbTablesSpecs) {
        if (log.isDebugEnabled()) {
            log.debug("Creating table(s) using table specifications: {}", dynamoDbTablesSpecs);
        }

        final List<String> tableNames = this.dynamoDbClient.listTables().tableNames();
        dynamoDbTablesSpecs.stream()
                .map(DynamoDbTablesSpecs::getTablesToCreate)
                .distinct()
                .flatMap(Collection::stream)
                .filter(dynamoDbTableSettings -> !tableNames.contains(dynamoDbTableSettings.getTableName()))
                .forEach(this::createTable);
    }

    private void createTable(final DynamoDbTableSettings tableSettings) {
        final CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(tableSettings.getTableName())
                .attributeDefinitions(tableSettings.getAttributeDefinitions())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(tableSettings.getKey().attributeName())
                        .keyType(tableSettings.getKeyType())
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(tableSettings.getProvisionedThroughputReadCapacity())
                        .writeCapacityUnits(tableSettings.getProvisionedThroughputWriteCapacity())
                        .build())
                .globalSecondaryIndexes(tableSettings.getGlobalSecondaryIndexes())
                .build();

        this.dynamoDbClient.createTable(createTableRequest);
        log.info("DynamoDb Table [{}] created", tableSettings.getTableName());
    }
}
