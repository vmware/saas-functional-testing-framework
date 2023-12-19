/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.es;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.AbstractFullContextTest;
import com.vmware.test.functional.saas.es.ElasticsearchHealthHelper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for Elasticsearch configuration.
 */
public class ElasticsearchTest extends AbstractFullContextTest {

    @Value("${AWS_ELASTICSEARCH_INDEX}")
    private String elasticsearchIndex;

    @Value("${AWS_ELASTICSEARCH_INDEX_ALIAS}")
    private String elasticsearchIndexAlias;

    @Autowired
    private ElasticsearchClient esClient;

    @Test
    public void localElasticsearchAutoconfiguration() throws IOException {
        assertThat("ElasticSearchClient cannot be null", this.esClient, notNullValue());
        // verify index was created by ElasticsearchInitializer
        assertThat(ElasticsearchHealthHelper
                .checkHealth(this.esClient, this.elasticsearchIndex), is(true));

        final String testEsField = "test_org_id";
        final String testData = UUID.randomUUID().toString();
        // Put data in ES index
        ElasticsearchTestUtils.processBulkIndexRequest(
                this.esClient,
                this.elasticsearchIndexAlias,
                List.of(Map.of(testEsField, testData))
        );

        // Verify data exists in ES
        final SearchResponse<Object> searchResult = this.esClient.search(s -> s
                    .index(this.elasticsearchIndex)
                    .query(q -> q
                          .bool(b -> b
                                .must(MatchQuery.of(m -> m
                                      .field(testEsField)
                                      .query(testData)
                                )._toQuery())
                          )
                    ),
              Object.class);
        assertThat("Retrieved record from ES total count is 1.", searchResult.hits().total(), notNullValue());
        assertThat("Retrieved record from ES total count is 1.", searchResult.hits().total().value(), is(1L));
        assertThat("Retrieved record from ES is matching the test data.", searchResult.toString().contains(testData),
              is(true));
    }
}
