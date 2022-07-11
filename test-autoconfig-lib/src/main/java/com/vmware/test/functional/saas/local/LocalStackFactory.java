/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import com.vmware.test.functional.saas.ServiceEndpoint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vmware.test.functional.saas.local.ServiceConditionUtil.getRequiredServiceDependencies;

/**
 * Creates {@code LocalStackContainer} instances. The implementation keeps track of all
 * {@code LocalStackContainer} instances created by this JVM and starts a new one only if
 * any of the previous do not provide the required services.
 *
 * Note: there is a theoretical situation where the same service should be exposed
 * on two different ports. For now this use case is not supported and the factory
 * method will fail with "Required services are ALREADY provided" error.
 */
@Slf4j
@AllArgsConstructor
public class LocalStackFactory implements FactoryBean<LocalStackContainer> {

    private static final ConcurrentMap<LocalStackContainer, List<LocalStackServiceInfo>> localStackServicesByContainer = new ConcurrentHashMap<>();

    private final ConfigurableListableBeanFactory listableBeanFactory;
    private final ServiceEndpoint localStackServiceEndpoint;
    private final List<LocalStackContainer.Service> localstackServices;
    private final Consumer<LocalStackContainer> containerModifier;

    @Override
    public LocalStackContainer getObject() {
        // check if initialized localstack containers have required services started on the correct ports!--
        final List<LocalStackServiceInfo> requestedLocalStackServices = getRequiredServiceDependencies(this.listableBeanFactory).stream()
                .filter(srv -> this.localstackServices.contains(srv.getService()))
                .map(s -> new LocalStackServiceInfo(s.getService(), mapPortBinding(s)))
                .collect(Collectors.toList());
        final List<LocalStackServiceInfo> services =  diffServices(requestedLocalStackServices);

        if (services.isEmpty()) {
            final List<Integer> localStackContainersIdentities = localStackServicesByContainer.keySet().stream()
                    .map(System::identityHashCode)
                    .collect(Collectors.toList());
            log.error("Required services are ALREADY provided by localstack containers [{}].", localStackContainersIdentities);
            throw new IllegalStateException("Required services are ALREADY provided.");
        }

        log.info("Configuring [{}] container with services [{}]", this.localStackServiceEndpoint.getContainerConfig().getName(), services);
        return init(services);
    }

    @Override
    public Class<?> getObjectType() {
        return LocalStackContainer.class;
    }

    private String mapPortBinding(final LocalService service) {
        final int serviceEndpointPort = this.listableBeanFactory
                .getBean(service.getEndpoint(), ServiceEndpoint.class)
                .getPort();
        return String.format("%d:%d/%s", serviceEndpointPort, this.localStackServiceEndpoint.getContainerConfig().getPort(), InternetProtocol.TCP);
    }

    private LocalStackContainer init(final List<LocalStackServiceInfo> services) {
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("At least one service must be specified.");
        }

        final LocalStackContainer container = new LocalStackContainer(DockerImageName.parse(this.localStackServiceEndpoint.getContainerConfig().getImageName()))
                .withServices(services.stream().map(LocalStackServiceInfo::getService).toArray(LocalStackContainer.Service[]::new));
        container.setPortBindings(services.stream().map(LocalStackServiceInfo::getPortBinding).collect(Collectors.toList()));
        this.containerModifier.accept(container);
        log.info("localstack identity [{}]", System.identityHashCode(container));
        localStackServicesByContainer.put(container, services);
        return container;
    }

    private List<LocalStackServiceInfo> diffServices(final List<LocalStackServiceInfo> requestedServices) {
        return requestedServices.stream()
                .filter(rs -> localStackServicesByContainer.values().stream()
                                .flatMap(Collection::stream)
                                .noneMatch(existingServices -> existingServices.service.equals(rs.getService())
                                        && existingServices.getPortBinding().equals(rs.getPortBinding()))
                )
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    private static class LocalStackServiceInfo {
        @NonNull
        private LocalStackContainer.Service service;
        @NonNull
        private String portBinding;
    }
}
