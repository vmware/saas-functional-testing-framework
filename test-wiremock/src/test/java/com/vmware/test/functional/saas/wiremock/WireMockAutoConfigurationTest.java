/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.wiremock;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.ServiceEndpoint;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.*;

/**
 * Test for {@link WireMockAutoConfiguration}.
 */
@ContextConfiguration(classes = WireMockAutoConfiguration.class)
@Test
public class WireMockAutoConfigurationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    ServiceEndpoint wireMockEndpoint;

    @Test
    public void startWireMockServer() throws IOException {
        createStubs();

        final int port = this.wireMockEndpoint.getPort();
        assertThat("Wiremock is not configured", port != -1);

        final HttpGet request = new HttpGet(this.wireMockEndpoint.getEndpoint() + "/api/test");

        //Create a web request to localhost:port/api/test and expect ok with body = "tes
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(request)) {

            assertThat("Expected HTTP.OK", response.getStatusLine().getStatusCode() == HttpStatus.OK.value());

            final HttpEntity entity = response.getEntity();
            assertThat("Expected response entity", entity != null);

            // return it as a String
            final String result = EntityUtils.toString(entity);
            assertThat("Expeted response to be test", result.equals("test"));
        }
    }

    private void createStubs() {
        stubFor(get(urlEqualTo("/api/test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "text/plain")
                        .withBody("test")));
    }
}
