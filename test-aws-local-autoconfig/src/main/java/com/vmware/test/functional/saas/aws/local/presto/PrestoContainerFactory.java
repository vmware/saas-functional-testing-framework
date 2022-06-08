/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.presto;

import java.time.Duration;
import java.util.function.Consumer;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.CustomDockerContainer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.vmware.test.functional.saas.local.CustomDockerContainer.DEFAULT_WAIT_STRATEGY_TIMEOUT;
import static com.vmware.test.functional.saas.local.CustomDockerContainer.createDockerContainer;

/**
 * Class that 'registers' catalogs in presto before container startup.
 * Presto catalogs are defined by properties files which are loaded on startup.
 * This class creates temporary files defined by {@link com.vmware.test.functional.saas.aws.presto.PrestoCatalogSpecs}
 * and mounts them to the given presto container.
 */
@Slf4j
public class PrestoContainerFactory implements FactoryBean<CustomDockerContainer> {

    private static final int ADDITIONAL_WAIT_TIME = 2000;

    private final ServiceEndpoint prestoEndpoint;

    private final Consumer<CustomDockerContainer> containerModifier;

    @Autowired //FIXME must be defined in a valid spring bean!
    private PrestoCatalogCreator prestoCatalogCreator;

    @Value("${presto.catalog.directory:/etc/trino/catalog/}")
    private String catalogDirectory;

    public PrestoContainerFactory(final ServiceEndpoint prestoEndpoint,
            final Consumer<CustomDockerContainer> containerModifier) {
        this.prestoEndpoint = prestoEndpoint;
        this.containerModifier = containerModifier;
    }

    @Override
    public CustomDockerContainer getObject() {
        // create presto container with default waiting strategy
        final String logWaitRegex = "(.*) (SERVER STARTED) (.*)";
        final CustomDockerContainer prestoContainer = createDockerContainer(this.prestoEndpoint,
                new CustomWaitStrategy(logWaitRegex, DEFAULT_WAIT_STRATEGY_TIMEOUT));
        mountCatalogs(prestoContainer);
        // Apply any custom modifications
        this.containerModifier.accept(prestoContainer);
        return prestoContainer;
    }

    /**
     * Mounts the created catalog files to the given presto container.
     *
     * @param prestoContainer The presto container to mount the files to.
     */
    private void mountCatalogs(final CustomDockerContainer prestoContainer) {
        this.prestoCatalogCreator.getCatalogFiles().forEach((fileName, mountableFile) -> {
                    log.info("Mounting catalog file [{}] to container [{}]", fileName, this.prestoEndpoint.getContainerConfig().getName());
                    prestoContainer.withCopyFileToContainer(mountableFile, this.catalogDirectory + fileName);
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
