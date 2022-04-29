/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test;

import org.apache.commons.lang3.RandomUtils;
import org.testcontainers.containers.Network;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Model for containerized service's internal endpoint.
 */
@Getter
public class InternalContainerServiceConfig {

    private static final String NETWORK_NAME = "dpa_test_network_" + RandomUtils.nextInt();

    private static final Network DPA_TEST_NETWORK = Network.builder()
            .createNetworkCmdModifier(cmd -> cmd
                    .withName(NETWORK_NAME))
            .build();

    // Calling getId() makes testContainers to create the network.
    static {
        DPA_TEST_NETWORK.getId();
    }

    @NonNull
    private final String imageName;

    @NonNull
    private final String name;

    private final int port;

    private final NetworkInfo networkInfo;

    public InternalContainerServiceConfig(@NonNull final String imageName, @NonNull final String name, final int port) {
        this.name = name;
        this.port = port;
        this.imageName = imageName;
        this.networkInfo = new NetworkInfo(DPA_TEST_NETWORK, NETWORK_NAME);
    }

    /**
     * Class used for holding information about custom docker network created for each
     * internal container service.
     */
    @Getter
    @AllArgsConstructor
    public static class NetworkInfo {

        @NonNull
        private final Network network;
        @NonNull
        private final String name;
    }
}
