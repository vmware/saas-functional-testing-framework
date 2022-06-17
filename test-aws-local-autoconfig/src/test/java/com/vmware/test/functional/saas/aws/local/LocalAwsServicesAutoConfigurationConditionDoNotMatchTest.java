/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.aws.local.dynamodb.DynamoDbResourceCreator;
import com.vmware.test.functional.saas.aws.local.kinesis.KinesisResourceCreator;
import com.vmware.test.functional.saas.aws.local.s3.S3ResourceCreator;
import com.vmware.test.functional.saas.aws.local.sns.SnsResourceCreator;
import com.vmware.test.functional.saas.aws.local.sqs.SqsResourceCreator;
import com.vmware.test.functional.saas.aws.dynamodb.DynamoDbResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.kinesis.KinesisResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.s3.S3ResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sns.SnsResourceAwaitingInitializer;
import com.vmware.test.functional.saas.aws.sqs.SqsResourceAwaitingInitializer;

import static org.hamcrest.MatcherAssert.*;

/**
 * This test verifies that if a AWS service (i.e. Service.KINESIS) is NOT specified in a test context configuration,
 * then the required client needed for working with the service is NOT created.
 */
@ContextHierarchy(@ContextConfiguration(classes = LocalAwsServicesAutoConfigurationConditionDoNotMatchTest.TestContext.class))
@FunctionalTest
public class LocalAwsServicesAutoConfigurationConditionDoNotMatchTest extends AbstractTestNGSpringContextTests {

    public static class TestContext {

    }

    @Autowired
    private ApplicationContext context;

    @Test
    public void beanConditionsDoNotMatch() {
        // The following are not specified as service dependencies for this test...and should not be created
        assertThat("DynamoDb client has been created - unexpected based on condition.", this.context.getBeansOfType(DynamoDbClient.class).size() == 0);
        assertThat("DynamoDbResourceCreator has been created - unexpected based on condition.", this.context.getBeansOfType(DynamoDbResourceCreator.class).size() == 0);
        assertThat("DynamoDbResourceAwaitingInitializer has been created - unexpected based on condition.", this.context.getBeansOfType(
                DynamoDbResourceAwaitingInitializer.class).size() == 0);
        assertThat("Kinesis client has been created - unexpected based on condition.", this.context.getBeansOfType(KinesisClient.class).size() == 0);
        assertThat("KinesisResourceCreator has been created - unexpected based on condition.", this.context.getBeansOfType(KinesisResourceCreator.class).size() == 0);
        assertThat("KinesisResourceAwaitingInitializer has been created - unexpected based on condition.", this.context.getBeansOfType(
                KinesisResourceAwaitingInitializer.class).size() == 0);
        assertThat("KMS client has been created - unexpected based on condition.", this.context.getBeansOfType(KmsClient.class).size() == 0);
        assertThat("Lambda client has been created - unexpected based on condition.", this.context.getBeansOfType(LambdaClient.class).size() == 0);
        assertThat("S3 client has been created - unexpected based on condition.", this.context.getBeansOfType(S3Client.class).size() == 0);
        assertThat("S3ResourceCreator has NOT been created - unexpected based on condition.", this.context.getBeansOfType(S3ResourceCreator.class).size() == 0);
        assertThat("S3ResourceAwaitingInitializer has NOT been created - unexpected based on condition.", this.context.getBeansOfType(
                S3ResourceAwaitingInitializer.class).size() == 0);
        assertThat("SES client has been created - unexpected based on condition.", this.context.getBeansOfType(SesClient.class).size() == 0);
        assertThat("SNS client has been created - unexpected based on condition.", this.context.getBeansOfType(SnsClient.class).size() == 0);
        assertThat("SNSResourceCreator has been created - unexpected based on condition.", this.context.getBeansOfType(SnsResourceCreator.class).size() == 0);
        assertThat("SNSResourceAwaitingInitializer has been created - unexpected based on condition.", this.context.getBeansOfType(
                SnsResourceAwaitingInitializer.class).size() == 0);
        assertThat("SQS client has been created - unexpected based on condition.", this.context.getBeansOfType(SqsClient.class).size() == 0);
        assertThat("SQSResourceCreator has been created - unexpected based on condition.", this.context.getBeansOfType(SqsResourceCreator.class).size() == 0);
        assertThat("SQSResourceAwaitingInitializer has been created - unexpected based on condition.", this.context.getBeansOfType(
                SqsResourceAwaitingInitializer.class).size() == 0);
    }
}
