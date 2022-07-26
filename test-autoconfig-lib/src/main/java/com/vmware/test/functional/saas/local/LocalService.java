/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import org.testcontainers.containers.localstack.LocalStackContainer;

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
        boolean localstackService;

        String getName() {
            return service.name();
        }
    }
}
