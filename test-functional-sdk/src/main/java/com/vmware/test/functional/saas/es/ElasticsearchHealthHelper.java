/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.es;

import io.searchbox.client.JestClient;
import io.searchbox.indices.IndicesExists;

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
     * @param jestClient {@link JestClient}.
     * @param index               The Elasticsearch index name.
     * @return {@code true} if the index exists, {@code true} if index is not created.
     */
    @SneakyThrows
    public static boolean checkHealth(final JestClient jestClient, final String index) {
        final IndicesExists request = (new IndicesExists.Builder(index)).build();
        return jestClient.execute(request).isSucceeded();
    }
}
