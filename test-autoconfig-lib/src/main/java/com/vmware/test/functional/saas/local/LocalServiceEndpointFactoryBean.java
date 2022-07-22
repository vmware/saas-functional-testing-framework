/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * FactoryBean for LocalServiceEndpoint.
 */
@RequiredArgsConstructor
class LocalServiceEndpointFactoryBean implements FactoryBean<ServiceEndpoint> {

    @NonNull
    protected final DockerContainerType dockerContainerType;
    @NonNull
    protected final Integer containerNameSuffix;
    @NonNull
    protected final String schema;
    @NonNull
    protected final String host;
    protected final Integer port;

    @Autowired //FIXME must be defined in a valid spring bean!
    private DockerConfig dockerConfig;

    LocalServiceEndpointFactoryBean(final DockerContainerType dockerContainerType,
                                    final Integer containerNameSuffix,
                                    final String schema,
                                    final String host) {
        this(dockerContainerType, containerNameSuffix, schema, host, null);
    }

    @Override
    public ServiceEndpoint getObject() {
        if (this.port == null) {
            return new ServiceEndpoint(this.schema, this.host, getInternalContainerServiceConfig());
        }
        return new ServiceEndpoint(this.port, this.schema, this.host, getInternalContainerServiceConfig());
    }

    @Override
    public Class<?> getObjectType() {
        return ServiceEndpoint.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

    protected final InternalContainerServiceConfig getInternalContainerServiceConfig() {
        final int internalContainerPort = this.dockerContainerType.getInternalDockerPortMapper().apply(this.dockerConfig);
        final String containerImageName = this.dockerContainerType.getDockerImageNameMapper().apply(this.dockerConfig);
        return new InternalContainerServiceConfig(
                containerImageName,
                this.dockerContainerType.name().toLowerCase() + "-" + this.containerNameSuffix,
                internalContainerPort);
    }
}
