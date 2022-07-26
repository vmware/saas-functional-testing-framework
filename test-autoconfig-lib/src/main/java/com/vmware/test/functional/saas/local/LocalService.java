/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents the services to be started locally. Internally used to bridge declaring functional tests service
 * dependencies.
 */
@Getter
public class LocalService {
    private final String name;
    private final String endpoint;
    private final Integer port;
    private final String scheme;
    private final LocalStackContainer.Service service;

    private final InternalContainerServiceConfig internalContainerServiceConfig;

    LocalService(final String name, final String endpointName, final InternalContainerServiceConfig internalContainerServiceConfig,
          final LocalStackContainer.Service service, final int port, final String scheme) {
        this.name = name;
        this.endpoint = endpointName;
        this.internalContainerServiceConfig = internalContainerServiceConfig;
        this.port = port;
        this.service = service;
        this.scheme = scheme;
    }

    LocalService(final String name, final String endpointName, final InternalContainerServiceConfig internalContainerServiceConfig, final int port) {
        this(name, endpointName, internalContainerServiceConfig, null, port, ServiceEndpoint.DEFAULT_SCHEME);
    }

    LocalService(final String name, final String endpointName, final InternalContainerServiceConfig internalContainerServiceConfig, final LocalStackContainer.Service service, final int port) {
        this(name, endpointName, internalContainerServiceConfig, service, port, ServiceEndpoint.DEFAULT_SCHEME);
    }

    @Builder
    @Getter
    static class BeanInfo {
        String endpointName;
        String name;
        Object beanRef;
        boolean localstackService;
    }
}
