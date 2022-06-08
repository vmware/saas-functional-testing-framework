/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

/**
 * Register localstack services to be provisioned by LocalStack container.
 */
// Refactor
@Slf4j
public class LocalstackServiceRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    public static final String BEAN_NAME_SUFFIX = "LocalStack";
    public static final String FACTORY_METHOD_NAME = "valueOf";
    public static final int LOCALSTACK_DEFAULT_SERVICE_PORT = 4566;

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
        final ConfigurableListableBeanFactory configurableListableBeanFactory = (ConfigurableListableBeanFactory)registry;
        final Set<LocalService> requiredServiceDependencies = ServiceConditionUtil
                .getRequiredServiceDependencies(configurableListableBeanFactory).stream()
                .filter(service -> !isEndpointDefined(service, configurableListableBeanFactory))
                .collect(Collectors.toSet());
        // Add a bean definition for localstack container itself.
        if (requiredServiceDependencies.stream().anyMatch(this::isRequiredLocalstackService)) {
            addLocalStackEndpointBeanDef(registry);
        }
        // Add bean definitions for requested services
        requiredServiceDependencies.forEach(service -> {
            if (isRequiredLocalstackService(service)) {
                addLocalStackContainerServiceBeanDefinition(service, registry);
            }
            addEndpointBeanDef(service, registry);
        });
    }

    private boolean isRequiredLocalstackService(final LocalService service) {
        final List<String> localstackServices = ServiceConditionUtil.getLocalstackServices(this.environment);
        return service.isLocalstackService() && localstackServices.contains(service.getService().getLocalStackName());
    }

    private void addLocalStackContainerServiceBeanDefinition(final LocalService service, final BeanDefinitionRegistry registry) {
        final String beanName = service.getService().getLocalStackName() + BEAN_NAME_SUFFIX;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        final RootBeanDefinition definition = new RootBeanDefinition(LocalStackContainer.Service.class);
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        definition.setFactoryMethodName(FACTORY_METHOD_NAME);
        final ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
        constructorArguments.addIndexedArgumentValue(0, service.getService().name());
        registry.registerBeanDefinition(beanName, definition);
    }

    private void addLocalStackEndpointBeanDef(final BeanDefinitionRegistry registry) {
        log.info("Adding local endpoint bean definition for Localstack");
        final RootBeanDefinition definitionEndpoint = new RootBeanDefinition(LocalServiceEndpointFactoryBean.class);
        definitionEndpoint.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        final ConstructorArgumentValues endpointConstructorArguments = definitionEndpoint.getConstructorArgumentValues();
        int argIndex = 0;
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, DockerContainerType.LOCALSTACK);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, this.containerNameSuffix);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_SCHEME);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_HOSTNAME);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex, LOCALSTACK_DEFAULT_SERVICE_PORT);

        registry.registerBeanDefinition(LocalServiceConstants.Components.LOCALSTACK_ENDPOINT, definitionEndpoint);
    }

    private void addEndpointBeanDef(final LocalService service,
                                    final BeanDefinitionRegistry registry) {
        log.info("Adding local endpoint bean definition for service {}", service.name());
        final RootBeanDefinition definitionEndpoint = new RootBeanDefinition(LocalServiceEndpointFactoryBean.class);
        definitionEndpoint.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        final ConstructorArgumentValues endpointConstructorArguments = definitionEndpoint.getConstructorArgumentValues();

        final String defaultPortsEnabledPropertyName = "default.ports.enabled";
        int argIndex = 0;
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, service.getDefaultContainerType());
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, this.containerNameSuffix);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, service.getScheme());
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_HOSTNAME);
        if (this.environment.containsProperty(defaultPortsEnabledPropertyName) && this.environment.getRequiredProperty(defaultPortsEnabledPropertyName, Boolean.class)) {
            endpointConstructorArguments.addIndexedArgumentValue(argIndex, service.getPort());
        }

        registry.registerBeanDefinition(service.getEndpoint(), definitionEndpoint);
    }

    private boolean isEndpointDefined(final LocalService service, final ConfigurableListableBeanFactory configurableListableBeanFactory) {
        ConfigurableListableBeanFactory parent = configurableListableBeanFactory;
        while (parent != null) {
            if (parent.containsBean(service.getEndpoint())) {
                return true;
            }
            parent = (ConfigurableListableBeanFactory)parent.getParentBeanFactory();
        }
        return false;
    }
}

