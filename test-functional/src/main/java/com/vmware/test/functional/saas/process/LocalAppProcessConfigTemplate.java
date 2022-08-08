/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import java.util.List;
import java.util.Objects;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategy;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Template for building app java configs.
 */
@Data
@Builder
@Slf4j
public final class LocalAppProcessConfigTemplate {

    private static final String AWS_CBOR_DISABLE_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableCbor";
    private static final String DISABLE_CERT_CHECKING_SYSTEM_PROPERTY = "com.amazonaws.sdk.disableCertChecking";
    private final TestAppSettings testAppSettings;
    private final TestAppDebugSettings testAppDebugSettings;
    private final ServiceEndpoint appEndpoint;
    @Builder.Default
    private final List<String> additionalAppCommandLineArgs = List.of();

    /**
     * The default builder for LocalTestProcessCtl.
     *
     * @return the builder instance
     */
    public LocalTestProcessCtl.LocalTestProcessCtlBuilder defaultTestProcessBuilder() {
        return LocalTestProcessCtl.builder()
                .command(() -> this.createAppCommand(this.additionalAppCommandLineArgs))
                .debugModeEnable(this.testAppDebugSettings.isDebugModeEnable())
                .debugPort(this.testAppDebugSettings.getDebugPort())
                .debugSuspendMode(this.testAppDebugSettings.getDebugSuspend())
                .waitingFor(this.createDefaultAppHealthEndpointWaitStrategy(this.appEndpoint));
    }

    CommandLine createAppCommand(final List<String> commandLineArgs) {
        if (StringUtils.isBlank(this.testAppSettings.getExecutableJar())) {
            throw new IllegalArgumentException("Executable JAR not configured - cannot be null/empty");
        }

        // Add the java runtime plus some common arguments used in our apps/services
        final CommandLine commandLine = new CommandLine("java")
                .addArgument("-Duser.language=en")
                .addArgument("-Duser.country=US")
                // kinesalite does not support CBOR: https://github.com/localstack/localstack/issues/592
                .addArgument("-D" + AWS_CBOR_DISABLE_SYSTEM_PROPERTY + "=1")
                // KPL requires SSL, so we have to start all of the other services with SSL.
                .addArgument("-D" + DISABLE_CERT_CHECKING_SYSTEM_PROPERTY + "=" + Boolean.TRUE);

        // Add any additional command line arguments if provided via the additionalAppCommandLineArgs method
        if (Objects.nonNull(commandLineArgs) && !commandLineArgs.isEmpty()) {
            for (final String commandLineArg : commandLineArgs) {
                if (StringUtils.isNotBlank(commandLineArg)) {
                    commandLine.addArgument(commandLineArg);
                }
            }
        }

        // Lastly, add the required executable jar reference
        commandLine.addArgument("-jar").addArgument(this.testAppSettings.getExecutableJar());
        log.debug("Using CommandLine: [{}]", commandLine);

        return commandLine;
    }

    WaitStrategy createDefaultAppHealthEndpointWaitStrategy(final ServiceEndpoint endpoint) {
        if (Objects.isNull(endpoint)) {
            throw new IllegalArgumentException("App/service endpoint cannot be null");
        }

        return new WaitStrategyBuilder()
                .forHttp(() -> endpoint.getEndpoint() + ServiceEndpoint.DEFAULT_HEALTH_ENDPOINT_PATH)
                .build();
    }
}
