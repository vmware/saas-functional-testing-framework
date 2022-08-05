/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceConditionUtil;

/**
 * Determine which containers should be started locally.
 */
// localstack
public final class ContainerCondition {

    private ContainerCondition() {

    }

    public static abstract class SimpleServiceCondition implements Condition {

        public final boolean matches(@NotNull final ConditionContext context,
              @NotNull final AnnotatedTypeMetadata metadata) {
            final List<String> localstackServices = LocalstackUtil.getLocalstackServices(context);
            final Set<Service> requiredServiceDependencies = ServiceConditionUtil.getRequiredServiceDependencies(context);
            return requiredServiceDependencies.contains(getService()) && !localstackServices.contains(getService().name());
        }

        protected abstract Service getService();
    }

    /**
     * Determine if LocalStack container should be started locally.
     */
    static class LocalStackContainerCondition implements Condition {

        @Override
        public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
            final List<String> localstackServices = LocalstackUtil.lookupRequiredServiceDependenciesInfo(context.getBeanFactory())
                    .stream()
                    .filter(LocalService.BeanInfo::isLocalstackService)
                    .map(LocalService.BeanInfo::getName)
                    .collect(Collectors.toList());

            return LocalstackUtil.getLocalstackServices(context).stream()
                    .anyMatch(localstackServices::contains);
        }
    }
}
