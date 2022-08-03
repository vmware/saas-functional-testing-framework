/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.environment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper to calculate the environment.
 * This provides ability to override environment variables which are prefixed with namespace.
 * <p>
 * This can be used if we want to override an env variable passed to an app process
 * (for example - if RULE_EVALUATION_QUEUE_NAME needs to be overridden by the spring environment
 * we can override the property by adding for example new sys property mosaic.RULE_EVALUATION_QUEUE_NAME="new value".)
 */
@Slf4j
public final class CalculateEnvironmentUtils {

    private CalculateEnvironmentUtils() {

    }

    /**
     * Utility method used to calculate the environment based on provided spring environment, namespace and
     * system properties.
     *
     * @param envPropertiesMap Map with environment properties to be overridden.
     * @param environment      Spring environment.
     * @param namespace        A namespace criteria.
     * @return Map with overridden environment properties.
     */
    public static Map<String, String> calculateEnv(final Map<String, String> envPropertiesMap, final Environment environment,
            final String namespace) {
        final Map<String, String> env = new HashMap<>(envPropertiesMap);
        if (environment == null) {
            return env;
        }
        // if the access to the spring env is granted, check it for overrides.
        for (Map.Entry<String, String> envVar : env.entrySet()) {
            log.trace("Env property provided with value: {}", envVar);
            final String key = (StringUtils.isNotBlank(namespace)) ? (namespace + "." + envVar.getKey()) : envVar.getKey();
            if (environment.containsProperty(key)) {
                log.debug("Overriding provided env property: [{}] with value: [{}]", key, environment.getRequiredProperty(key));
                // override the value with the one from the environment
                envVar.setValue(environment.getRequiredProperty(key));
            }
        }
        return env;
    }
}
