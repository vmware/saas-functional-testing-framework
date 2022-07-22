/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Enumerated class representing the available Docker Container types used for functional test execution.
 */
// legacy model
@Getter
public enum DockerContainerType {

    DYNAMODB(DockerConfig::getDynamoDbImage, DockerConfig::getDynamoDbPort),
    ELASTICSEARCH(DockerConfig::getElasticsearchImage, DockerConfig::getElasticsearchPort),
    KINESIS(DockerConfig::getKinesisImage, DockerConfig::getKinesisPort),
    KMS(DockerConfig::getKmsImage, DockerConfig::getKmsPort),
    LOCALSTACK(DockerConfig::getLocalstackImageName, DockerConfig::getLocalstackDefaultServicePort),
    POSTGRES(DockerConfig::getPostgresImage, DockerConfig::getPostgresPort),
    PRESTO(DockerConfig::getPrestoImage, DockerConfig::getPrestoPort),
    REDIS(DockerConfig::getRedisImage, DockerConfig::getRedisPort),
    REDSHIFT(DockerConfig::getPostgresImage, DockerConfig::getPostgresPort),
    UNKNOWN(c -> UnknownContainerTypeConfig.NAME, c -> UnknownContainerTypeConfig.PORT);

    private final Function<DockerConfig, String> dockerImageNameMapper;
    private final Function<DockerConfig, Integer> internalDockerPortMapper;

    DockerContainerType(final Function<DockerConfig, String> dockerImageNameMapper, final Function<DockerConfig, Integer> internalDockerPortMapper) {
        this.dockerImageNameMapper = dockerImageNameMapper;
        this.internalDockerPortMapper = internalDockerPortMapper;
    }

    /**
     * UNKNOWN Docker Container Type values.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class UnknownContainerTypeConfig {
        public static final String NAME = "Unknown";
        public static final int PORT = -1;
    }
}
