/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.es;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.searchbox.client.JestClient;
import io.searchbox.indices.Refresh;

import lombok.SneakyThrows;

/**
 * Routines for Elasticsearch tests.
 */
public final class ElasticsearchTestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ElasticsearchTestUtils() {

    }

    @SneakyThrows
    static void processBulkIndexRequest(final JestClient jestClient, final String indexAlias, final List<Map<String, Object>> source) {
        final io.searchbox.core.Bulk.Builder bulkBuilder = new io.searchbox.core.Bulk.Builder();
        source.forEach((doc) -> {
            final Map<String, Object> sourceMap = new LinkedHashMap<>(doc);
            final Object id = sourceMap.remove("_id");
            try {
                bulkBuilder.addAction(new io.searchbox.core.Index.Builder(OBJECT_MAPPER.writeValueAsString(sourceMap))
                        .index(indexAlias)
                        .type("default")
                        .id(id == null ? null : String.valueOf(id))
                        .build());
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        });
        jestClient.execute(bulkBuilder.build());

        final Refresh request = new io.searchbox.indices.Refresh.Builder().addIndex(indexAlias).build();
        jestClient.execute(request);
    }
}
