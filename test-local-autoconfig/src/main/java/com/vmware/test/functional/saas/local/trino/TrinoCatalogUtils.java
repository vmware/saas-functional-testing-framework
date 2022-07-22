/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.trino;

import java.util.Map;

import com.vmware.test.functional.saas.ServiceEndpoint;

/**
 * Utility methods for creating trino catalog settings.
 */
public final class TrinoCatalogUtils {

    private TrinoCatalogUtils() { }

    /**
     * Returns default trino catalog properties for ElasticSearch.
     * @param elasticsearchEndpoint the local elasticsearch endpoint.
     * @return Map with properties.
     */
    public static Map<String, String> elasticsearchCatalog(final ServiceEndpoint elasticsearchEndpoint) {
        return Map.ofEntries(
                Map.entry("connector.name", "elasticsearch"),
                Map.entry("elasticsearch.host", elasticsearchEndpoint.getContainerConfig().getName()),
                Map.entry("elasticsearch.port", String.valueOf(elasticsearchEndpoint.getContainerConfig().getPort())),
                Map.entry("elasticsearch.default-schema-name", "es"),
                Map.entry("elasticsearch.scroll-size", "1000"),
                Map.entry("elasticsearch.scroll-timeout", "1m"),
                Map.entry("elasticsearch.request-timeout", "10s"),
                Map.entry("elasticsearch.connect-timeout", "20s"),
                Map.entry("elasticsearch.node-refresh-interval", "1s")
        );
    }
}
