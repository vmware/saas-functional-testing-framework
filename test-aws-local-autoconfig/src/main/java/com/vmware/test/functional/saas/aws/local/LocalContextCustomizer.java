/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.local;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

import com.vmware.test.functional.saas.aws.local.service.LocalstackServiceRegistrar;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Customizes the {@link ConfigurableApplicationContext application contexts}
 * by adding a {@link PropertySources property source} used to resolve the names and settings
 * for the different local resources inuse by the functional tests.
 * Such resources are kinesis streams, s3 buckets, SQS queues/subscriptions etc.
 *
 * <p>
 * Everywhere in the test code (test classes or java configs) the resources are
 * referenced using the expression-driven dependency injection:
 * </p>
 *
 * <pre class="code">
 * &#064;Value("${AWS_KINESIS_STREAM_NAME_AIRWATCH_OUTPUT}")
 * private String streamName;
 * </pre>
 *
 * <p>This customizer register a classpath resource property source which tries to load classpath entry named "local-test.properties".</p>
 *
 * <p>Installing an instance of the LocalstackServiceRegistrar is another responsibility of this customizer. Since the registrar is an implementation of
 * {@code BeanDefinitionRegistryPostProcessor} it is not appropriate to just list it as normal java bean because the latter will trigger early initialization of
 * the entire config - which may not be obvious - and may result in unpredictable behavior.</p>
 */
@AllArgsConstructor
@EqualsAndHashCode
@Slf4j
public class LocalContextCustomizer implements ContextCustomizer {

    private static final String PROPERTY_SOURCE_NAME = "Local Test Resources Names and Options";
    private static final ClassPathResource CLASS_PATH_RESOURCE = new ClassPathResource("local-test.properties");
    private final String contextKey;

    /**
     * Factory for creating {@link LocalContextCustomizer} instance.
     */
    public static class Factory implements ContextCustomizerFactory {
        @Override
        public ContextCustomizer createContextCustomizer(final Class<?> testClass, final List<ContextConfigurationAttributes> configAttributes) {
            return new LocalContextCustomizer(LocalContextCustomizer.class.getName());
        }
    }

    @Override
    public void customizeContext(final ConfigurableApplicationContext context, final MergedContextConfiguration mergedConfig) {
        final MutablePropertySources sources = context.getEnvironment().getPropertySources();
        try {
            sources.addFirst(new ResourcePropertySource(PROPERTY_SOURCE_NAME, CLASS_PATH_RESOURCE));
        } catch (final IOException e) {
            log.info("[local-test.properties] classpath resource not found");
        }
        final BeanDefinitionRegistry registry = (BeanDefinitionRegistry)context;
        final AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);
        reader.registerBean(LocalstackServiceRegistrar.class, LocalstackServiceRegistrar.class.getName());
    }
}
