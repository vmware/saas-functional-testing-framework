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
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.vmware.test.functional.saas.InternalContainerServiceConfig;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

import static com.vmware.test.functional.saas.local.LocalServiceConstants.LOCALSTACK_DEFAULT_SERVICE_PORT;
import static com.vmware.test.functional.saas.local.LocalServiceConstants.LOCALSTACK_IMAGE_NAME;

/**
 * Register localstack services to be provisioned by LocalStack container.
 */
// Refactor
@Slf4j
public class LocalstackServiceRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

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
        final ConfigurableListableBeanFactory configurableListableBeanFactory = (ConfigurableListableBeanFactory)registry;
        final Set<LocalService.BeanInfo> requiredServiceDependencies = ServiceConditionUtil
                .lookupRequiredServiceDependenciesInfo(configurableListableBeanFactory).stream()
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

    private boolean isRequiredLocalstackService(final LocalService.BeanInfo service) {
        final List<String> localstackServices = ServiceConditionUtil.getLocalstackServices(this.environment);
        return service.isLocalstackService() && localstackServices.contains(service.getName());
    }

    private void addLocalStackContainerServiceBeanDefinition(final LocalService.BeanInfo service,
          final BeanDefinitionRegistry registry) {
        final String beanName = service.name + BEAN_NAME_SUFFIX;
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        final RootBeanDefinition definition = new RootBeanDefinition(LocalStackContainerServiceFactoryBean.class);
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        final ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
        constructorArguments.addIndexedArgumentValue(0, service.getBeanRef());
        registry.registerBeanDefinition(beanName, definition);
    }

    private void addLocalStackEndpointBeanDef(final BeanDefinitionRegistry registry) {
        log.info("Adding local service bean definition for Localstack");
        final RootBeanDefinition definitionService = new RootBeanDefinition(LocalService.class);
        definitionService.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        final ConstructorArgumentValues serviceConstructorArguments = definitionService.getConstructorArgumentValues();
        int argIndex = 0;
        serviceConstructorArguments.addIndexedArgumentValue(argIndex++, "_LOCALSTACK");
        serviceConstructorArguments.addIndexedArgumentValue(argIndex++, LocalServiceConstants.Components.LOCALSTACK_ENDPOINT);
        serviceConstructorArguments.addIndexedArgumentValue(argIndex++,
              new InternalContainerServiceConfig(
                    LOCALSTACK_IMAGE_NAME,
                    "localstack",
                    LOCALSTACK_DEFAULT_SERVICE_PORT));
        serviceConstructorArguments.addIndexedArgumentValue(argIndex, LOCALSTACK_DEFAULT_SERVICE_PORT);

        registry.registerBeanDefinition("_LOCALSTACK", definitionService);

        log.info("Adding local endpoint bean definition for Localstack");
        final RootBeanDefinition definitionEndpoint = new RootBeanDefinition(LocalServiceEndpointFactoryBean.class);
        definitionEndpoint.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        final ConstructorArgumentValues endpointConstructorArguments = definitionEndpoint.getConstructorArgumentValues();
        argIndex = 0;
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, new RuntimeBeanReference("_LOCALSTACK"));
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, this.containerNameSuffix);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_HOSTNAME);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex, true);
        registry.registerBeanDefinition(LocalServiceConstants.Components.LOCALSTACK_ENDPOINT, definitionEndpoint);
    }

    private void addEndpointBeanDef(final LocalService.BeanInfo service,
                                    final BeanDefinitionRegistry registry) {
        log.info("Adding local endpoint bean definition for service {}", service.getName());
        final RootBeanDefinition definitionEndpoint = new RootBeanDefinition(LocalServiceEndpointFactoryBean.class);
        definitionEndpoint.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        final ConstructorArgumentValues endpointConstructorArguments = definitionEndpoint.getConstructorArgumentValues();

        final String defaultPortsEnabledPropertyName = "default.ports.enabled";
        final boolean defaultPortsEnabled = this.environment.containsProperty(defaultPortsEnabledPropertyName)
              && this.environment.getRequiredProperty(defaultPortsEnabledPropertyName, Boolean.class);

        int argIndex = 0;
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, service.getBeanRef());
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, this.containerNameSuffix);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_HOSTNAME);
        endpointConstructorArguments.addIndexedArgumentValue(argIndex, defaultPortsEnabled);

        registry.registerBeanDefinition(service.getEndpointName(), definitionEndpoint);
    }

    private boolean isEndpointDefined(final LocalService.BeanInfo service,
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
}

