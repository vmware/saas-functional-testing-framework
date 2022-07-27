/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that only matches when all {@link Service Services} are specified via
 * the {@link ServiceDependencies @ServiceDependencies} on bean definitions (usually {@link org.springframework.context.annotation.Configuration} classes) that have been processed by
 * the application context so far.
 * This means you must make sure that the bean using this condition is processed after the beans that specify your
 * service dependencies.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnServicesCondition.class)
public @interface ConditionalOnService {

    /**
     * Services required for the conditional to match.
     * @return services required for the conditional to match.
     */
    Service[] value() default {};

    /**
     * Strategy to decide if the application context hierarchy (parent contexts) should be
     * considered.
     * @return the search strategy
     */
    SearchStrategy search() default SearchStrategy.CURRENT;

    boolean conditionalOnMissingBean() default false;
}
