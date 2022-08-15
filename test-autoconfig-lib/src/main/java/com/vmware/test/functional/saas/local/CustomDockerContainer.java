/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import java.time.Duration;
import java.util.Collections;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.RemoteDockerImage;

import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom Container extending testcontainers {@link GenericContainer}. To be used for configuring a container where a required service by
 * functional tests will be started in docker.
 */
@Slf4j
public final class CustomDockerContainer extends GenericContainer<CustomDockerContainer> {

    public static final Duration DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT = Duration.ofSeconds(480);
    public static final Duration DEFAULT_WAIT_STRATEGY_TIMEOUT = Duration.ofSeconds(480);

    CustomDockerContainer(final RemoteDockerImage image,
            final ServiceEndpoint serviceEndpoint) {
        super(image);

        this.withCreateContainerCmdModifier(cmd -> cmd.withName(serviceEndpoint.getContainerConfig().getName()));
        this.withStartupCheckStrategy(new IsRunningStartupCheckStrategy().withTimeout(DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT));
        this.setPortBindings(Collections.singletonList(configurePortBinding(serviceEndpoint)));
    }

    private static String configurePortBinding(final ServiceEndpoint serviceEndpoint) {
        return String.format("%d:%d/%s", serviceEndpoint.getPort(), serviceEndpoint.getContainerConfig().getPort(), InternetProtocol.TCP);
    }

    private static RemoteDockerImage initImage(final ServiceEndpoint serviceEndpoint) {
        // getDockerImage will lazy init docker driver
        try (GenericContainer<?> container = new GenericContainer<>(serviceEndpoint.getContainerConfig().getImageName())) {
            log.info("Configuring docker container [{}] from image [{}] on host port: [{}]",
                    serviceEndpoint.getContainerConfig().getName(), container.getDockerImageName(), serviceEndpoint.getPort());
            return container.getImage();
        }
    }

    /**
     * Returns a @code CustomDockerContainer} instance. The actual docker container is not started.
     * @param serviceEndpoint the {@link ServiceEndpoint} that will be started in the container
     * @param waitStrategy a custom {@link WaitStrategy} that is applied on container startup
     * @return a {@link CustomDockerContainer}
     */
    public static CustomDockerContainer createDockerContainer(final ServiceEndpoint serviceEndpoint,
            final ContainerNetworkManager containerNetworkManager,
            final WaitStrategy waitStrategy) {
        final RemoteDockerImage dockerImage = initImage(serviceEndpoint);
        try (CustomDockerContainer customDockerContainer = new CustomDockerContainer(
                dockerImage,
                serviceEndpoint)) {
            customDockerContainer
                    .withExposedPorts(serviceEndpoint.getContainerConfig().getPort())
                    .withNetwork(containerNetworkManager.getNetwork(serviceEndpoint.getContainerConfig().getNetworkInfo().getName()))
                    .waitingFor(waitStrategy);

            log.info("Container [{}] has identity [{}]", serviceEndpoint.getContainerConfig().getName(), System.identityHashCode(customDockerContainer));
            return customDockerContainer;
        }
    }
}

