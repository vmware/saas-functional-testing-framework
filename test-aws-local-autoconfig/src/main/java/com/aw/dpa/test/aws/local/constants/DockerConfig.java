/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Docker configurations needed for container deployment.
 */
@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "docker")
public class DockerConfig {

    public static final String LOCALSTACK_VERSION = "0.12.5";

    private String dynamoDbImage;
    private int dynamoDbPort;
    private String kinesisImage;
    private int kinesisPort;
    private String kmsImage;
    private int kmsPort;
    private String redisImage;
    private int redisPort;
    private String prestoImage;
    private int prestoPort;
    private String postgresImage;
    private int postgresPort;
    private String elasticsearchImage;
    private int elasticsearchPort;
    private String localstackImageNameFormat;
    private int localstackDefaultServicePort;

    public String getLocalstackImageName() {
        return String.format(getLocalstackImageNameFormat(), LOCALSTACK_VERSION);
    }
}
