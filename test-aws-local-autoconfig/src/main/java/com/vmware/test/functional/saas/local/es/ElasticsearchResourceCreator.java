/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.es;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;
import com.vmware.test.functional.saas.es.ElasticsearchIndexBuildConfiguration;
import com.vmware.test.functional.saas.es.ElasticsearchIndexSettings;

import io.searchbox.client.JestClient;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link ElasticsearchIndexBuildConfiguration Elasticsearch indices}
 * when started.
 */
@Slf4j
public class ElasticsearchResourceCreator extends AbstractResourceCreator {

    private final JestClient jestClient;

    public ElasticsearchResourceCreator(final JestClient jestClient,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.jestClient = jestClient;
    }

    @Override
    protected void doStart() {
        final List<ElasticsearchIndexBuildConfiguration> esIndexBuildConfigurations = new ArrayList<>(
                getContext().getBeansOfType(ElasticsearchIndexBuildConfiguration.class).values());
        if (!esIndexBuildConfigurations.isEmpty()) {
            initializeIndices(esIndexBuildConfigurations);
        }
    }

    private void initializeIndices(final List<ElasticsearchIndexBuildConfiguration> esIndexBuildConfigurations) {
        log.debug("Creating ES indices using {}", esIndexBuildConfigurations);
        esIndexBuildConfigurations.stream()
                .map(ElasticsearchIndexBuildConfiguration::getIndicesToCreate)
                .flatMap(Collection::stream)
                .forEach(this::createIndex);
    }

    private void createIndex(final ElasticsearchIndexSettings elasticsearchIndexSettings) {
        final String index = elasticsearchIndexSettings.getIndex();
        try {
            ElasticsearchUtils.createIndex(this.jestClient, index,
                    elasticsearchIndexSettings.getIndexSettingsSupplier().get(),
                    elasticsearchIndexSettings.getIndexMappingsSupplier().get(),
                    elasticsearchIndexSettings.getIndexAlias());
            log.info("ES index [{}] created", index);
        } catch (final Exception e) {
            log.info("ES index [{}] creation failed. Checking host resources for more information.", index);
             // Due to INTEL-33901 a check and logging of host resources is done so that next time intermittent test failure happens, there are more details
            checkHostResources();
            throw e;
        }
    }

    @SneakyThrows
    private void checkHostResources() {
        // display the heading of ps aux output to help read second command more easily
        final List<Process> printPsHeadingCmd = ProcessBuilder.startPipeline(Arrays.asList(
                new ProcessBuilder("ps", "aux"),
                new ProcessBuilder("head", "-1")));

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(printPsHeadingCmd.get(printPsHeadingCmd.size() - 1).getInputStream(), StandardCharsets.UTF_8))) {
            log.info(br.lines().collect(Collectors.joining("\n")));
        }

        // sort the output from ps aux command in reverse numeric order based on the fourth column (memory usage)
        final List<Process> sortedProcessesByMemCmd = ProcessBuilder.startPipeline(Arrays.asList(
                new ProcessBuilder("ps", "aux"),
                new ProcessBuilder("sort", "-rnk4"),
                new ProcessBuilder("head", "-50")));
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(sortedProcessesByMemCmd.get(sortedProcessesByMemCmd.size() - 1).getInputStream(), StandardCharsets.UTF_8))) {
            log.info(br.lines().collect(Collectors.joining("\n")));
        }
    }
}
