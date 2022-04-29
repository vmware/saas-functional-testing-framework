/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.aws.local.redshift;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aw.dpa.test.LocalServiceEndpoint;
import com.aw.dpa.test.aws.local.AbstractFullContextTest;
import com.aw.dpa.test.aws.local.dbms.JdbcTemplateFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Test for redshift configuration.
 */
@ContextHierarchy(
        @ContextConfiguration(classes = RedshiftTest.TestContext.class))
public class RedshiftTest extends AbstractFullContextTest {

    @Configuration
    public static class TestContext {

        @Bean
        @ConfigurationProperties(prefix = "default-redshift-db-settings")
        @Lazy
        RedshiftDataSourceConfig redshiftDataSourceConfig() {
            return RedshiftDataSourceConfig.builder().build();
        }

        @Bean
        @Lazy
        RedshiftDataSourceFactory redshiftDataSourceFactory(@Lazy final LocalServiceEndpoint redshiftEndpoint,
                final RedshiftDbSettings redshiftDbSettings,
                final RedshiftDataSourceConfig redshiftDataSourceConfig) {
            return new RedshiftDataSourceFactory(redshiftEndpoint, redshiftDbSettings, redshiftDataSourceConfig);
        }

        @Bean
        @Lazy
        JdbcTemplateFactory redshiftJdbcTemplate(@Lazy final RedshiftDataSourceFactory redshiftDataSourceFactory) {
            return new JdbcTemplateFactory(redshiftDataSourceFactory.getObject());
        }
    }

    @Qualifier("redshiftJdbcTemplate")
    @Autowired
    private JdbcTemplate redshiftJdbcTemplate;

    private String testTable;

    private String testColumn;

    @BeforeClass(alwaysRun = true)
    public void createTable() {
        this.testTable = "test_dpa";
        this.testColumn = "test_id";
        final String createTableCmd = String.format("CREATE TABLE %s (%s VARCHAR(256) NOT NULL)",
                this.testTable, this.testColumn);

        // Create test table in redshift
        this.redshiftJdbcTemplate.execute(createTableCmd);
    }

    @Test
    public void localRedshiftSelect() {

        final String testData = "testData_" + UUID.randomUUID();
        final String insertTestDataCmd = String.format("INSERT INTO %s VALUES('%s')",
                this.testTable, testData);
        final String queryTestDataCmd = String.format("SELECT %1$s FROM %2$s WHERE %1$s=?",
                this.testColumn, this.testTable);

        // Insert test data
        this.redshiftJdbcTemplate.execute(insertTestDataCmd);

        final List<String> results = this.redshiftJdbcTemplate.query(queryTestDataCmd,
                ps -> ps.setString(1, testData),
                (rs, rowNum) -> rs.getString(1));
        assertThat(results, is(not(empty())));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(testData));
    }
}
