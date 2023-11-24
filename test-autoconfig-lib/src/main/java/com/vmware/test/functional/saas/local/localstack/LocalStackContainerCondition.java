/*
 * Copyright 2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.localstack;

import static com.vmware.test.functional.saas.local.localstack.LocalstackUtil.lookupRequiredLocalstackServices;

import java.util.List;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.vmware.test.functional.saas.Service;

/**
 * Determine if LocalStack container should be started locally.
 */
class LocalStackContainerCondition implements Condition {

   @Override
   public boolean matches(@NotNull final ConditionContext context, @NotNull final AnnotatedTypeMetadata metadata) {
      final List<String> localstackServices = lookupRequiredLocalstackServices(context.getBeanFactory())
            .stream()
            .map(Service::name)
            .toList();

      return LocalstackUtil.getLocalstackServices(context).stream()
            .anyMatch(localstackServices::contains);
   }
}
