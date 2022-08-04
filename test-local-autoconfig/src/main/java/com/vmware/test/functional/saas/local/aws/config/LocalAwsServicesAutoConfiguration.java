/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.local.aws.dynamodb.DynamoDbClientFactory;
import com.vmware.test.functional.saas.local.aws.dynamodb.DynamoDbResourceCreator;
import com.vmware.test.functional.saas.local.aws.kinesis.KinesisClientFactory;
import com.vmware.test.functional.saas.local.aws.kinesis.KinesisResourceCreator;
import com.vmware.test.functional.saas.local.aws.kms.KmsClientFactory;
import com.vmware.test.functional.saas.local.aws.lambda.LambdaClientFactory;
import com.vmware.test.functional.saas.local.aws.redshift.RedshiftDbSettings;
import com.vmware.test.functional.saas.local.aws.s3.S3ClientFactory;
import com.vmware.test.functional.saas.local.aws.s3.S3ResourceCreator;
import com.vmware.test.functional.saas.local.aws.ses.SesClientFactory;
import com.vmware.test.functional.saas.local.aws.sns.SnsClientFactory;
import com.vmware.test.functional.saas.local.aws.sns.SnsResourceCreator;
import com.vmware.test.functional.saas.local.aws.sqs.SQSClientFactory;
import com.vmware.test.functional.saas.local.aws.sqs.SqsResourceCreator;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.kinesis.KinesisResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.s3.S3ResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sns.SnsResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sqs.SqsResourceAwaitingInitializer;
import com.vmware.test.functional.saas.ConditionalOnService;
import com.vmware.test.functional.saas.local.pg.PostgresDatabaseCreator;

/**
 * Local AWS Services AutoConfiguration. To be used by {@code FunctionalTest}.
 */
@Configuration
@Import(DockerContainersConfiguration.class)
@AutoConfigureOrder(Integer.MAX_VALUE)
@EnableConfigurationProperties(AwsSettings.class)
@PropertySource("classpath:aws-local.properties")
public class LocalAwsServicesAutoConfiguration {

    @Autowired
    private AwsSettings awsSettings;

    @Bean
    @ConditionalOnService(Service.DYNAMO_DB)
    @Lazy
    DynamoDbClientFactory dynamoDbFactory(@Lazy final ServiceEndpoint dynamoDbEndpoint) {
        return new DynamoDbClientFactory(dynamoDbEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.DYNAMO_DB, search = SearchStrategy.ALL)
    @Lazy
    DynamoDbResourceCreator dynamoDbResourceCreator(@Lazy final DynamoDbClientFactory dynamoDbClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new DynamoDbResourceCreator(dynamoDbClientFactory,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.DYNAMO_DB, search = SearchStrategy.ALL)
    @Lazy
    DynamoDbResourceAwaitingInitializer dynamoDbResourceAwaitingInitializer(@Lazy final DynamoDbClientFactory dynamoDbClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new DynamoDbResourceAwaitingInitializer(dynamoDbClientFactory.getObject(), functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.KINESIS)
    @Lazy
    KinesisClientFactory kinesisFactory(@Lazy final ServiceEndpoint kinesisEndpoint) {
        return new KinesisClientFactory(kinesisEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.KINESIS, search = SearchStrategy.ALL)
    @Lazy
    KinesisResourceCreator kinesisResourceCreator(@Lazy final KinesisClientFactory kinesisClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new KinesisResourceCreator(kinesisClientFactory, this.awsSettings,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.KINESIS, search = SearchStrategy.ALL)
    @Lazy
    KinesisResourceAwaitingInitializer kinesisResourceAwaitingInitializer(@Lazy final KinesisClientFactory kinesisClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new KinesisResourceAwaitingInitializer(kinesisClientFactory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.KMS)
    @Lazy
    KmsClientFactory kmsFactory(@Lazy final ServiceEndpoint kmsEndpoint) {
        return new KmsClientFactory(kmsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.LAMBDA)
    @Lazy
    LambdaClientFactory lambdaFactory(@Lazy final ServiceEndpoint lambdaEndpoint) {
        return new LambdaClientFactory(lambdaEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.REDSHIFT, search = SearchStrategy.ALL)
    @Lazy
    PostgresDatabaseCreator<RedshiftDbSettings> redshiftDatabaseCreator(
          final FunctionalTestExecutionSettings functionalTestExecutionSettings,
          final ServiceEndpoint redshiftEndpoint) {
        return new PostgresDatabaseCreator<>(functionalTestExecutionSettings,
              redshiftEndpoint, RedshiftDbSettings.class);
    }

    @Bean
    @ConditionalOnService(Service.S3)
    @Lazy
    S3ClientFactory s3Factory(@Lazy final ServiceEndpoint s3Endpoint) {
        return new S3ClientFactory(s3Endpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.S3, search = SearchStrategy.ALL)
    @Lazy
    S3ResourceCreator s3ResourceCreator(final S3ClientFactory s3ClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new S3ResourceCreator(functionalTestExecutionSettings,
                this.awsSettings, s3ClientFactory.getObject());
    }

    @Bean
    @ConditionalOnService(value = Service.S3, search = SearchStrategy.ALL)
    @Lazy
    S3ResourceAwaitingInitializer s3ResourceAwaitingInitializer(final S3ClientFactory s3ClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new S3ResourceAwaitingInitializer(s3ClientFactory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.SES)
    @Lazy
    SesClientFactory sesFactory(@Lazy final ServiceEndpoint sesEndpoint) {
        return new SesClientFactory(sesEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.SNS)
    @Lazy
    SnsClientFactory snsFactory(@Lazy final ServiceEndpoint snsEndpoint) {
        return new SnsClientFactory(snsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SNS, search = SearchStrategy.ALL)
    @Lazy
    SnsResourceCreator snsResourceCreator(@Lazy final SnsClientFactory snsClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SnsResourceCreator(snsClientFactory,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SNS, search = SearchStrategy.ALL)
    @Lazy
    SnsResourceAwaitingInitializer snsResourceAwaitingInitializer(@Lazy final SnsClientFactory snsClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SnsResourceAwaitingInitializer(snsClientFactory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.SQS)
    @Lazy
    SQSClientFactory sqsFactory(@Lazy final ServiceEndpoint sqsEndpoint) {
        return new SQSClientFactory(sqsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SQS, search = SearchStrategy.ALL)
    @Lazy
    SqsResourceCreator sqsResourceCreator(@Lazy final SQSClientFactory sqsClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SqsResourceCreator(sqsClientFactory, functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SQS, search = SearchStrategy.ALL)
    @Lazy
    SqsResourceAwaitingInitializer sqsResourceAwaitingInitializer(@Lazy final SQSClientFactory sqsClientFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SqsResourceAwaitingInitializer(sqsClientFactory.getObject(),
                functionalTestExecutionSettings);
    }
}
