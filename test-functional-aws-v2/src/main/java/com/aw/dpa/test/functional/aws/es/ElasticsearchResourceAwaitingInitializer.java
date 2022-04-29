/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.es;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.functional.aws.AbstractAwsResourceAwaitingInitializer;

import io.searchbox.client.JestClient;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies Elasticsearch indices, provided by {@link ElasticsearchIndexBuildConfiguration},
 * exist when started.
 */
@Slf4j
public class ElasticsearchResourceAwaitingInitializer extends AbstractAwsResourceAwaitingInitializer {

    private final JestClient jestClient;

    public ElasticsearchResourceAwaitingInitializer(final JestClient jestClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.jestClient = jestClient;
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
        await().until(() -> ElasticsearchHealthHelper.checkHealth(this.jestClient, elasticsearchIndexSettings.getIndex()));
        log.info("Verified ES index [{}] exists", elasticsearchIndexSettings.getIndex());
    }
}
