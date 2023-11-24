/*
 * Copyright 2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

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

import lombok.extern.slf4j.Slf4j;

import com.vmware.test.functional.saas.ServiceEndpoint;

@Slf4j
public class LocalServiceEndpointRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

   private final int containerNameSuffix = RandomUtils.nextInt();

   private Environment environment;

   @Override
   public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
      this.addContainerNetworkBeanDefinition(registry);
   }

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
      // Empty
   }

   @Override
   public void setEnvironment(Environment environment) {
      this.environment = environment;
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
      endpointConstructorArguments.addIndexedArgumentValue(argIndex++, new RuntimeBeanReference(service.getName()));
      endpointConstructorArguments.addIndexedArgumentValue(argIndex++, this.containerNameSuffix);
      endpointConstructorArguments.addIndexedArgumentValue(argIndex++, ServiceEndpoint.DEFAULT_HOSTNAME);
      endpointConstructorArguments.addIndexedArgumentValue(argIndex, defaultPortsEnabled);

      registry.registerBeanDefinition(service.getEndpointName(), definitionEndpoint);
   }

   private void addContainerNetworkBeanDefinition(
         final BeanDefinitionRegistry registry) {
      final String beanName = "containerNetworkManager";
      if (registry.containsBeanDefinition(beanName)) {
         return;
      }
      final RootBeanDefinition definition = new RootBeanDefinition(ContainerNetworkManager.class);
      definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      registry.registerBeanDefinition(beanName, definition);
   }

}
