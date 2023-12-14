/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.es;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.es.ElasticsearchHealthHelper;
import com.vmware.test.functional.saas.local.aws.config.DockerContainersConfiguration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.google.common.base.Preconditions;

import io.trino.jdbc.TrinoDriver;

import lombok.SneakyThrows;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for {@link DockerContainersConfiguration}.
 * Test verifies local trino is configured to work with local ES.
 */
@ContextHierarchy(@ContextConfiguration(classes = LocalTrinoWithLocalESConfigTest.TestContext.class))
@FunctionalTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LocalTrinoWithLocalESConfigTest extends AbstractTestNGSpringContextTests {

    private static final String ES_TEST_DATA_FIELD = "data_field";
    private static final String ES_CATALOG = "elasticsearch";
    private static final String ES_SCHEMA = "es";
    private static final String SELECT_FROM_TABLE_CMD = "select %s from %s";
    private static final String SHOW_TABLES_CMD = "show tables";

    private String testData;
    private String testEsIndex;

    @ServiceDependencies({ Service.TRINO, Service.ELASTICSEARCH })
    public static class TestContext {

    }

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private ServiceEndpoint trinoEndpoint;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        // create ES index
        assertThat("ElasticSearchClient cannot be null", this.esClient, notNullValue());
        this.testEsIndex = "test_" + StringUtils.replace(UUID.randomUUID().toString(), "-", "");

        ElasticsearchUtils.createIndex(this.esClient, this.testEsIndex,
                null,
                null,
                "");
        // Verify index was created
        await("Await ES index creation.").until(() -> ElasticsearchHealthHelper.checkHealth(this.esClient, this.testEsIndex)
              , is(true));

        // Create record
        this.testData = UUID.randomUUID().toString();
        final List<Map<String, Object>> esTestDataToSeed = List.of(
                Map.of(ES_TEST_DATA_FIELD, this.testData)
        );

        // Put record in ES
        ElasticsearchTestUtils.processBulkIndexRequest(
                this.esClient, this.testEsIndex,
                esTestDataToSeed);

        // Verify record is included in the index
        final SearchResponse<Object> searchResult = this.esClient.search(s -> s
                    .index(this.testEsIndex)
                    .query(q -> q
                          .bool(b -> b
                                .must(MatchQuery.of(m -> m
                                      .field(ES_TEST_DATA_FIELD)
                                      .query(this.testData)
                                )._toQuery())
                          )
                    ),
              Object.class);
        assertThat("Retrieved record from ES is not null or empty.", searchResult.toString(), not(emptyOrNullString()));
        assertThat("Retrieved record from ES is matching the test data.", searchResult.toString().contains(this.testData), is(true));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        // Dropping ES index
        assertThat(dropIndex(this.testEsIndex), is(true));
    }

    @Test
    public void localTrinoConfigWithLocalES() {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(TrinoDriver.class);
        dataSource.setUrl("jdbc:trino://localhost:" + this.trinoEndpoint.getPort());
        dataSource.setCatalog(ES_CATALOG);
        dataSource.setSchema(ES_SCHEMA);
        dataSource.setUsername("test");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // List tables in ES from trino
        final List<String> tables = jdbcTemplate.query(SHOW_TABLES_CMD, resultSetObj -> {
            final List<String> databasesList = new ArrayList<>();
            while (resultSetObj.next()) {
                if (resultSetObj.getString(1).equals(this.testEsIndex)) {
                    databasesList.add(resultSetObj.getString(1));
                }
            }
            return databasesList;
        });

        assertThat(tables, is(not(nullValue())));
        assertThat(tables.size(), is(1));

        // Get record in ES from trino
        final String getRecordsCommand = String.format(SELECT_FROM_TABLE_CMD, ES_TEST_DATA_FIELD, this.testEsIndex);
        final List<ResultSet> records = jdbcTemplate.query(getRecordsCommand, resultSetObj -> {
            final List<ResultSet> recordsList = new ArrayList<>();
            while (resultSetObj.next()) {
                if (resultSetObj.getString(ES_TEST_DATA_FIELD).equals(this.testData)) {
                    recordsList.add(resultSetObj);
                }
            }
            return recordsList;
        });

        assertThat(records, is(not(nullValue())));
        assertThat(records.size(), is(1));
    }

    @SneakyThrows
    public boolean dropIndex(final String index) {
        Preconditions.checkArgument(index != null && !index.isBlank(), "index name is required");
        return this.esClient.indices().delete( idx -> idx.index(index)).acknowledged();
    }
}
