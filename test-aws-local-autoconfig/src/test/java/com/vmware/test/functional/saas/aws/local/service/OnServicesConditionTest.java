/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.service;

import java.util.Map;
import java.util.Set;

import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class OnServicesConditionTest {

    private ConditionContext context;
    private OnServicesCondition onServicesCondition = new OnServicesCondition();

    @BeforeTest(alwaysRun = true)
    void setup() {
        this.onServicesCondition = new OnServicesCondition();
        this.context = mock(ConditionContext.class);
    }

    @Test
    public void verifyOnServicesConditionMatchCurrentContext() {
        final AnnotatedTypeMetadata metadata = mockAnnotatedTypeMetadata(SearchStrategy.CURRENT, Service.PRESTO);

        try (MockedStatic<ServiceConditionUtil> utilities = mockStatic(ServiceConditionUtil.class)) {
            utilities.when(() -> ServiceConditionUtil.getRequiredServiceDependencies(this.context, false))
                    .thenReturn(Set.of(Service.PRESTO));
            final boolean result = this.onServicesCondition.matches(this.context, metadata);
            assertThat(result, is(true));
        }
    }

    @Test
    public void verifyOnServicesConditionMatchAll() {
        final AnnotatedTypeMetadata metadata = mockAnnotatedTypeMetadata(SearchStrategy.ALL, Service.S3);

        try (MockedStatic<ServiceConditionUtil> utilities = mockStatic(ServiceConditionUtil.class)) {
            utilities.when(() -> ServiceConditionUtil.getRequiredServiceDependencies(this.context, true))
                    .thenReturn(Set.of(Service.S3));
            final boolean result = this.onServicesCondition.matches(this.context, metadata);
            assertThat(result, is(true));
        }
    }

    private AnnotatedTypeMetadata mockAnnotatedTypeMetadata(final SearchStrategy searchStrategy, final Service... value) {
        final AnnotatedTypeMetadata annotatedTypeMetadata = mock(AnnotatedTypeMetadata.class, RETURNS_DEEP_STUBS);
        when(annotatedTypeMetadata.getAnnotations()
                .get(ConditionalOnService.class))
                .thenReturn(MergedAnnotation.of(ConditionalOnService.class, Map.of(
                        "value", value,
                        "search", searchStrategy)));
        return annotatedTypeMetadata;
    }
}
