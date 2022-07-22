/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.AbstractFullContextTest;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbHealthHelper;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for Dynamo DB configuration.
 */
@Test(groups = "explicitOrderTestClass")
public class DynamoDbTest extends AbstractFullContextTest {

    @Value("${AWS_DYNAMODB_TEST_TABLE}")
    private String dynamoDbTableName;

    @Value("${AWS_DYNAMODB_TEST_TABLE_WITH_GSI}")
    private String dynamoDbTableWithIndexName;

    @Value("${AWS_DYNAMODB_TEST_TABLE_PARTITION_KEY}")
    private String dynamoDbTablePartitionKey;

    @Value("${AWS_DYNAMODB_TEST_INDEX_NAME}")
    private String dynamoDbIndexName;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Test
    public void localDynamoDbAutoconfiguration() {
        // verify table was created by DynamoDbInitializer
        assertThat(DynamoDbHealthHelper.checkHealth(this.dynamoDbClient, this.dynamoDbTableName), is(true));

        final String attributeValue = "test";
        this.dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(this.dynamoDbTableName)
                .item(Map.of(
                        this.dynamoDbTablePartitionKey,
                        AttributeValue.builder().s(attributeValue).build()))
                .build());

        final GetItemResponse getItemResponse = this.dynamoDbClient.getItem(
                GetItemRequest.builder()
                        .tableName(this.dynamoDbTableName)
                        .key(Map.of(this.dynamoDbTablePartitionKey,
                                AttributeValue.builder().s(attributeValue).build()))
                        .build());
        assertThat(getItemResponse.sdkHttpResponse().isSuccessful(), is(true));
        assertThat(getItemResponse.item().size(), is(1));
        assertThat((getItemResponse.item().entrySet().iterator().next()).getValue().s(), is(attributeValue));
    }

    @Test
    public void localDynamoDbAutoconfigurationWithIndex() {
        // verify table was created by DynamoDbInitializer
        assertThat(DynamoDbHealthHelper.checkHealth(this.dynamoDbClient, this.dynamoDbTableName), is(true));

        final String attributeValue = "test";
        final String indexValue = "indexValue";
        this.dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(this.dynamoDbTableWithIndexName)
                .item(Map.of(
                        this.dynamoDbTablePartitionKey,
                        AttributeValue.builder().s(attributeValue).build(),
                        this.dynamoDbIndexName,
                        AttributeValue.builder().s(indexValue).build()))
                .build());
        final QueryResponse queryResponse = this.dynamoDbClient.query(QueryRequest.builder()
                .tableName(this.dynamoDbTableWithIndexName)
                .indexName(this.dynamoDbIndexName)
                .keyConditions(Map.of(this.dynamoDbIndexName,
                        Condition.builder()
                                .attributeValueList(AttributeValue.builder()
                                        .s(indexValue)
                                        .build())
                                .comparisonOperator(ComparisonOperator.EQ)
                                .build()))
                .build());
        assertThat(queryResponse.sdkHttpResponse().isSuccessful(), is(true));
        assertThat(queryResponse.items().size(), is(1));
        assertThat(queryResponse.items().get(0).keySet().containsAll(ImmutableList.of(this.dynamoDbIndexName, this.dynamoDbTablePartitionKey)), is(true));
        assertThat(queryResponse.items().get(0).get(this.dynamoDbIndexName).s(), is(indexValue));
        assertThat(queryResponse.items().get(0).get(this.dynamoDbTablePartitionKey).s(), is(attributeValue));
    }
}
