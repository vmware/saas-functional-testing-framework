/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.event.ApplicationEventsTestExecutionListener;
import org.springframework.test.context.event.EventPublishingTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.ServletTestExecutionListener;

/**
 * Abstract Functional Tests class used to bootstrap and load parent
 * context used in functional tests.
 * Each test extending AbstractFunctionalTests will have parent context loaded which is configured
 * and customized by adding entries to /META-INF/spring.factories
 * under the key com.vmware.test.functional.saas.FunctionalTest.
 */
@ContextHierarchy(@ContextConfiguration(classes = SharedConfig.class))
@TestExecutionListeners(value = { ServletTestExecutionListener.class, DirtiesContextBeforeModesTestExecutionListener.class,
        ApplicationEventsTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, EventPublishingTestExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
        inheritListeners = false)
@FunctionalTest
public class AbstractFunctionalTests extends AbstractTestNGSpringContextTests {

}
