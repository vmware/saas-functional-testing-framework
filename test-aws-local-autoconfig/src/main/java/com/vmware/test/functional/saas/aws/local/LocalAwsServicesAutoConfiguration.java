/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.local;

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
import com.vmware.test.functional.saas.aws.local.dynamodb.DynamoDbFactory;
import com.vmware.test.functional.saas.aws.local.dynamodb.DynamoDbResourceCreator;
import com.vmware.test.functional.saas.aws.local.kinesis.KinesisFactory;
import com.vmware.test.functional.saas.aws.local.kinesis.KinesisResourceCreator;
import com.vmware.test.functional.saas.aws.local.kms.KmsFactory;
import com.vmware.test.functional.saas.aws.local.lambda.LambdaFactory;
import com.vmware.test.functional.saas.aws.local.s3.S3Factory;
import com.vmware.test.functional.saas.aws.local.s3.S3ResourceCreator;
import com.vmware.test.functional.saas.aws.local.service.DockerContainersConfiguration;
import com.vmware.test.functional.saas.aws.local.ses.SesFactory;
import com.vmware.test.functional.saas.aws.local.sns.SnsFactory;
import com.vmware.test.functional.saas.aws.local.sns.SnsResourceCreator;
import com.vmware.test.functional.saas.aws.local.sqs.SQSFactory;
import com.vmware.test.functional.saas.aws.local.sqs.SqsResourceCreator;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.kinesis.KinesisResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.s3.S3ResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sns.SnsResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sqs.SqsResourceAwaitingInitializer;
import com.vmware.test.functional.saas.local.ConditionalOnService;

/**
 * Local AWS Services AutoConfiguration. To be used by {@code FunctionalTest}.
 */
// TODO split
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
    DynamoDbFactory dynamoDbFactory(@Lazy final ServiceEndpoint dynamoDbEndpoint) {
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
    KinesisFactory kinesisFactory(@Lazy final ServiceEndpoint kinesisEndpoint) {
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
    KmsFactory kmsFactory(@Lazy final ServiceEndpoint kmsEndpoint) {
        return new KmsFactory(kmsEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.LAMBDA)
    @Lazy
    LambdaFactory lambdaFactory(@Lazy final ServiceEndpoint lambdaEndpoint) {
        return new LambdaFactory(lambdaEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.S3)
    @Lazy
    S3Factory s3Factory(@Lazy final ServiceEndpoint s3Endpoint) {
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
    SesFactory sesFactory(@Lazy final ServiceEndpoint sesEndpoint) {
        return new SesFactory(sesEndpoint, this.awsSettings);
    }

    @Bean
    @ConditionalOnService(Service.SNS)
    @Lazy
    SnsFactory snsFactory(@Lazy final ServiceEndpoint snsEndpoint) {
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
    SQSFactory sqsFactory(@Lazy final ServiceEndpoint sqsEndpoint) {
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
