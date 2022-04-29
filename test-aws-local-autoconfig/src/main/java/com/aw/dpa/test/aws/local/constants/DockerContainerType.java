/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.constants;

import java.util.function.Function;

import lombok.Getter;

/**
 * Enumerated class representing the available Docker Container types used for functional test execution.
 */
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
    UNKNOWN(c -> DockerContainerConstants.UnknownContainerTypeConfig.NAME, c -> DockerContainerConstants.UnknownContainerTypeConfig.PORT);

    private final Function<DockerConfig, String> dockerImageNameMapper;
    private final Function<DockerConfig, Integer> internalDockerPortMapper;

    DockerContainerType(final Function<DockerConfig, String> dockerImageNameMapper, final Function<DockerConfig, Integer> internalDockerPortMapper) {
        this.dockerImageNameMapper = dockerImageNameMapper;
        this.internalDockerPortMapper = internalDockerPortMapper;
    }
}
