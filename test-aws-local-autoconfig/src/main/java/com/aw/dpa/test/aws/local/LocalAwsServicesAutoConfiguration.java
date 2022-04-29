/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.dynamodb.DynamoDbFactory;
import com.aw.dpa.test.aws.local.dynamodb.DynamoDbResourceCreator;
import com.aw.dpa.test.aws.local.kinesis.KinesisFactory;
import com.aw.dpa.test.aws.local.kinesis.KinesisResourceCreator;
import com.aw.dpa.test.aws.local.kms.KmsFactory;
import com.aw.dpa.test.aws.local.lambda.LambdaFactory;
import com.aw.dpa.test.aws.local.s3.S3Factory;
import com.aw.dpa.test.aws.local.s3.S3ResourceCreator;
import com.aw.dpa.test.aws.local.service.ConditionalOnService;
import com.aw.dpa.test.aws.local.service.DockerContainersConfiguration;
import com.aw.dpa.test.aws.local.service.Service;
import com.aw.dpa.test.aws.local.ses.SesFactory;
import com.aw.dpa.test.aws.local.sns.SnsFactory;
import com.aw.dpa.test.aws.local.sns.SnsResourceCreator;
import com.aw.dpa.test.aws.local.sqs.SQSFactory;
import com.aw.dpa.test.aws.local.sqs.SqsResourceCreator;
import com.aw.dpa.test.functional.aws.dynamodb.DynamoDbResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.kinesis.KinesisResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.s3.S3ResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.sns.SnsResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.sqs.SqsResourceAwaitingInitializer;

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
    DynamoDbFactory dynamoDbFactory(@Lazy final LocalServiceEndpoint dynamoDbEndpoint) {
        return new DynamoDbFactory(dynamoDbEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.DYNAMO_DB, search = SearchStrategy.ALL)
    @Lazy
    DynamoDbResourceCreator dynamoDbResourceCreator(@Lazy final DynamoDbFactory dynamoDbFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new DynamoDbResourceCreator(dynamoDbFactory,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.DYNAMO_DB, search = SearchStrategy.ALL)
    @Lazy
    DynamoDbResourceAwaitingInitializer dynamoDbResourceAwaitingInitializer(@Lazy final DynamoDbFactory dynamoDbFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new DynamoDbResourceAwaitingInitializer(dynamoDbFactory.getObject(), functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.KINESIS)
    @Lazy
    KinesisFactory kinesisFactory(@Lazy final LocalServiceEndpoint kinesisEndpoint) {
        return new KinesisFactory(kinesisEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.KINESIS, search = SearchStrategy.ALL)
    @Lazy
    KinesisResourceCreator kinesisResourceCreator(@Lazy final KinesisFactory kinesisFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new KinesisResourceCreator(kinesisFactory, this.awsSettings,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.KINESIS, search = SearchStrategy.ALL)
    @Lazy
    KinesisResourceAwaitingInitializer kinesisResourceAwaitingInitializer(@Lazy final KinesisFactory kinesisFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new KinesisResourceAwaitingInitializer(kinesisFactory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.KMS)
    @Lazy
    KmsFactory kmsFactory(@Lazy final LocalServiceEndpoint kmsEndpoint) {
        return new KmsFactory(kmsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.LAMBDA)
    @Lazy
    LambdaFactory lambdaFactory(@Lazy final LocalServiceEndpoint lambdaEndpoint) {
        return new LambdaFactory(lambdaEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.S3)
    @Lazy
    S3Factory s3Factory(@Lazy final LocalServiceEndpoint s3Endpoint) {
        return new S3Factory(s3Endpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.S3, search = SearchStrategy.ALL)
    @Lazy
    S3ResourceCreator s3ResourceCreator(final S3Factory s3Factory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new S3ResourceCreator(functionalTestExecutionSettings,
                this.awsSettings, s3Factory.getObject());
    }

    @Bean
    @ConditionalOnService(value = Service.S3, search = SearchStrategy.ALL)
    @Lazy
    S3ResourceAwaitingInitializer s3ResourceAwaitingInitializer(final S3Factory s3Factory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new S3ResourceAwaitingInitializer(s3Factory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.SES)
    @Lazy
    SesFactory sesFactory(@Lazy final LocalServiceEndpoint sesEndpoint) {
        return new SesFactory(sesEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.SNS)
    @Lazy
    SnsFactory snsFactory(@Lazy final LocalServiceEndpoint snsEndpoint) {
        return new SnsFactory(snsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SNS, search = SearchStrategy.ALL)
    @Lazy
    SnsResourceCreator snsResourceCreator(@Lazy final SnsFactory snsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SnsResourceCreator(snsFactory,
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SNS, search = SearchStrategy.ALL)
    @Lazy
    SnsResourceAwaitingInitializer snsResourceAwaitingInitializer(@Lazy final SnsFactory snsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SnsResourceAwaitingInitializer(snsFactory.getObject(),
                functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(Service.SQS)
    @Lazy
    SQSFactory sqsFactory(@Lazy final LocalServiceEndpoint sqsEndpoint) {
        return new SQSFactory(sqsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SQS, search = SearchStrategy.ALL)
    @Lazy
    SqsResourceCreator sqsResourceCreator(@Lazy final SQSFactory sqsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SqsResourceCreator(sqsFactory, functionalTestExecutionSettings);
    }

    @Bean
    @ConditionalOnService(value = Service.SQS, search = SearchStrategy.ALL)
    @Lazy
    SqsResourceAwaitingInitializer sqsResourceAwaitingInitializer(@Lazy final SQSFactory sqsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return new SqsResourceAwaitingInitializer(sqsFactory.getObject(),
                functionalTestExecutionSettings);
    }
}
