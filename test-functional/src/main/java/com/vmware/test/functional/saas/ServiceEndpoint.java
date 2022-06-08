/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Model for Local Service Endpoint.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceEndpoint implements BeanNameAware, EnvironmentAware, InitializingBean {

    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String DEFAULT_HEALTH_ENDPOINT_PATH = "/health";

    private static final String ENDPOINT_FORMAT = "%s://%s:%s";

    @NonNull
    private final PortSupplier portSupplier;
    @NonNull
    private final String scheme;
    @NonNull
    private final String hostName;
    @Getter
    private final InternalContainerServiceConfig containerConfig;

    @Setter
    private String beanName;
    @Setter
    private Environment environment;

    // Dynamic port allocation
    public ServiceEndpoint(final String scheme) {
        this(new PortSupplier(), scheme, DEFAULT_HOSTNAME, null);
    }

    public ServiceEndpoint(final String scheme, final String hostName) {
        this(new PortSupplier(), scheme, hostName, null);
    }

    public ServiceEndpoint(final String scheme, final String hostName, final InternalContainerServiceConfig containerConfig) {
        this(new PortSupplier(), scheme, hostName, containerConfig);
    }

    // Fixed port allocation
    public ServiceEndpoint(final int port, final String scheme) {
        this(new PortSupplier(port), scheme, DEFAULT_HOSTNAME, null);
    }

    public ServiceEndpoint(final int port, final String scheme, final String hostName) {
        this(new PortSupplier(port), scheme, hostName, null);
    }

    public ServiceEndpoint(final int port, final String scheme, final String hostName, final InternalContainerServiceConfig containerConfig) {
        this(new PortSupplier(port), scheme, hostName, containerConfig);
    }

    /**
     * Returns the port associated with the endpoint.
     *
     * @return The port.
     */
    public int getPort() {
        return this.portSupplier.getAsInt();
    }

    /**
     * Returns the hostName associated with the endpoint.
     *
     * @return The hostName.
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Retrieve local service endpoint. Enable use of default port for each service.
     *
     * @return Local service endpoint.
     */
    public String getEndpoint() {
        return String.format(ENDPOINT_FORMAT, StringUtils.defaultIfBlank(this.scheme, DEFAULT_SCHEME), this.hostName, this.portSupplier.getAsInt());
    }

    /**
     * Retrieve internal service endpoint. Can be used when the containers run in the same user defined docker network.
     *
     * @return Local service endpoint.
     */
    public String getInternalDockerEndpoint() {
        if (this.containerConfig == null) {
            final String hostEndpoint = getEndpoint();
            log.warn("Service not running in docker or missing container configuration - returning host endpoint: [{}]", hostEndpoint);
            return hostEndpoint;
        }
        return String.format(ENDPOINT_FORMAT, StringUtils.defaultIfBlank(this.scheme, DEFAULT_SCHEME), this.containerConfig.getName(), this.containerConfig.getPort());
    }

    @Override
    public void afterPropertiesSet() {
        final String portOverridePropertyName = String.format("%s.port.override", this.beanName);
        if (this.environment.containsProperty(portOverridePropertyName)) {
            final Integer portOverride = this.environment.getRequiredProperty(portOverridePropertyName, Integer.class);
            log.info("Overriding port value for {} - new value [{}] old value [{}]",
                    this.beanName, portOverride, this.portSupplier.port);
            this.portSupplier.port = portOverride;
        } else {
            log.info("{} was allocated port: [{}]", this.beanName, this.portSupplier.getAsInt());
        }
    }
}
