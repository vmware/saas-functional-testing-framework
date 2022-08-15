/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategy;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@FunctionalTest
@ContextConfiguration(classes = LocalAppProcessConfigTemplateTest.TestConfig.class)
@TestPropertySource(properties = {
        "pizza.executableJar=my-pizza.jar",
        "pizza.apphome=/spaghetti",
        "pizza.debug.debugModeEnable=true"})
public class LocalAppProcessConfigTemplateTest extends AbstractTestNGSpringContextTests {

    @Configuration
    @EnableConfigurationProperties
    public static class TestConfig {

        @Bean
        @ConfigurationProperties(prefix = "pizza")
        TestAppSettings testPizzaAppSettings() {
            return new TestAppSettings();
        }

        @Bean
        @ConfigurationProperties(prefix = "pizza.debug")
        TestAppDebugSettings testPizzaAppDebugSettings() {
            return new TestAppDebugSettings();
        }

        @Bean
        @Lazy
        ServiceEndpoint pizzaAppEndpoint() {
            return new ServiceEndpoint(ServiceEndpoint.DEFAULT_SCHEME);
        }

        @Bean
        public LocalTestProcessCtl pizzaAppProcess() {
            final LocalAppProcessConfigTemplate template = LocalAppProcessConfigTemplate
                    .builder().appEndpoint(pizzaAppEndpoint())
                    .testAppSettings(testPizzaAppSettings())
                    .testAppDebugSettings(testPizzaAppDebugSettings())
                    .build();
            return template.defaultTestProcessBuilder()
                    .environmentSupplier(() -> Map.of("APP_HOME", testPizzaAppSettings().getApphome()))
                    .build();
        }
    }

    @Autowired
    private LocalTestProcessCtl pizzaAppProcess;

    @Test
    public void ensureDebugEnabledIsCorrect() {
        final boolean debugEnabled = this.pizzaAppProcess.isDebugModeEnable();
        assertThat("debug enabled", debugEnabled, is(true));
    }

    @Test
    public void ensureCommandIsCorrect() {
        final String[] arguments = this.pizzaAppProcess.getCommand().get().getArguments();
        assertThat("app jar", arguments[arguments.length - 1], is("my-pizza.jar"));
    }

    @Test
    public void ensureWaitIsCorrect() {
        final WaitStrategy waitStrategy = this.pizzaAppProcess.getWaitingFor();
        assertThat("app wait strategy", waitStrategy, notNullValue());
    }

    @Test
    public void ensureEnvCorrect() {
        final Map<String, String> env = this.pizzaAppProcess.getEnvironmentSupplier().get();
        assertThat("app environment", env.get("APP_HOME"), is("/spaghetti"));
    }
}
