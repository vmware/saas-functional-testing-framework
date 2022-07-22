/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.es;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.local.aws.AbstractFullContextTest;
import com.vmware.test.functional.saas.es.ElasticsearchHealthHelper;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

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
    private JestClient jestClient;

    @Test
    public void localElasticsearchAutoconfiguration() throws IOException {
        assertThat("ElasticSearchClient cannot be null", this.jestClient, notNullValue());
        // verify index was created by ElasticsearchInitializer
        assertThat(ElasticsearchHealthHelper
                .checkHealth(this.jestClient, this.elasticsearchIndex), is(true));

        final String testEsField = "test_org_id";
        final String testData = UUID.randomUUID().toString();
        // Put data in ES index
        ElasticsearchTestUtils.processBulkIndexRequest(
                this.jestClient,
                this.elasticsearchIndexAlias,
                List.of(Map.of(testEsField, testData))
        );

        // Verify data exists in ES
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(testEsField, testData)));

        final SearchResult searchResultSource = this.jestClient
                .execute(new Search.Builder(searchSourceBuilder.toString()).addIndex(this.elasticsearchIndex).build());
        assertThat("Retrieved record from ES total count is 1.", searchResultSource.getTotal(), is(1L));
        assertThat("Retrieved record from ES is matching the test data.", searchResultSource.getSourceAsString().contains(testData), is(true));
    }
}
