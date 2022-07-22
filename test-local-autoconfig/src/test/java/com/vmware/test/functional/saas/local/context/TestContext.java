/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.context;

import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.local.pg.PostgresDbSettings;
import com.vmware.test.functional.saas.local.aws.redshift.RedshiftDbSettings;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbTableSettings;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbTablesSpecs;
import com.vmware.test.functional.saas.es.ElasticsearchIndexBuildConfiguration;
import com.vmware.test.functional.saas.es.ElasticsearchIndexSettings;
import com.vmware.test.functional.saas.aws.kinesis.KinesisStreamsSpec;
import com.vmware.test.functional.saas.trino.TrinoCatalogSettings;
import com.vmware.test.functional.saas.trino.TrinoCatalogSpecs;
import com.vmware.test.functional.saas.aws.s3.S3BucketSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSpecs;
import com.vmware.test.functional.saas.aws.sns.SnsTopicsSpecs;
import com.vmware.test.functional.saas.aws.sqs.SqsQueuesSpec;

public class TestContext {

    @Configuration
    @ServiceDependencies(Service.REDIS)
    public static class StartRedisServiceContext {

    }

    @Configuration
    @ServiceDependencies(Service.KMS)
    public static class StartKmsServiceContext {

    }

    @Configuration
    public static class EmptyTestContext {

    }

    @Configuration
    @ServiceDependencies({
            Service.ELASTICSEARCH,
            Service.DYNAMO_DB,
            Service.KINESIS,
            Service.KMS,
            Service.LAMBDA,
            Service.POSTGRES,
            Service.TRINO,
            Service.REDIS,
            Service.REDSHIFT,
            Service.S3,
            Service.SES,
            Service.SNS,
            Service.SQS
    })
    @PropertySource("classpath:test.properties")
    public static class FullTestContext {

        public static final String MEMORY_CATALOG_NAME = "memory";

        @Value("${AWS_DYNAMODB_TEST_TABLE}")
        private String dynamoDbTableName;

        @Value("${AWS_DYNAMODB_TEST_TABLE_WITH_GSI}")
        private String dynamoDbTableWithIndexName;

        @Value("${AWS_DYNAMODB_TEST_TABLE_PARTITION_KEY}")
        private String dynamoDbTablePartitionKey;

        @Value("${AWS_DYNAMODB_TEST_INDEX_NAME}")
        private String dynamoDbIndexName;

        @Value("${AWS_ELASTICSEARCH_INDEX}")
        private String elasticsearchIndex;

        @Value("${AWS_ELASTICSEARCH_INDEX_ALIAS}")
        private String elasticsearchIndexAlias;

        @Value("${REDSHIFT_DB_NAME}")
        private String redshiftDbName;

        @Value("${POSTGRES_DB_NAME}")
        private String postgresDbName;

        @Value("${AWS_SNS_TEST_TOPIC}")
        private String snsTopicName;

        @Value("${AWS_KINESIS_STREAM_NAME_AIRWATCH_OUTPUT}")
        private String testStreamToCreate;

        @Value("${AWS_SQS_TEST_QUEUE}")
        private String testQueueToCreate;

        @Bean
        DynamoDbTablesSpecs dynamoDbTablesSpecs() {
            final AttributeDefinition key = AttributeDefinition.builder()
                    .attributeName(this.dynamoDbTablePartitionKey)
                    .attributeType(ScalarAttributeType.S)
                    .build();
            return DynamoDbTablesSpecs.builder()
                    .tablesToCreate(
                            List.of(DynamoDbTableSettings.builder()
                                            .tableName(this.dynamoDbTableName)
                                            .key(key)
                                            .attributeDefinitions(Collections.singletonList(key))
                                            .keyType(KeyType.HASH)
                                            .provisionedThroughputReadCapacity(1L)
                                            .provisionedThroughputWriteCapacity(1L)
                                            .build(),
                                    DynamoDbTableSettings.builder()
                                            .tableName(this.dynamoDbTableWithIndexName)
                                            .key(key)
                                            .keyType(KeyType.HASH)
                                            .provisionedThroughputReadCapacity(1L)
                                            .provisionedThroughputWriteCapacity(1L)
                                            .globalSecondaryIndexes(Collections.singletonList(GlobalSecondaryIndex.builder()
                                                    .indexName(this.dynamoDbIndexName)
                                                    .provisionedThroughput(ProvisionedThroughput.builder()
                                                            .readCapacityUnits(1L)
                                                            .writeCapacityUnits(1L)
                                                            .build())
                                                    .projection(Projection.builder()
                                                            .projectionType(ProjectionType.KEYS_ONLY)
                                                            .build())
                                                    .keySchema(KeySchemaElement.builder()
                                                            .attributeName(this.dynamoDbIndexName)
                                                            .keyType(KeyType.HASH)
                                                            .build())
                                                    .build()))
                                            .attributeDefinitions(List.of(
                                                    key,
                                                    AttributeDefinition.builder()
                                                            .attributeName(this.dynamoDbIndexName)
                                                            .attributeType(ScalarAttributeType.S)
                                                            .build()))
                                            .build()))
                    .build();
        }

        @Bean
        ElasticsearchIndexBuildConfiguration elasticsearchIndexBuildConfiguration() {
            return ElasticsearchIndexBuildConfiguration.builder()
                    .indicesToCreate(Collections.singletonList(ElasticsearchIndexSettings.builder()
                            .index(this.elasticsearchIndex)
                            .indexAlias(this.elasticsearchIndexAlias)
                            .build()))
                    .build();
        }

        @Bean
        KinesisStreamsSpec kinesisStreamsSpec() {
            return KinesisStreamsSpec.builder()
                    .streamsToCreate(Collections.singletonList(this.testStreamToCreate))
                    .build();
        }

        @Bean
        public TrinoCatalogSpecs memoryCatalog() {
            return TrinoCatalogSpecs.builder()
                    .catalog(TrinoCatalogSettings.builder()
                            .name(MEMORY_CATALOG_NAME)
                            .properties(Map.of(
                                    "connector.name", MEMORY_CATALOG_NAME,
                                    "memory.max-data-per-node", "128MB"))
                            .build())
                    .build();
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
        S3BucketSettings s3BucketSettings() {
            return S3BucketSettings.builder()
                    .name("test1")
                    .build();
        }

        @Bean
        S3BucketSpecs s3BucketSpecs(final S3BucketSettings s3BucketSettings) {
            return S3BucketSpecs.builder()
                    .bucket(s3BucketSettings)
                    .build();
        }

        @Bean
        SnsTopicsSpecs snsTopicsSpecs() {
            return SnsTopicsSpecs.builder()
                    .topicsToCreate(Collections.singletonList(this.snsTopicName))
                    .build();
        }

        @Bean
        SqsQueuesSpec sqsQueuesSpec() {
            return SqsQueuesSpec.builder()
                    .queuesToCreate(Collections.singletonList(this.testQueueToCreate))
                    .build();
        }

        @Bean
        RedshiftDbSettings redshiftDbSettings() {
            return RedshiftDbSettings.builder()
                   .dbName(this.redshiftDbName)
                   .build();
        }

        @Bean
        PostgresDbSettings postgresDbSettings() {
            return PostgresDbSettings.builder()
                    .dbName(this.postgresDbName)
                    .build();
        }
    }

}
