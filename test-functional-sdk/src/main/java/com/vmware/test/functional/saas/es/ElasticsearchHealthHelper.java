/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.SneakyThrows;

/**
 * Elasticsearch Health Helper.
 */
public final class ElasticsearchHealthHelper {

    private ElasticsearchHealthHelper() {

    }

    /**
     * Elasticsearch Health Helper - verifying the index creation.
     *
     * @param esClient {@link ElasticsearchClient}.
     * @param index               The Elasticsearch index name.
     * @return {@code true} if the index exists, {@code true} if index is not created.
     */
    @SneakyThrows
    public static boolean checkHealth(final ElasticsearchClient esClient, final String index) {
        return esClient.indices().exists(ExistsRequest.of(builder -> builder.index(index))).value();
    }
}
