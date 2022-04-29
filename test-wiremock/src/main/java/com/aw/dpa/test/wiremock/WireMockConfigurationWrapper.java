/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.wiremock;

import org.springframework.cloud.contract.wiremock.WireMockConfiguration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.aw.dpa.test.FunctionalTestExecutionSettings.LOCAL_RUN_PHASE;
import static com.aw.dpa.test.FunctionalTestExecutionSettings.LOCAL_RUN_SKIP_DEPS;
import static com.aw.dpa.test.FunctionalTestExecutionSettings.localRunPhaseIntegerValue;
import static com.aw.dpa.test.FunctionalTestExecutionSettings.shouldSkip;

/**
 * Allows WireMockConfiguration to be opted in/out based on local.run.phase and local.run.skip.deps.
 */
@Configuration
@Conditional(WireMockConfigurationWrapper.WireMock.class)
@Import(WireMockConfiguration.class)
public class WireMockConfigurationWrapper {

    /**
     * Determine if WireMock service should be started locally.
     */
    public static class WireMock implements Condition {

        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            final Environment environment = context.getEnvironment();
            final boolean localRunSkipDeps = environment.getProperty(LOCAL_RUN_SKIP_DEPS, Boolean.class, false);
            final String localRunPhase = environment.getProperty(LOCAL_RUN_PHASE);
            return !shouldSkip(localRunPhaseIntegerValue(localRunPhase), localRunSkipDeps, 0);
        }
    }
}
