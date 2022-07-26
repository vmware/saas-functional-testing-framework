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

import com.vmware.test.functional.saas.Service;

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
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.DYNAMO_DB)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(Service.DYNAMO_DB.name());
        }
    }

    /**
     * Determine if Kinesis service should be started locally.
     */
    public static class KinesisContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            return ServiceConditionUtil.getRequiredServiceDependencies(context).contains(Service.KINESIS)
                    && !ServiceConditionUtil.getLocalstackServices(context).contains(Service.KINESIS.name());
        }
    }

    /**
     * Determine if LocalStack container should be started locally.
     */
    public static class LocalStackContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            final List<String> localstackServices = ServiceConditionUtil.lookupRequiredServiceDependenciesInfo(context.getBeanFactory())
                    .stream()
                    .filter(LocalService.BeanInfo::isLocalstackService)
                    .map(LocalService.BeanInfo::getName)
                    .collect(Collectors.toList());

            return ServiceConditionUtil.getLocalstackServices(context).stream()
                    .anyMatch(localstackServices::contains);
        }
    }
}
