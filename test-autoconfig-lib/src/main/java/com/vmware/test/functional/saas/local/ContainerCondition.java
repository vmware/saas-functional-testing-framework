/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

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
// localstack
public final class ContainerCondition {

    private ContainerCondition() {

    }

    /**
     * Determine if Dynamodb service should be started locally.
     */
    public static class DynamodbContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(LocalService.DYNAMO_DB)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(LocalStackContainer.Service.DYNAMODB.getLocalStackName());
        }
    }

    /**
     * Determine if Kinesis service should be started locally.
     */
    public static class KinesisContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(LocalService.KINESIS)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(LocalStackContainer.Service.KINESIS.getLocalStackName());
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
                    .filter(LocalService::isLocalstackService)
                    .map(LocalService::getService)
                    .map(LocalStackContainer.Service::getLocalStackName)
                    .collect(Collectors.toList());

            return ServiceConditionUtil.getLocalstackServices(context).stream()
                    .anyMatch(localstackServices::contains);
        }
    }
}
