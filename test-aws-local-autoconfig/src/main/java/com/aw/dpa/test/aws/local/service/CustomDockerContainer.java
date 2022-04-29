/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.service;

import java.time.Duration;
import java.util.Collections;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.RemoteDockerImage;

import com.aw.dpa.test.LocalServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom Container extending testcontainers {@link GenericContainer}. To be used for configuring a container where a required service by
 * functional tests will be started in docker.
 */
@Slf4j
public final class CustomDockerContainer extends GenericContainer<CustomDockerContainer> {

    public static final Duration DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT = Duration.ofSeconds(120);
    public static final Duration DEFAULT_WAIT_STRATEGY_TIMEOUT = Duration.ofSeconds(120);

    CustomDockerContainer(final RemoteDockerImage image,
            final LocalServiceEndpoint serviceEndpoint) {
        super(image);

        this.withCreateContainerCmdModifier(cmd -> cmd.withName(serviceEndpoint.getContainerConfig().getName()));
        this.withStartupCheckStrategy(new IsRunningStartupCheckStrategy().withTimeout(DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT));
        this.setPortBindings(Collections.singletonList(configurePortBinding(serviceEndpoint)));
    }

    private static String configurePortBinding(final LocalServiceEndpoint localServiceEndpoint) {
        return String.format("%d:%d/%s", localServiceEndpoint.getPort(), localServiceEndpoint.getContainerConfig().getPort(), InternetProtocol.TCP);
    }

    private static RemoteDockerImage initImage(final LocalServiceEndpoint localServiceEndpoint) {
        // getDockerImage will lazy init docker driver
        try (GenericContainer<?> container = new GenericContainer<>(localServiceEndpoint.getContainerConfig().getImageName())) {
            log.info("Configuring docker container [{}] from image [{}] on host port: [{}]",
                    localServiceEndpoint.getContainerConfig().getName(), container.getDockerImageName(), localServiceEndpoint.getPort());
            return container.getImage();
        }
    }

    /**
     * Returns a @code CustomDockerContainer} instance. The actual docker container is not started.
     * @param serviceEndpoint the {@link LocalServiceEndpoint} that will be started in the container
     * @param waitStrategy a custom {@link WaitStrategy} that is applied on container startup
     * @return a {@link CustomDockerContainer}
     */
    public static CustomDockerContainer createDockerContainer(final LocalServiceEndpoint serviceEndpoint,
            final WaitStrategy waitStrategy) {
        final RemoteDockerImage dockerImage = initImage(serviceEndpoint);
        try (CustomDockerContainer customDockerContainer = new CustomDockerContainer(
                dockerImage,
                serviceEndpoint)) {
            customDockerContainer
                    .withExposedPorts(serviceEndpoint.getContainerConfig().getPort())
                    .withNetwork(serviceEndpoint.getContainerConfig().getNetworkInfo().getNetwork())
                    .waitingFor(waitStrategy);

            log.info("Container [{}] has identity [{}]", serviceEndpoint.getContainerConfig().getName(), System.identityHashCode(customDockerContainer));
            return customDockerContainer;
        }
    }
}

