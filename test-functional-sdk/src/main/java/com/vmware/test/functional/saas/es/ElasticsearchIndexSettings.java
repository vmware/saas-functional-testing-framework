/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.es;

import java.util.function.Supplier;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import lombok.Builder;
import lombok.Data;

/**
 * Local Elasticsearch index configuration settings.
 */
@Builder
@Data
public class ElasticsearchIndexSettings {

    private String index;
    @Builder.Default
    private String indexAlias = "";
    @Builder.Default
    private Supplier<TypeMapping> indexMappingsSupplier = () -> null;
    @Builder.Default
    private Supplier<IndexSettings> indexSettingsSupplier = () -> null;
}
