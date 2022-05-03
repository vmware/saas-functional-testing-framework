/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process;

import java.time.Duration;

import org.apache.commons.exec.CommandLine;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.process.wait.strategy.HttpWaitStrategy;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategyBuilder;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.*;

/**
 * Tests for {@link HttpWaitStrategy}.
 */
@Test
@Slf4j
public class HttpWaitStrategyTest {

    private static final String HOST = "localhost";

    private WireMockServer wireMockServer;
    private String mockedBaseUrl;
    private LocalTestProcessCtl testProcessCtl;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        this.testProcessCtl = LocalTestProcessCtl.builder()
                .command(() -> new CommandLine("java"))
                // set to 5 seconds so that test does not wait the default 60 seconds timeout
                .startupTimeout(Duration.ofSeconds(5))
                .build();

        this.wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(0));
        this.wireMockServer.start();

        final int wireMockServerPort = this.wireMockServer.port();
        this.mockedBaseUrl = String.format("http://%s:%d", HOST, wireMockServerPort);
        log.info("Starting server on {}:{}", HOST, wireMockServerPort);
    }

    @Test
    public void urlHealthCheckOk() {
        this.wireMockServer.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse().withStatus(200)
                        .withBody(String.valueOf(HttpStatus.SC_OK))));

        assertThat("Url Health Check Ok", new WaitStrategyBuilder()
                .forHttp(this.mockedBaseUrl + "/health").build()
                .hasCompleted(this.testProcessCtl.getLocalTestProcessContext()));
    }

    @Test
    public void urlHealthCheckNotOk() {
        this.wireMockServer.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse().withStatus(400)
                        .withBody(String.valueOf(HttpStatus.SC_NOT_FOUND))));
        assertThat("Url Health Check Not OK", !new WaitStrategyBuilder()
                .forHttp(this.mockedBaseUrl + "/health").build()
                .hasCompleted(this.testProcessCtl.getLocalTestProcessContext()));

    }

    @Test
    public void cannotOpenConnection() {
        assertThat("Url Health Check Not OK", !new WaitStrategyBuilder()
                .forHttp(this.mockedBaseUrl + "/health").build()
                .hasCompleted(this.testProcessCtl.getLocalTestProcessContext()));
    }

    @Test
    public void malformedUrlConnection() {
        assertThat("Url Health Check Not OK", !new WaitStrategyBuilder()
                .forHttp(this.mockedBaseUrl + "/health").build()
                .hasCompleted(this.testProcessCtl.getLocalTestProcessContext()));
    }

    @AfterMethod(alwaysRun = true)
    public void resetStubs() {
        this.wireMockServer.resetAll();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        this.wireMockServer.stop();
    }
}
