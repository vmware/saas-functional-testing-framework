/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents the services to be started locally. Internally used to bridge declaring functional tests service
 * dependencies.
 */
@Getter
public class LocalService {

    private final Integer port;
    private final String scheme;

    private final InternalContainerServiceConfig internalContainerServiceConfig;

    LocalService(final InternalContainerServiceConfig internalContainerServiceConfig, final int port, final String scheme) {
        this.internalContainerServiceConfig = internalContainerServiceConfig;
        this.port = port;
        this.scheme = scheme;
    }

    LocalService(final InternalContainerServiceConfig internalContainerServiceConfig, final int port) {
        this(internalContainerServiceConfig, port, ServiceEndpoint.DEFAULT_SCHEME);
    }

    @Builder
    @Getter
    static class BeanInfo {
        String endpointName;
        Object beanRef;
        Service service;

        String getName() {
            return service.name();
        }
    }
}
