/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.local.aws.lambda.sam.process.SamProcessControl;
import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;
import com.vmware.test.functional.saas.ConditionalOnService;
import com.vmware.test.functional.saas.process.DpaTestAppDebug;

/**
 * Sam Process Autoconfiguration.
 */
@Configuration
@AutoConfigureOrder(Integer.MAX_VALUE)
public class SamAutoConfiguration {

    @Value("${test.lambda.additionalCommandLineArgs:#{null}}")
    private String[] additionalCommandLineArgs;

    /**
     * All lambda functions defined as LambdaFunctionSpecs beans are exposed for debugging on a single port.
     * Due to SAM limitations, if we need to debug more than one lambda at a time, we need to create
     * separate SamProcessControl instances for those with the corresponding LambdaFunctionSpecs instances.
     * <p>
     * For those we need to create also LambdaClient, LocalServiceEndpoint, SamLambdaLogExtractor.
     * <p>
     * Note: In the above case, we should also ensure that all autowire points in the app are qualified
     * (e.g. @Qualifier or by parameter name) so that no DuplicateBeanDefinitionException for these instances.
     *
     * @return {@code DpaTestAppDebug} bean.
     */
    @Bean
    @ConfigurationProperties(prefix = "default.test.lambda")
    @ConditionalOnService(value = Service.LAMBDA)
    DpaTestAppDebug dpaTestAppDebug() {
        return new DpaTestAppDebug();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnService(value = Service.LAMBDA)
    SamProcessControl samProcessControl(final List<LambdaFunctionSpecs> lambdaFunctionSpecs,
            final ServiceEndpoint lambdaEndpoint,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        return SamProcessControl.builder()
                .lambdaEndpoint(lambdaEndpoint)
                .lambdaFunctionSpecs(lambdaFunctionSpecs)
                .debugPort(dpaTestAppDebug().getDebugPort())
                .debugModeEnabled(Boolean.parseBoolean(dpaTestAppDebug().getDebugModeEnable()))
                .functionalTestExecutionSettings(functionalTestExecutionSettings)
                .additionalCommandLineArgs(this.additionalCommandLineArgs)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnService(value = Service.LAMBDA)
    SamLambdaLogExtractor samLambdaLogExtractor(final SamProcessControl samProcessControl) {
        return new SamLambdaLogExtractor(samProcessControl.getLambdaLogFile());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnService(value = Service.LAMBDA)
    SamLambdaRequestContextFactory samLambdaRequestContextFactory(final List<LambdaFunctionSpecs> lambdaFunctionSpecs, final SamProcessControl samProcessControl) {
        return new SamLambdaRequestContextFactory(lambdaFunctionSpecs, samProcessControl.getLambdaLogFile());
    }
}
