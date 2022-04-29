/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.listeners;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import com.aw.dpa.test.TestExecutionListenerMapping;

@Configuration
public class FunctionalTestListenerTestContext {

    @Bean
    @Lazy
    public TestExecutionListenerMapping testEventListener() {
        final Map<Class<?>, TestExecutionListener> processors = new HashMap<>();
        final LocalTestDataProcessor testEventProcessor = new LocalTestDataProcessor();

        //Attach the test processor to the test, to be able to validate the events.
        FunctionalTestListenerTest.testEventProcessor = testEventProcessor;

        //Attach Local execution processors to test classes.
        processors.put(FunctionalTestListenerTest.class, testEventProcessor);

        return TestExecutionListenerMapping.builder()
                .testExecutionListenerByTestClass(processors)
                .build();
    }

    public class LocalTestDataProcessor implements TestExecutionListener {

        public boolean beforeTestExecutionInvoked;
        public boolean beforeTestClassInvoked;
        public boolean beforeTestMethodInvoked;
        public boolean prepareTestInstanceInvoked;
        public boolean afterTestMethodInvoked;
        public boolean afterTestClassInvoked;
        public boolean afterTestExecutionInvoked;

        @Override
        public void beforeTestMethod(final TestContext testContext) {
            this.beforeTestMethodInvoked = true;
        }

        @Override
        public void prepareTestInstance(final TestContext testContext) {
            this.prepareTestInstanceInvoked = true;
        }

        @Override
        public void beforeTestClass(final TestContext testContext) {
            this.beforeTestClassInvoked = true;

        }

        @Override
        public void beforeTestExecution(final TestContext testContext) {
            this.beforeTestExecutionInvoked = true;
        }

        @Override
        public void afterTestMethod(final TestContext testContext) {
            this.afterTestMethodInvoked = true;
        }

        @Override
        public void afterTestExecution(final TestContext testContext) {
            this.afterTestExecutionInvoked = true;
        }

        @Override
        public void afterTestClass(final TestContext testContext) {
            this.afterTestClassInvoked = true;
        }
    }

}

