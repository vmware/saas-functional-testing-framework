/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.es;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.common.AbstractResourceAwaitingInitializer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies Elasticsearch indices, provided by {@link ElasticsearchIndexBuildConfiguration},
 * exist when started.
 */
@Slf4j
public class ElasticsearchResourceAwaitingInitializer extends AbstractResourceAwaitingInitializer {

    private final ElasticsearchClient esClient;

    public ElasticsearchResourceAwaitingInitializer(final ElasticsearchClient esClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.esClient = esClient;
    }

    @Override
    public void doStart() {
        final List<ElasticsearchIndexBuildConfiguration> esIndexBuildConfigurations = new ArrayList<>(
                getContext().getBeansOfType(ElasticsearchIndexBuildConfiguration.class).values());
        if (!esIndexBuildConfigurations.isEmpty()) {
            log.debug("Verifying Elasticsearch indices exist from {}", esIndexBuildConfigurations);
            esIndexBuildConfigurations.stream()
                    .map(ElasticsearchIndexBuildConfiguration::getIndicesToCreate)
                    .flatMap(Collection::stream)
                    .forEach(this::verifyIndex);
        }
    }

    private void verifyIndex(final ElasticsearchIndexSettings elasticsearchIndexSettings) {
        await().until(() -> ElasticsearchHealthHelper.checkHealth(this.esClient, elasticsearchIndexSettings.getIndex()));
        log.info("Verified ES index [{}] exists", elasticsearchIndexSettings.getIndex());
    }
}
