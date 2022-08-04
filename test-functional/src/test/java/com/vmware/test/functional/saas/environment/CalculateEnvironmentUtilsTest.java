/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.environment;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.process.LocalTestProcessCtl;
import com.vmware.test.functional.saas.process.TestApp;
import com.vmware.test.functional.saas.process.TestCommand;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;

import static org.hamcrest.MatcherAssert.*;

/**
 * Tests to verify how {@link LocalTestProcessCtl} environment is calculated.
 */
@TestPropertySource(properties = { "prefix.app.env.var1=newValue1", "app.env.var2=newValue2" })
public class CalculateEnvironmentUtilsTest extends AbstractTestNGSpringContextTests {

    private static final String TEST_EVN_VAR_1_VALUE = "value1";
    private static final String TEST_EVN_VAR_2_VALUE = "value2";

    @Autowired
    Environment environment;

    @Test
    public void calculateEnvWithNamespaceAndOverriddenProperty() {
        final LocalTestProcessCtl testProcessWithNamespace = LocalTestProcessCtl.builder()
                .command(TestCommand::createCommand)
                .environmentSupplier(() -> Map.of(TestApp.TEST_ENV_VAR_1, TEST_EVN_VAR_1_VALUE))
                .environment(this.environment)
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("^Test App Log Line : newValue1$")
                        .build())
                .namespace("prefix")
                .build();
        testProcessWithNamespace.start();
        assertThat("Running", testProcessWithNamespace.isRunning());
        testProcessWithNamespace.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void calculateEnvWithoutNameSpaceAndOverriddenPropertyWithPrefix() {
        final LocalTestProcessCtl testProcessWithoutNamespace = LocalTestProcessCtl.builder()
                .command(TestCommand::createCommand)
                .environmentSupplier(() -> Map.of(TestApp.TEST_ENV_VAR_1, TEST_EVN_VAR_1_VALUE))
                .environment(this.environment)
                .startupTimeout(Duration.ofSeconds(3))
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("^Test App Log Line : newValue1$")
                        .build())
                .build();
        testProcessWithoutNamespace.start();
        assertThat("Not Running", !testProcessWithoutNamespace.isRunning());
    }

    @Test
    public void calculateEnvWithoutNamespaceAndOverriddenProperty() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(TestCommand::createCommand)
                .environmentSupplier(() -> Map.of(TestApp.TEST_ENV_VAR_2, TEST_EVN_VAR_2_VALUE))
                .environment(this.environment)
                .waitingFor(new WaitStrategyBuilder()
                        .forLogMessagePattern("^Test App Log Line : newValue2$")
                        .build())
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test
    public void calculateEnvWithoutEnvSupplier() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(TestCommand::createCommand)
                .environment(this.environment)
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("^Test App Log Line : 1$").build())
                .build();
        testProcessCtl.start();
        assertThat("Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void calculateEnvWithoutEnvironmentPassed() {
        final LocalTestProcessCtl testProcessCtl = LocalTestProcessCtl.builder()
                .command(TestCommand::createCommand)
                .environmentSupplier(() -> Map.of(TestApp.TEST_ENV_VAR_2, TEST_EVN_VAR_2_VALUE))
                .startupTimeout(Duration.ofSeconds(5))
                .waitingFor(new WaitStrategyBuilder().forLogMessagePattern("^Test App Log Line : newValue2$").build())
                .build();
        testProcessCtl.start();
        assertThat("Not Running", testProcessCtl.isRunning());
        testProcessCtl.stop();
    }
}
