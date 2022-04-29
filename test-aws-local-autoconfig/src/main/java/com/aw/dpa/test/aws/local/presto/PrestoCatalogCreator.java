/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.presto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;
import org.testcontainers.utility.MountableFile;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.SmartLifecyclePhases;
import com.aw.dpa.test.functional.aws.presto.PrestoCatalogSettings;
import com.aw.dpa.test.functional.aws.presto.PrestoCatalogSpecs;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates temporary files that define presto catalogs.
 */
@Slf4j
public class PrestoCatalogCreator implements SmartLifecycle {

    private static final String CATALOG_NAME_FORMAT = "%s.properties";

    @Getter
    private final Map<String, MountableFile> catalogFiles;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;

    private final List<PrestoCatalogSpecs> catalogSpecs;

    private final LocalServiceEndpoint prestoEndpoint;

    public PrestoCatalogCreator(
            final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final List<PrestoCatalogSpecs> catalogSpecs,
            final LocalServiceEndpoint prestoEndpoint) {
        this.functionalTestExecutionSettings = functionalTestExecutionSettings;
        this.catalogSpecs = catalogSpecs;
        this.prestoEndpoint = prestoEndpoint;
        this.catalogFiles = new HashMap<>();
    }

    @Override
    public void start() {
        if (this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip creating catalogs");
            return;
        }
        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            log.warn("Presto catalog creator already started.");
            return;
        }
        if (CollectionUtils.isEmpty(this.catalogSpecs)) {
            log.info("No catalog specs provided.");
            return;
        }
        // create all presto catalog files and attach them to the container.
        this.catalogSpecs.stream()
                .flatMap(spec -> spec.getCatalogs().stream())
                .forEach(this::createPrestoCatalog);
    }

    private void createPrestoCatalog(final PrestoCatalogSettings catalogSettings) {
        final String fileName = String.format(CATALOG_NAME_FORMAT, catalogSettings.getName());
        final File tmpCatalogPropertiesFile = new File(FileUtils.getTempDirectory(), fileName);
        log.info("Creating catalog properties [{}] for container [{}]", fileName, this.prestoEndpoint.getContainerConfig().getName());
        try {
            FileUtils.forceDeleteOnExit(tmpCatalogPropertiesFile);
            final List<String> lines = catalogSettings.getProperties().entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            Files.write(tmpCatalogPropertiesFile.toPath(), lines, StandardCharsets.ISO_8859_1);
            this.catalogFiles.put(fileName, MountableFile.forHostPath(tmpCatalogPropertiesFile.getPath()));
        } catch (final IOException ioException) {
            throw new RuntimeException(
                    String.format("An error occurred while working with temporary file [%s] for catalog [%s]", tmpCatalogPropertiesFile.getPath(), catalogSettings.getName()),
                    ioException);
        }
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            log.warn("Presto catalog creator already stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    // Run phase for this is the smallest because we want it to run before any containers are started
    // by the GenericRunner class. That's because presto catalogs must be defined as properties files
    // and available to Presto before it's started.
    @Override
    public int getPhase() {
        return SmartLifecyclePhases.PRESTO_CATALOG_CREATOR.getPhase();
    }
}
