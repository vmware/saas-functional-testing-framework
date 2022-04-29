/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

import com.aw.dpa.test.aws.local.dynamodb.DynamoDbResourceCreator;
import com.aw.dpa.test.aws.local.kinesis.KinesisResourceCreator;
import com.aw.dpa.test.aws.local.s3.S3ResourceCreator;
import com.aw.dpa.test.aws.local.sns.SnsResourceCreator;
import com.aw.dpa.test.aws.local.sqs.SqsResourceCreator;
import com.aw.dpa.test.functional.aws.dynamodb.DynamoDbResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.kinesis.KinesisResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.s3.S3ResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.sns.SnsResourceAwaitingInitializer;
import com.aw.dpa.test.functional.aws.sqs.SqsResourceAwaitingInitializer;

import static org.hamcrest.MatcherAssert.*;

/**
 * Test verifies that if a AWS service (i.e. Service.KINESIS) is specified in a test context configuration,
 * then the required client needed for working with the service is created.
 */
public class LocalAwsServicesAutoConfigurationConditionMatchTest extends AbstractFullContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void beanConditionsMatch() {
        // The following are specified as service dependencies for this test...and should be created
        assertThat("DynamoDb client has NOT been created - unexpected.", this.context.getBeansOfType(DynamoDbClient.class).size() == 1);
        assertThat("DynamoDbResourceCreator has NOT been created - unexpected.", this.context.getBeansOfType(DynamoDbResourceCreator.class).size() == 1);
        assertThat("DynamoDbResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                DynamoDbResourceAwaitingInitializer.class).size() == 1);
        assertThat("Kinesis client has NOT been created - unexpected.", this.context.getBeansOfType(KinesisClient.class).size() == 1);
        assertThat("KinesisResourceCreator has NOT been created - unexpected", this.context.getBeansOfType(KinesisResourceCreator.class).size() == 1);
        assertThat("KinesisResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                KinesisResourceAwaitingInitializer.class).size() == 1);
        assertThat("KMS client has NOT been created - unexpected", this.context.getBeansOfType(KmsClient.class).size() == 1);
        assertThat("Lambda client has NOT been created - unexpected", this.context.getBeansOfType(LambdaClient.class).size() == 1);
        // Two beans of type PostgresDatabaseInitializer should be created - one for simple postgres db and one for redshift db initialization
        assertThat("S3 client has NOT been created - unexpected", this.context.getBeansOfType(S3Client.class).size() == 1);
        assertThat("S3ResourceCreator has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                S3ResourceCreator.class).size() == 1);
        assertThat("S3ResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                S3ResourceAwaitingInitializer.class).size() == 1);
        assertThat("SES client has NOT been created - unexpected", this.context.getBeansOfType(SesClient.class).size() == 1);
        assertThat("SNS client has NOT been created - unexpected", this.context.getBeansOfType(SnsClient.class).size() == 1);
        assertThat("SNSResourceCreator has NOT been created - unexpected", this.context.getBeansOfType(SnsResourceCreator.class).size() == 1);
        assertThat("SNSResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                SnsResourceAwaitingInitializer.class).size() == 1);
        assertThat("SQS client has NOT been created - unexpected", this.context.getBeansOfType(SqsClient.class).size() == 1);
        assertThat("SQSResourceCreator has NOT been created - unexpected", this.context.getBeansOfType(SqsResourceCreator.class).size() == 1);
        assertThat("SQSResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                SqsResourceAwaitingInitializer.class).size() == 1);
    }
}
