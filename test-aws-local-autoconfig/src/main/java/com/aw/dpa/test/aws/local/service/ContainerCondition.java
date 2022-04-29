/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testcontainers.containers.localstack.LocalStackContainer;

/**
 * Determine which containers should be started locally.
 */
public final class ContainerCondition {

    private ContainerCondition() {

    }

    /**
     * Determine if Dynamodb service should be started locally.
     */
    public static class DynamodbContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.DYNAMO_DB)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(LocalStackContainer.Service.DYNAMODB.getLocalStackName());
        }
    }

    /**
     * Determine if Elasticsearch service should be started locally.
     */
    public static class ElasticsearchContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.ELASTICSEARCH);
        }
    }

    /**
     * Determine if Kinesis service should be started locally.
     */
    public static class KinesisContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.KINESIS)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(LocalStackContainer.Service.KINESIS.getLocalStackName());
        }
    }

    /**
     * Determine if Kms service should be started locally.
     */
    public static class KmsContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.KMS);
        }
    }

    /**
     * Determine if Redis service should be started locally.
     */
    public static class RedisContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.REDIS);
        }
    }

    /**
     * Determine if Redshift service should be started locally.
     */
    public static class RedshiftContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.REDSHIFT);
        }
    }

    /**
     * Determine if Postgres service should be started locally.
     */
    public static class PostgresContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.POSTGRES);
        }
    }

    /**
     * Determine if Presto service should be started locally.
     */
    public static class PrestoContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.PRESTO);
        }
    }

    /**
     * Determine if LocalStack container should be started locally.
     */
    public static class LocalStackContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            final List<String> localstackServices = ServiceConditionUtil.getRequiredServiceDependencies(context)
                    .stream()
                    .filter(Service::isLocalstackService)
                    .map(Service::getService)
                    .map(LocalStackContainer.Service::getLocalStackName)
                    .collect(Collectors.toList());

            return ServiceConditionUtil.getLocalstackServices(context).stream()
                    .anyMatch(localstackServices::contains);
        }
    }
}
