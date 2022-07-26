/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * FactoryBean for LocalServiceEndpoint.
 */
@RequiredArgsConstructor
class LocalServiceEndpointFactoryBean implements FactoryBean<ServiceEndpoint> {

    @NotNull
    protected final LocalService localService;
    @NonNull
    protected final Integer containerNameSuffix;
    @NonNull
    protected final String host;
    @NonNull
    protected final Boolean defaultPortsEnabled;

    @Override
    public ServiceEndpoint getObject() {
        if (this.defaultPortsEnabled) {
            return new ServiceEndpoint(this.localService.getPort(), this.localService.getScheme(), this.host,
                  getInternalContainerServiceConfig());

        }
        return new ServiceEndpoint(this.localService.getScheme(), this.host, getInternalContainerServiceConfig());
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
        final InternalContainerServiceConfig explicitContainerServiceConfig = this.localService.getInternalContainerServiceConfig();
        if (explicitContainerServiceConfig != null) {
            return new InternalContainerServiceConfig(
                  explicitContainerServiceConfig.getImageName(),
                  explicitContainerServiceConfig.getName() + "-" + containerNameSuffix,
                  explicitContainerServiceConfig.getPort()
            );
        }
        return new InternalContainerServiceConfig("Unknown", "unknown", -1);
    }
}
