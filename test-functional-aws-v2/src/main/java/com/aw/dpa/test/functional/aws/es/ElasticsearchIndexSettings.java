/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.es;

import java.util.function.Supplier;

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
    private Supplier<String> indexMappingsSupplier = () -> "{\"default\":{\"dynamic\":\"true\"}}";
    @Builder.Default
    private Supplier<String> indexSettingsSupplier = () -> "";
}
