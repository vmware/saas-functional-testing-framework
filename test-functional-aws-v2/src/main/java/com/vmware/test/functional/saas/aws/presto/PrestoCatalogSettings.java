/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.presto;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Local presto catalog configuration properties.
 */
@Builder
@Getter
public class PrestoCatalogSettings {

    @NonNull
    private final String name;
    @NonNull
    private final Map<String, String> properties;

    /**
     * Custom builder implementation that overrides the build method.
     */
    public static class PrestoCatalogSettingsBuilder {

        private String name;
        private Map<String, String> properties;

        /**
         * Verifies the properties map contains a required property for any catalog.
         * @return {@code PrestoCatalogSettings}
         */
        public PrestoCatalogSettings build() {
            if (!this.properties.containsKey("connector.name") || StringUtils.isBlank(this.properties.get("connector.name"))) {
                throw new RuntimeException(String.format("Presto catalog settings [%s] missing required property field 'connector.name'", this.name));
            }
            return new PrestoCatalogSettings(this.name, this.properties);
        }
    }

}
