/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.Environment;

/**
 * Utility class to retrieve AWS services, requested through {@link ServiceDependencies}.
 * Retrieve AWS services requested to be provisioned by LocalStack.
 */
final class ServiceConditionUtil {

    private static final String SERVICES_PROVIDED_BY_LOCALSTACK = "services.provided.by.localstack";

    private ServiceConditionUtil() {

    }

    /**
     * Utility method to retrieve required service dependencies from the context.
     *
     * @param context {@link ConditionContext}
     * @return Set of required {@link Service}
     */
    static Set<Service> getRequiredServiceDependencies(final ConditionContext context) {
        return getRequiredServiceDependencies(context, false);
    }

    static Set<Service> getRequiredServiceDependencies(final ConditionContext context, final boolean searchAll) {
        final ConfigurableListableBeanFactory listableBeanFactory = context.getBeanFactory();
        if (listableBeanFactory == null) {
            return Collections.emptySet();
        }
        final Set<Service> requestedServices = new HashSet<>();
        collectRequiredServiceDependencies(listableBeanFactory, searchAll, requestedServices);
        return requestedServices;
    }

    /**
     * Utility method to retrieve required service dependencies from the context.
     *
     * @param listableBeanFactory {@link ConfigurableListableBeanFactory}
     * @return Set of required {@link Service}
     */
    static Set<Service> getRequiredServiceDependencies(final ConfigurableListableBeanFactory listableBeanFactory) {
        final Set<Service> requestedServices = new HashSet<>();
        collectRequiredServiceDependencies(listableBeanFactory, false, requestedServices);
        return requestedServices;
    }

    /**
     * Utility method to retrieve required Localstack services from the context.
     *
     * @param context {@link ConditionContext}
     * @return List of services that should be started in localstack.
     */
    static List<String> getLocalstackServices(@NotNull final ConditionContext context) {
        final Environment environment = context.getEnvironment();
        return getLocalstackServices(environment);
    }

    /**
     * Utility method to retrieve required Localstack services from the context.
     *
     * @param environment {@link Environment}
     * @return List of services that should be started in localstack.
     */
    static List<String> getLocalstackServices(final Environment environment) {
        List<String> localstackServices = new ArrayList<>();
        final String localstackRequestedServices = environment.getProperty(SERVICES_PROVIDED_BY_LOCALSTACK);
        if (StringUtils.isNotBlank(localstackRequestedServices)) {
            localstackServices = Arrays.asList(localstackRequestedServices.split(","));
        }
        return localstackServices;
    }

    private static void collectRequiredServiceDependencies(final ConfigurableListableBeanFactory listableBeanFactory, final boolean searchAll, final Set<Service> requestedServices) {
        final String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(ServiceDependencies.class);
        for (String beanName : beanNames) {
            final BeanDefinition beanDefinition = listableBeanFactory.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                final MergedAnnotations mergedAnnotations = ((AnnotatedBeanDefinition)beanDefinition).getMetadata().getAnnotations();
                final Service[] services = mergedAnnotations.get(ServiceDependencies.class).synthesize().services();
                requestedServices.addAll(Arrays.asList(services));
            }
        }
        final BeanFactory parentBeanFactory = listableBeanFactory.getParentBeanFactory();
        if (searchAll && parentBeanFactory instanceof ConfigurableListableBeanFactory) {
            collectRequiredServiceDependencies((ConfigurableListableBeanFactory)parentBeanFactory, true, requestedServices);
        }
    }
}
