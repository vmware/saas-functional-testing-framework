/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.service;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;

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
                    this.search.equals(SearchStrategy.ALL)).contains(service);
        }
    }
}