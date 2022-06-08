/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.LocalServiceEndpoint;

import lombok.Getter;

import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.DYNAMODB_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.ELASTICSEARCH_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.KINESIS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.KMS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.LAMBDA_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.POSTGRES_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.PRESTO_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.REDIS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.REDSHIFT_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.S3_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SES_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SNS_ENDPOINT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.Components.SQS_ENDPOINT;

/**
 * Enumerated class representing the type of services to be started locally. To be used by declaring functional tests service dependencies.
 */
// legacy model
// move to test-functional
@Getter
public enum LocalService {

    DYNAMO_DB(DYNAMODB_ENDPOINT, DockerContainerType.DYNAMODB, LocalStackContainer.Service.DYNAMODB, 10151),
    ELASTICSEARCH(ELASTICSEARCH_ENDPOINT, DockerContainerType.ELASTICSEARCH, 10152),
    KINESIS(KINESIS_ENDPOINT, DockerContainerType.KINESIS, LocalStackContainer.Service.KINESIS, 10153, "https"),
    KMS(KMS_ENDPOINT, DockerContainerType.KMS, 10154),
    LAMBDA(LAMBDA_ENDPOINT, DockerContainerType.UNKNOWN, null, 10163),
    POSTGRES(POSTGRES_ENDPOINT, DockerContainerType.POSTGRES, 10155),
    PRESTO(PRESTO_ENDPOINT, DockerContainerType.PRESTO, 10156),
    REDIS(REDIS_ENDPOINT, DockerContainerType.REDIS, 10157),
    REDSHIFT(REDSHIFT_ENDPOINT, DockerContainerType.REDSHIFT, 10162),
    S3(S3_ENDPOINT, DockerContainerType.LOCALSTACK, LocalStackContainer.Service.S3, 10158),
    SES(SES_ENDPOINT, DockerContainerType.LOCALSTACK, LocalStackContainer.Service.SES, 10161),
    SNS(SNS_ENDPOINT, DockerContainerType.LOCALSTACK, LocalStackContainer.Service.SNS, 10159),
    SQS(SQS_ENDPOINT, DockerContainerType.LOCALSTACK, LocalStackContainer.Service.SQS, 10160);

    static {
        final Set<Integer> ports = new HashSet<>();
        Arrays.stream(values()).forEach(value -> {
            if (!ports.add(value.port)) {
                throw new RuntimeException("Duplicate port found in Service enum: " + value + " -> " + value.port);
            }
        });
    }

    private final String endpoint;
    private final int port;
    private final String scheme;
    private final LocalStackContainer.Service service;
    private final DockerContainerType defaultContainerType;

    LocalService(final String endpointName, final DockerContainerType defaultContainerType, final LocalStackContainer.Service service, final int port, final String scheme) {
        this.endpoint = endpointName;
        this.defaultContainerType = defaultContainerType;
        this.port = port;
        this.service = service;
        this.scheme = scheme;
    }

    LocalService(final String endpointName, final DockerContainerType defaultContainerType, final int port) {
        this(endpointName, defaultContainerType, null, port, LocalServiceEndpoint.DEFAULT_SCHEME);
    }

    LocalService(final String endpointName, final DockerContainerType defaultContainerType, final LocalStackContainer.Service service, final int port) {
        this(endpointName, defaultContainerType, service, port, LocalServiceEndpoint.DEFAULT_SCHEME);
    }

    public boolean isLocalstackService() {
        return this.service != null;
    }

}
