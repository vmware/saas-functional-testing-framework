/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;

import com.vmware.test.functional.saas.Service;

import lombok.Builder;
import lombok.SneakyThrows;

/**
 * {@link Condition} that checks for the presence or absence of specific {@link Service}.
 */
class OnServicesCondition implements Condition {

    @SneakyThrows
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Objects.requireNonNull - handle never returns null ")
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final ConditionalOnService conditionalOnService = metadata.getAnnotations().get(ConditionalOnService.class).synthesize();
        if (conditionalOnService.conditionalOnMissingBean()) {
            if (metadata instanceof MethodMetadata) {
                final String type = ((MethodMetadata)metadata).getReturnTypeName();
                final String[] names = Objects.requireNonNull(context.getBeanFactory()).getBeanNamesForType(Class.forName(type, true, context.getClassLoader()),
                        true, false);
                if (names.length > 0) {
                    return false;
                }
            }
        }
        final ContextMatcher matcher = ContextMatcher.builder()
                .context(context)
                .search(conditionalOnService.search())
                .build();
        return Arrays.stream(conditionalOnService.value())
                .allMatch(matcher::matchesService);
    }

    @Builder
    private static class ContextMatcher {
        private final ConditionContext context;
        private final SearchStrategy search;

        public boolean matchesService(final Service service) {
            return ServiceConditionUtil.getRequiredServiceDependencies(this.context,
                    this.search.equals(SearchStrategy.ALL))
                    .stream().map(LocalService::name).collect(Collectors.toSet())
                    .contains(service.name());
        }
    }
}
