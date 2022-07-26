/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DeleteStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ResourceNotFoundException;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.Topic;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.AbstractFullContextTest;
import com.vmware.test.functional.saas.local.aws.config.DockerContainersConfiguration;
import com.vmware.test.functional.saas.local.context.TestContext;
import com.vmware.test.functional.saas.local.utils.ServiceDependenciesHealthHelper;
import com.vmware.test.functional.saas.aws.kinesis.KinesisHealthHelper;
import com.vmware.test.functional.saas.aws.s3.S3BucketSettings;
import com.vmware.test.functional.saas.aws.sns.SnsHealthHelper;
import com.vmware.test.functional.saas.aws.sqs.SqsHealthHelper;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@link DockerContainersConfiguration}.
 */
@Test(groups = "explicitOrderTestClass")
public class LocalContextTest extends AbstractFullContextTest {

    @Value("${AWS_SNS_TEST_TOPIC}")
    private String snsTopicName;

    @Value("${AWS_KINESIS_STREAM_NAME_AIRWATCH_OUTPUT}")
    private String testStreamName;

    @Value("${AWS_SQS_TEST_QUEUE}")
    private String testQueueName;

    @Value("${POSTGRES_DB_NAME}")
    private String postgresDbName;

    @Value("${REDSHIFT_DB_NAME}")
    private String redshiftDbName;

    @Autowired
    private KinesisClient kinesisClient;

    @Autowired
    private KmsClient kmsClient;

    @Autowired
    private ServiceEndpoint trinoEndpoint;

    @Autowired
    private ServiceEndpoint postgresEndpoint;

    @Autowired
    private ServiceEndpoint redshiftEndpoint;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3BucketSettings s3BucketSettings;

    @Autowired
    private SesClient sesClient;

    @Autowired
    private SnsClient snsClient;

    @Autowired
    private SqsClient sqsClient;

    @Test
    public void localKinesisAutoconfiguration() {
        assertThat(KinesisHealthHelper.checkHealth(this.kinesisClient, this.testStreamName), is(true));

        final DeleteStreamRequest deleteStreamRequest = DeleteStreamRequest.builder().streamName(this.testStreamName).build();
        assertThat(this.kinesisClient.deleteStream(deleteStreamRequest).sdkHttpResponse().isSuccessful(), is(true));

        await().until(() -> verifyDeleted(this.testStreamName));
    }

    @Test
    public void localKmsAutoconfiguration() {
        assertThat("kms is running ok", ServiceDependenciesHealthHelper.isKmsHealthy(this.kmsClient));
    }

    @Test
    public void localTrinoAutoconfiguration() {
        assertThat("Trino service is running OK.", ServiceDependenciesHealthHelper.isTrinoHealthy(
                this.trinoEndpoint, TestContext.FullTestContext.MEMORY_CATALOG_NAME));
    }

    @Test
    public void localPostgresAutoconfiguration() {
        assertThat("Local postgres is autoconfigured.", ServiceDependenciesHealthHelper.isPostgresHealthy(this.postgresEndpoint, this.postgresDbName));
    }

    @Test
    public void localRedshiftAutoconfiguration() {
        assertThat("Local redshift is autoconfigured.", ServiceDependenciesHealthHelper.isPostgresHealthy(this.redshiftEndpoint, this.redshiftDbName));
    }

    @Test
    public void localRedisAutoconfiguration() {
        assertThat("Redis service is running OK.", ServiceDependenciesHealthHelper.isRedisHealthy(this.redisTemplate));
    }

    @Test
    public void localS3AutoConfiguration() {

        final String key = "key";

        final Map<String, String> metadata = new HashMap<>();
        metadata.put("stryker-etag", "somethingelse");

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(key)
                        .metadata(metadata)
                        .build(),
                RequestBody.fromBytes(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 }));

        final HeadObjectResponse headObject = this.s3Client
                .headObject(HeadObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(key)
                        .build());
        assertThat(headObject.metadata().get("stryker-etag"), is("somethingelse"));
    }

    @Test
    public void localSesAutoConfiguration() {
        assertThat("SesClient cannot be null", this.sesClient, notNullValue());
        final String senderEmailAddress = "sender@test.com";

        // Verify sender email in order to successfully send email
        this.sesClient.verifyEmailIdentity(VerifyEmailIdentityRequest.builder().emailAddress(senderEmailAddress).build());
        assertThat(this.sesClient.listIdentities().identities(), contains(senderEmailAddress));

        final SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .source(senderEmailAddress)
                .destination(Destination.builder()
                        .toAddresses("recipient@test.com")
                        .build())
                .message(Message.builder()
                        .subject(Content.builder().data("Subject").build())
                        .body(Body.builder()
                                .text(Content.builder()
                                        .data("Text")
                                        .build())
                                .build())
                        .build())
                .build();
        final SendEmailResponse sendEmailResponse = this.sesClient.sendEmail(sendEmailRequest);
        assertThat(sendEmailResponse.sdkHttpResponse().isSuccessful(), is(true));
    }

    @Test
    public void localSnsAutoConfiguration() {
        assertThat("SnsClient cannot be null", this.snsClient, notNullValue());
        assertThat(SnsHealthHelper.checkHealth(this.snsClient, this.snsTopicName), is(true));

        final List<String> listAllTopicArns = this.snsClient.listTopics().topics().stream().map(Topic::topicArn).collect(Collectors.toList());
        // Example of a topic ARN: arn:aws:sns:us-east-2:123456789012:MyTopic
        final List<String> filteredTopicArns = listAllTopicArns.stream().filter(t -> this.snsTopicName.equals(StringUtils.substringAfterLast(t, ":")))
                .collect(Collectors.toList());

        assertThat(filteredTopicArns.size(), is(1));
        final String subscriptionEndpoint = "http://some-test-endpoint";
        final SubscribeResponse subscribeResponse = this.snsClient.subscribe(
                SubscribeRequest.builder().topicArn(filteredTopicArns.get(0))
                        .endpoint(subscriptionEndpoint)
                        .protocol("http")
                        .build());

        assertThat(subscribeResponse.sdkHttpResponse().isSuccessful(), is(true));
        final List<Subscription> subscriptions = this.snsClient.listSubscriptions().subscriptions();

        assertThat("subscription exists", subscriptions.stream().anyMatch(subscription -> subscriptionEndpoint.equals(subscription.endpoint())));
    }

    @Test
    public void localSqsAutoConfiguration() {
        assertThat("SqsClient cannot be null", this.sqsClient, notNullValue());
        assertThat(SqsHealthHelper.checkHealth(this.sqsClient, this.testQueueName), is(true));

        final GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(this.testQueueName).build();
        final String queueUrl = this.sqsClient.getQueueUrl(getQueueUrlRequest).queueUrl();
        final SendMessageResponse sendMessageResponse = this.sqsClient
                .sendMessage(SendMessageRequest.builder().queueUrl(queueUrl).messageBody("Test message").build());
        assertThat(sendMessageResponse.sdkHttpResponse().isSuccessful(), is(true));
    }

    private boolean verifyDeleted(final String streamName) {
        try {
            final DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();
            this.kinesisClient.describeStream(describeStreamRequest);
            return false;
        } catch (final ResourceNotFoundException e) {
            return true;
        }
    }
}
