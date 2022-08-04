/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;

import lombok.extern.slf4j.Slf4j;

/**
 * Configurations specific to Functional Test Execution Environment.
 */
@Slf4j
public class FunctionalTestExecutionSettings {
    public static final String LOCAL_RUN_SKIP_DEPS = "local.run.skip.deps";
    public static final String LOCAL_RUN_PHASE = "local.run.phase";
    private static final String LOCAL_RUN_PHASE_EXP = "${" + LOCAL_RUN_PHASE + "}";

    @Value("${" + LOCAL_RUN_SKIP_DEPS + ":false}")
    private boolean localRunSkipDeps;
    @Value("#{T(com.vmware.test.functional.saas.FunctionalTestExecutionSettings).localRunPhaseIntegerValue('" + LOCAL_RUN_PHASE_EXP + "')}")
    private Integer localRunPhase;

    /**
     * Determine if the given lifecycle component should be skipped from the startup sequence.
     *
     * @param lifecycle component to check
     * @return whether the component should be started or not
     */
    public boolean shouldSkip(final SmartLifecycle lifecycle) {
        return shouldSkip(this.localRunPhase, this.localRunSkipDeps, lifecycle.getPhase());
    }

    /**
     * Determine if the given lifecycle phase should be skipped from the startup sequence.
     *
     * @param localRunPhase the value of the "local.run.phase" property
     * @param localRunSkipDeps the value of the "local.run.skip.deps" property
     * @param targetLifecyclePhase lifecycle phase to check
     * @return  whether the lifecycle phase should be reached or not
     */
    public static boolean shouldSkip(final Integer localRunPhase, final boolean localRunSkipDeps, final int targetLifecyclePhase) {
        log.debug("localRunPhase = {}, localRunSkipDeps = {}, lifecycle.phase = {}",
                localRunPhase, localRunSkipDeps, targetLifecyclePhase);
        /*
         * For a lifecycle component with a target lifecycle phase = 0,
         * the following table (of combinations of config properties localRunPhase and localRunSkipDeps)
         * will determine whether the component should be skipped:
         * services-only         - no  <- (localRunPhase = 1,         localRunSkipDeps = false)
         * apps-only-no-services - yes <- (localRunPhase = "default", localRunSkipDeps = true)
         * apps-only             - no  <- (localRunPhase = "default", localRunSkipDeps = false)
         * tests-only            - yes <- (localRunPhase = null,      localRunSkipDeps = true)
         * default               - no  <- (localRunPhase = null,      localRunSkipDeps = false)
         */
        if (localRunPhase == null) {
            return localRunSkipDeps;
        }
        if (localRunPhase == targetLifecyclePhase) {
            return false;
        }
        return localRunPhase < targetLifecyclePhase || localRunSkipDeps;
    }

    /**
     * Converts the value of the "local.run.phase" property to Integer.
     *
     * @param localRunPhase the value of "local.run.phase" property.
     * @return return integer value of "local.run.phase" property.
     */
    public static Integer localRunPhaseIntegerValue(final String localRunPhase) {
        if (LOCAL_RUN_PHASE_EXP.equals(localRunPhase) || localRunPhase == null) {
            // property expression is not resolved means property is not specified (i.e null)
            return null;
        }
        return "default".equals(localRunPhase) ? SmartLifecycle.DEFAULT_PHASE : Integer.parseInt(localRunPhase);
    }
}
