/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.MergedAnnotations;

import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;

/**
 * Utility class to retrieve services, requested through {@link ServiceDependencies}.
 */
public final class ServiceConditionUtil {

    private ServiceConditionUtil() {

    }

    /**
     * Utility method to retrieve required service dependencies from the context.
     *
     * @param listableBeanFactory {@link ConfigurableListableBeanFactory}
     * @return Set of required {@link Service}
     */
    public static Set<Service> getRequiredServiceDependencies(final ConfigurableListableBeanFactory listableBeanFactory) {
        final Set<Service> requestedServices = new HashSet<>();
        collectRequiredServiceDependencies(listableBeanFactory, false, requestedServices);
        return requestedServices;
    }

    /**
     * Utility method to retrieve required service dependencies from the context.
     *
     * @param context {@link ConditionContext}
     * @return Set of required {@link Service}
     */
    public static Set<Service> getRequiredServiceDependencies(final ConditionContext context) {
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

    private static String getEndpointName(TypedStringValue endpointNameArgumentValue) {
        return endpointNameArgumentValue.getValue();
    }

}
