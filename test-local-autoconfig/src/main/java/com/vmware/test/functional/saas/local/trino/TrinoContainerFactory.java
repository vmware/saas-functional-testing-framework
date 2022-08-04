/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.trino;

import java.time.Duration;
import java.util.function.Consumer;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.ContainerNetworkManager;
import com.vmware.test.functional.saas.trino.TrinoCatalogSpecs;
import com.vmware.test.functional.saas.local.CustomDockerContainer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.vmware.test.functional.saas.local.CustomDockerContainer.DEFAULT_WAIT_STRATEGY_TIMEOUT;
import static com.vmware.test.functional.saas.local.CustomDockerContainer.createDockerContainer;

/**
 * Class that 'registers' catalogs in trino before container startup.
 * Trino catalogs are defined by properties files which are loaded on startup.
 * This class creates temporary files defined by {@link TrinoCatalogSpecs}
 * and mounts them to the given trino container.
 */
@Slf4j
public class TrinoContainerFactory implements FactoryBean<CustomDockerContainer> {

    private static final int ADDITIONAL_WAIT_TIME = 2000;

    private final ServiceEndpoint trinoEndpoint;

    private final ContainerNetworkManager containerNetworkManager;

    private final TrinoCatalogCreator trinoCatalogCreator;

    private final Consumer<CustomDockerContainer> containerModifier;

    @Value("${trino.catalog.directory:/etc/trino/catalog/}")
    private String catalogDirectory;

    public TrinoContainerFactory(final ServiceEndpoint trinoEndpoint,
            ContainerNetworkManager containerNetworkManager,
            TrinoCatalogCreator trinoCatalogCreator,
            final Consumer<CustomDockerContainer> containerModifier) {
        this.trinoEndpoint = trinoEndpoint;
        this.containerNetworkManager = containerNetworkManager;
        this.trinoCatalogCreator = trinoCatalogCreator;
        this.containerModifier = containerModifier;
    }

    @Override
    public CustomDockerContainer getObject() {
        // create trino container with default waiting strategy
        final String logWaitRegex = "(.*) (SERVER STARTED) (.*)";
        final CustomDockerContainer trinoContainer = createDockerContainer(this.trinoEndpoint,
                this.containerNetworkManager,
                new CustomWaitStrategy(logWaitRegex, DEFAULT_WAIT_STRATEGY_TIMEOUT));
        mountCatalogs(trinoContainer);
        // Apply any custom modifications
        this.containerModifier.accept(trinoContainer);
        return trinoContainer;
    }

    /**
     * Mounts the created catalog files to the given trino container.
     *
     * @param trinoContainer The trino container to mount the files to.
     */
    private void mountCatalogs(final CustomDockerContainer trinoContainer) {
        this.trinoCatalogCreator.getCatalogFiles().forEach((fileName, mountableFile) -> {
                    log.info("Mounting catalog file [{}] to container [{}]", fileName, this.trinoEndpoint.getContainerConfig().getName());
                    trinoContainer.withCopyFileToContainer(mountableFile, this.catalogDirectory + fileName);
                }
        );
    }

    @Override
    public Class<?> getObjectType() {
        return CustomDockerContainer.class;
    }

    private static class CustomWaitStrategy extends LogMessageWaitStrategy {

        CustomWaitStrategy(final String regEx, final Duration startupTimeout) {
            super();
            this.withRegEx(regEx).withStartupTimeout(startupTimeout);
        }

        @Override
        @SneakyThrows(InterruptedException.class)
        public void waitUntilReady() {
            super.waitUntilReady();
            Thread.sleep(ADDITIONAL_WAIT_TIME);
        }
    }
}
