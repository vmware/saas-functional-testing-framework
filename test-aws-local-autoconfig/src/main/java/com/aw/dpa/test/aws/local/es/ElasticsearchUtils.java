/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.es;

import org.apache.commons.lang3.StringUtils;

import com.aw.dpa.test.functional.aws.es.ElasticsearchHealthHelper;
import com.google.common.base.Preconditions;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Routines to work with Elasticsearch.
 */
@Slf4j
public final class ElasticsearchUtils {

    private static final String EMPTY_ELASTICSEARCH_SETTINGS = "";
    private static final String DYNAMIC_INDEX_MAPPINGS = "{\"default\":{\"dynamic\":\"true\"}}";

    private ElasticsearchUtils() {

    }

    static String emptyIndexSettings() {
        return EMPTY_ELASTICSEARCH_SETTINGS;
    }

    static String dynamicIndexMappings() {
        return DYNAMIC_INDEX_MAPPINGS;
    }

    /**
     * Create Elasticsearch index.
     * Index is created with settings and mappings loaded from elasticsearch bootstrap project.
     *
     * @param jestClient JestClient client.
     * @param index Index to be created in Elasticsearch.
     * @param settings .
     * @param mappings .
     * @param alias Index alias.
     */
    @SneakyThrows
    static void createIndex(final JestClient jestClient, final String index, final String settings,
                                   final String mappings, final String alias) {
        Preconditions.checkArgument(index != null && !index.isBlank(), "Index reference cannot be null/empty");

        if (ElasticsearchHealthHelper.checkHealth(jestClient, index)) {
            log.info("Elasticsearch Index [{}] exists...moving on", index);
            return;
        }

        final io.searchbox.indices.CreateIndex.Builder createIndexBuilder = new io.searchbox.indices.CreateIndex.Builder(index);

        if (StringUtils.isNotBlank(alias)) {
            final String json = "{\"" + alias + "\":{ \"" + "is_write_index" + "\" : true }}";
            createIndexBuilder.aliases(json);
        }

        if (StringUtils.isNotBlank(settings)) {
            createIndexBuilder.settings(settings);
        }

        createIndexBuilder.mappings(mappings);
        final JestResult result = jestClient.execute(createIndexBuilder.build());
        if (result.isSucceeded()) {
            log.info("Creating index [{}] with alias [{}] with configured settings and mappings", index, alias);
        } else {
            log.info("Creating index [{}] failed with error: {}", index, result.getErrorMessage());
            throw new RuntimeException(result.getErrorMessage());
        }
    }
}
