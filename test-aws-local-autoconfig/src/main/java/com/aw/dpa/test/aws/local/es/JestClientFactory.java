/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.es;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

import io.searchbox.client.JestClient;
import io.searchbox.client.config.HttpClientConfig;

/**
 * JestClient Factory.
 * Provides local {@link JestClient}. To be used by Functional tests.
 */
public class JestClientFactory implements FactoryBean<JestClient> {

    private final ConfigurableEnvironment env;
    private final LocalServiceEndpoint elasticsearchEndpoint;

    public JestClientFactory(final LocalServiceEndpoint elasticsearchEndpoint, final ConfigurableEnvironment env) {
        this.elasticsearchEndpoint = elasticsearchEndpoint;
        this.env = env;
    }

    @Override
    public JestClient getObject() {
        final int port = this.elasticsearchEndpoint.getPort();
        final String scheme = this.env.getRequiredProperty("ELASTICSEARCH_REST_HTTP_SCHEME") + "://";
        final List<String> serverUris = Arrays.stream(this.elasticsearchEndpoint.getHostName().split(","))
                .map((host) -> {
                    final String hostTrimmed = host.trim();
                    return port != ElasticsearchConstants.HTTP_PORT && port != ElasticsearchConstants.HTTPS_PORT && !hostTrimmed.contains(":")
                            ? String.format("%s%s:%s", scheme, hostTrimmed, port) : scheme + hostTrimmed;
                }).collect(Collectors.toUnmodifiableList());

        final io.searchbox.client.JestClientFactory factory = new io.searchbox.client.JestClientFactory();

        final Iterator<String> serverUrisIter = serverUris.iterator();
        final HttpClientConfig.Builder builder = ((((new HttpClientConfig.Builder(serverUrisIter.next()))
                .multiThreaded((true)))
                .connTimeout((ElasticsearchConstants.DEFAULT_TIMEOUT)))
                .readTimeout((ElasticsearchConstants.DEFAULT_READ_TIMEOUT)))
                .maxTotalConnection((ElasticsearchConstants.DEFAULT_MAX_CONS))
                .defaultMaxTotalConnectionPerRoute((ElasticsearchConstants.DEFAULT_MAX_CONS))
                .discoveryEnabled(false);

        while (serverUrisIter.hasNext()) {
            builder.addServer(serverUrisIter.next());
        }

        builder.gson((new GsonBuilder())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create());
        factory.setHttpClientConfig(builder.build());
        return factory.getObject();
    }

    @Override
    public Class<?> getObjectType() {
        return JestClient.class;
    }

}
