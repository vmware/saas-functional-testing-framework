/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas;

import org.apache.commons.lang3.RandomUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Model for containerized service's internal endpoint.
 */
@Getter
public class InternalContainerServiceConfig {

    private static final String NETWORK_NAME = "test_network_" + RandomUtils.nextInt();

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
        this.networkInfo = new NetworkInfo(NETWORK_NAME);
    }

    /**
     * Information about the network the containerized service is connected to.
     */
    @Getter
    @AllArgsConstructor
    public static class NetworkInfo {

        @NonNull
        private final String name;
    }
}
