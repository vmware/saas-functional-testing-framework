/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.service;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.aw.dpa.test.InternalContainerServiceConfig;
import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.constants.DockerConfig;
import com.aw.dpa.test.aws.local.constants.DockerContainerType;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * FactoryBean for LocalServiceEndpoint.
 */
@RequiredArgsConstructor
class LocalServiceEndpointFactoryBean implements FactoryBean<LocalServiceEndpoint> {

    @NonNull
    protected final DockerContainerType dockerContainerType;
    @NonNull
    protected final Integer containerNameSuffix;
    @NonNull
    protected final String schema;
    @NonNull
    protected final String host;
    protected final Integer port;

    @Autowired
    private DockerConfig dockerConfig;

    LocalServiceEndpointFactoryBean(final DockerContainerType dockerContainerType,
            final Integer containerNameSuffix,
            final String schema,
            final String host) {
        this(dockerContainerType, containerNameSuffix, schema, host, null);
    }

    @Override
    public LocalServiceEndpoint getObject() {
        if (this.port == null) {
            return new LocalServiceEndpoint(this.schema, this.host, getInternalContainerServiceConfig());
        }
        return new LocalServiceEndpoint(this.port, this.schema, this.host, getInternalContainerServiceConfig());
    }

    @Override
    public Class<?> getObjectType() {
        return LocalServiceEndpoint.class;
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
