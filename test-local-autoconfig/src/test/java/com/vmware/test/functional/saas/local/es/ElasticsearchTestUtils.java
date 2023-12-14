/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.es;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;

import lombok.SneakyThrows;

/**
 * Routines for Elasticsearch tests.
 */
public final class ElasticsearchTestUtils {

    private ElasticsearchTestUtils() {

    }

    @SneakyThrows
    static void processBulkIndexRequest(final ElasticsearchClient esClient, final String indexAlias, final List<Map<String, Object>> source) {
        BulkRequest.Builder br = new BulkRequest.Builder();
        source.stream()
              .map(LinkedHashMap::new)
              .forEach(doc -> br.operations(op -> op
                    .index(idx -> idx
                          .index(indexAlias)
                          .id(doc.containsValue("_id") ? String.valueOf(doc.remove("_id")) : null)
                          .document(doc))));
        esClient.bulk(br.build());

        esClient.indices().refresh(idx -> idx.index(indexAlias));
    }
}
