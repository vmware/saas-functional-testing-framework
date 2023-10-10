/*
 * Copyright 2022-2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.localstack;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.LocalService;

import lombok.extern.slf4j.Slf4j;

import static com.vmware.test.functional.saas.local.localstack.LocalstackConstants.LOCALSTACK_DEFAULT_SERVICE_PORT;
import static com.vmware.test.functional.saas.local.localstack.LocalstackConstants.LOCALSTACK_IMAGE_NAME;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.constraints.NotNull;

/**
 * Register localstack services to be provisioned by LocalStack container.
 */
// Refactor
@Slf4j
public class LocalstackContainerServiceRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    public static final String BEAN_NAME_SUFFIX = "LocalStack";

    private final int containerNameSuffix = RandomUtils.nextInt();
    private Environment environment;

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        if (!(registry instanceof ConfigurableListableBeanFactory)) {
            return;
        }
        final ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory)registry;
        this.lookupRequiredLocalstackServices(beanFactory)
              .stream()
              .filter(not(service -> isEndpointDefined(service, beanFactory)))
              .forEach(service -> addLocalStackContainerServiceBeanDefinition(service, registry));

        /*
            TODO: ???
            if (services.size() > 0) {
                addLocalStackContainerEndpointBeanDef(registry);
            }
        */
    }

    private Set<Service> lookupRequiredLocalstackServices(final ConfigurableListableBeanFactory beanFactory) {
        final List<String> localstackServices = LocalstackUtil.getLocalstackServices(this.environment);
        final Set<Service> services = LocalstackUtil.lookupRequiredLocalstackServices(beanFactory);
        services.removeIf(service -> localstackServices.contains(service.name()));
        return services;
    }

    private void addLocalStackContainerServiceBeanDefinition(final Service service,
          final BeanDefinitionRegistry registry) {

        final String beanName = service.name() + BEAN_NAME_SUFFIX;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        final RootBeanDefinition definition = new RootBeanDefinition(LocalstackContainerServiceFactoryBean.class);
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        final ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
        constructorArguments.addIndexedArgumentValue(0, service);
        registry.registerBeanDefinition(beanName, definition);
    }

    private void addLocalStackContainerEndpointBeanDef(final BeanDefinitionRegistry registry) {
        log.info("Adding local service bean definition for Localstack");
        final RootBeanDefinition definitionService = new RootBeanDefinition(LocalService.class);
        definitionService.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        final ConstructorArgumentValues serviceConstructorArguments = definitionService.getConstructorArgumentValues();
        int argIndex = 0;
        serviceConstructorArguments.addIndexedArgumentValue(argIndex++,
              new InternalContainerServiceConfig(
                    LOCALSTACK_IMAGE_NAME,
                    "localstack",
                    LOCALSTACK_DEFAULT_SERVICE_PORT));
        serviceConstructorArguments.addIndexedArgumentValue(argIndex, LOCALSTACK_DEFAULT_SERVICE_PORT);

        registry.registerBeanDefinition("_LOCALSTACK", definitionService);

        log.info("Adding local endpoint bean definition for Localstack");
        final RootBeanDefinition definitionEndpoint = new RootBeanDefinition(LocalStackContainerEndpointFactoryBean.class);
        definitionEndpoint.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        final ConstructorArgumentValues endpointConstructorArguments = definitionEndpoint.getConstructorArgumentValues();
        endpointConstructorArguments.addIndexedArgumentValue(0, new RuntimeBeanReference("_LOCALSTACK"));
        registry.registerBeanDefinition(LocalstackConstants.LOCALSTACK_ENDPOINT, definitionEndpoint);
    }

    private boolean isEndpointDefined(final Service service,
          final ConfigurableListableBeanFactory configurableListableBeanFactory) {
        ConfigurableListableBeanFactory parent = configurableListableBeanFactory;
        while (parent != null) {
            if (parent.containsBean(service.getEndpointName())) {
                return true;
            }
            parent = (ConfigurableListableBeanFactory)parent.getParentBeanFactory();
        }
        return false;
    }

    private static class LocalStackContainerEndpointFactoryBean implements FactoryBean<ServiceEndpoint> {

        @NotNull
        protected final LocalService localService;

        LocalStackContainerEndpointFactoryBean(LocalService localService) {
            this.localService = localService;
        }

        //TODO: Singleton?
        @Override
        public ServiceEndpoint getObject() throws Exception {
            final ServiceEndpoint endpoint = new ServiceEndpoint(
                  this.localService.getPort(),
                  this.localService.getScheme(),
                  ServiceEndpoint.DEFAULT_HOSTNAME,
                  getInternalContainerServiceConfig());
            return endpoint;
        }

        @Override
        public Class<?> getObjectType() {
            return ServiceEndpoint.class;
        }

        protected final InternalContainerServiceConfig getInternalContainerServiceConfig() {
            return Optional.ofNullable(this.localService.getInternalContainerServiceConfig())
                  .map(explicitConfig -> new InternalContainerServiceConfig(
                        explicitConfig.getImageName(),
                        explicitConfig.getName() + RandomUtils.nextInt(),
                        explicitConfig.getPort()))
                  .orElse(new InternalContainerServiceConfig("Unknown", "unknown", -1));
        }
    }
}

