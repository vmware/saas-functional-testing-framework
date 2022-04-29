/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Docker containers constants.
 */
public final class DockerContainerConstants {

    private DockerContainerConstants() {
    }

    /**
     * Docker Containers Commands.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Command {

        public static final String SSL = "--ssl";
    }

    /**
     * Docker Containers Environment Keys.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Environment {
        /*
         * Elasticsearch goes into read-only mode when you have less than 5% of free disk space.
         * Setting this to false allows us to disable that disk allocation decider.
         * See https://www.elastic.co/guide/en/elasticsearch/reference/6.8/disk-allocator.html
         */
        public static final String ELASTICSEARCH_CLUSTER_DISK_THRESHOLD_ENABLED = "cluster.routing.allocation.disk.threshold_enabled";

        public static final String KMS_REGION = "KMS_REGION";
        public static final String KMS_ACCOUNT_ID = "KMS_ACCOUNT_ID";

        public static final String LOCALSTACK_REGION = "DEFAULT_REGION";
    }

    /**
     * UNKNOWN Docker Container Type values.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UnknownContainerTypeConfig {
        public static final String NAME = "Unknown";
        public static final int PORT = -1;
    }

}
