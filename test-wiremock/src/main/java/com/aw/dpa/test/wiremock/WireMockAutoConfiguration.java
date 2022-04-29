/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.wiremock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.PortSupplier;
import com.github.tomakehurst.wiremock.client.WireMock;

import lombok.extern.slf4j.Slf4j;

import static com.aw.dpa.test.LocalServiceEndpoint.DEFAULT_SCHEME;

/**
 * Wiremock server autoconfiguration.
 */

@Configuration
@Import(WireMockConfigurationWrapper.class)
@ConditionalOnMissingBean(name = "wireMockEndpoint")
@Slf4j
public class WireMockAutoConfiguration {

    /**
     * Determine if wireMockClientConfigurer service should be started locally.
     */
    public static class WireMockClient extends WireMockConfigurationWrapper.WireMock {
        @Override
        public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
            return !super.matches(context, metadata);
        }
    }

    private static final int WIRE_MOCK_PORT = 10140;

    @Value("${default.ports.enabled:false}")
    private boolean defaultPortsEnabled;

    @Bean
    LocalServiceEndpoint wireMockEndpoint() {
        return new LocalServiceEndpoint(computeWireMockPort(), DEFAULT_SCHEME);
    }

    @Bean
    @Conditional(WireMockClient.class)
    SmartLifecycle wireMockClientConfigurer(final LocalServiceEndpoint wireMockEndpoint) {
        log.info("install wiremock client configurer");
        return new SmartLifecycle() {
            private volatile boolean running;
            @Override
            public void start() {
                log.info("configure wiremock for client");
                WireMock.configureFor(wireMockEndpoint.getPort());
                this.running = true;
            }

            @Override
            public void stop() {
                this.running = false;
            }

            @Override
            public int getPhase() {
                return 0;
            }

            @Override
            public boolean isRunning() {
                return this.running;
            }
        };
    }

    @Bean
    WireMockConfigurationCustomizer wireMockConfigurationCustomizer(final LocalServiceEndpoint wireMockEndpoint) {
        return (config ->  {
            log.info("configure wiremock for server");
            config.port(wireMockEndpoint.getPort());
        });
    }

    private int computeWireMockPort() {
        return this.defaultPortsEnabled ? WIRE_MOCK_PORT : new PortSupplier().getAsInt();
    }
}
