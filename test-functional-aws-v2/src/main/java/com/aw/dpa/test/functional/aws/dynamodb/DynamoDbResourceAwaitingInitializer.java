/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.functional.aws.AbstractAwsResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies DynamoDb tables, provided by {@link DynamoDbTablesSpecs},
 * exist when started.
 */
@Slf4j
public class DynamoDbResourceAwaitingInitializer extends AbstractAwsResourceAwaitingInitializer {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbResourceAwaitingInitializer(final DynamoDbClient dynamoDbClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void doStart() {
        final List<DynamoDbTablesSpecs> dynamoDbTablesSpecs = new ArrayList<>(getContext().getBeansOfType(DynamoDbTablesSpecs.class).values());
        if (!dynamoDbTablesSpecs.isEmpty()) {
            log.debug("Verifying DDB table(s) exist using table specifications: {}", dynamoDbTablesSpecs);
            dynamoDbTablesSpecs.stream()
                    .map(DynamoDbTablesSpecs::getTablesToCreate)
                    .distinct()
                    .flatMap(Collection::stream)
                    .forEach(this::tableExists);
        }
    }

    private void tableExists(final DynamoDbTableSettings tableSettings) {
        await().until(() -> DynamoDbHealthHelper.checkHealth(this.dynamoDbClient, tableSettings.getTableName()));
        log.info("Verified DynamoDb Table [{}] exists", tableSettings.getTableName());
    }
}
