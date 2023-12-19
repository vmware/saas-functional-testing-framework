/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.es;

import org.apache.commons.lang3.StringUtils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import com.vmware.test.functional.saas.es.ElasticsearchHealthHelper;

import co.elastic.clients.elasticsearch._types.mapping.DynamicMapping;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Routines to work with Elasticsearch.
 */
@Slf4j
public final class ElasticsearchUtils {

    private ElasticsearchUtils() {

    }

    /**
     * Create Elasticsearch index.
     * Index is created with settings and mappings loaded from elasticsearch bootstrap project.
     *
     * @param esClient ElasticsearchClient client.
     * @param index Index to be created in Elasticsearch.
     * @param settings .
     * @param mappings .
     * @param alias Index alias.
     */
    @SneakyThrows
    static void createIndex(final ElasticsearchClient esClient, final String index, final IndexSettings settings,
                                   final TypeMapping mappings, final String alias) {
        Preconditions.checkArgument(index != null && !index.isBlank(), "Index reference cannot be null/empty");

        if (ElasticsearchHealthHelper.checkHealth(esClient, index)) {
            log.info("Elasticsearch Index [{}] exists...moving on", index);
            return;
        }

        CreateIndexResponse result = esClient.indices().create(CreateIndexRequest.of(builder -> {
            if (StringUtils.isNotBlank(alias)) {
                builder.aliases(alias, aliasBuilder -> aliasBuilder.isWriteIndex(true));
            }
            if (settings != null) {
                builder.settings(settings);
            }
            if (mappings == null) {
                builder.mappings(mappingsBuilder -> mappingsBuilder.dynamic(DynamicMapping.True));
            } else {
                builder.mappings(mappings);
            }

            builder.index(index);
            return builder;
        }));
        if (result.acknowledged()) {
            log.info("Creating index [{}] with alias [{}] with configured settings and mappings", index, alias);
        } else {
            log.info("Creating index [{}] failed with error: {}", index, result);
            throw new RuntimeException(result.toString());
        }
    }
}
