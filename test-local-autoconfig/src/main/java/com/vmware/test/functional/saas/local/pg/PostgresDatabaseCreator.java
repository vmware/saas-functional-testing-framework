/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.pg;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.AbstractDatabaseCreator;
import com.vmware.test.functional.saas.local.GenericDbmsSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that initializes Postgres databases.
 */
@Slf4j
public class PostgresDatabaseCreator<S extends GenericDbmsSettings> extends AbstractDatabaseCreator<S> {

    private static final String POSTGRES_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String POSTGRES_DEFAULT_DATABASE = "postgres";

    private final ServiceEndpoint postgresEndpoint;
    private final JdbcTemplate defaultPostgresJdbcTemplate;

    public PostgresDatabaseCreator(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final ServiceEndpoint postgresEndpoint,
            final Class<S> dbmsSettingsClass) {
        super(functionalTestExecutionSettings, dbmsSettingsClass);
        this.postgresEndpoint = postgresEndpoint;
        this.defaultPostgresJdbcTemplate = getJdbcTemplate(POSTGRES_DEFAULT_DATABASE);
    }

    @Override
    protected void createDatabase(final S dbmsSettings) {
        doCreateDatabase(dbmsSettings);
        customizeDatabase(dbmsSettings);
    }

    private void doCreateDatabase(final S dbmsSettings) {
        log.info("Creating role and database in cluster at [{}:{}]", this.postgresEndpoint.getHostName(), this.postgresEndpoint.getPort());
        this.defaultPostgresJdbcTemplate.execute(String.format("DROP ROLE IF EXISTS %1$s", dbmsSettings.getDbName()));
        this.defaultPostgresJdbcTemplate.execute(String.format("DROP DATABASE IF EXISTS %1$s", dbmsSettings.getDbName()));
        this.defaultPostgresJdbcTemplate.execute(String.format("CREATE ROLE %1$s LOGIN PASSWORD '%1$s' SUPERUSER;", dbmsSettings.getDbName()));
        this.defaultPostgresJdbcTemplate.execute(String.format("CREATE DATABASE %1$s owner %1$s ENCODING 'UTF8';", dbmsSettings.getDbName()));
    }

    private void customizeDatabase(final S dbmsSettings) {
        final JdbcTemplate jdbcTemplateForNewDatabase = getJdbcTemplate(dbmsSettings.getDbName());
        jdbcTemplateForNewDatabase.execute("CREATE EXTENSION IF NOT EXISTS \"pgcrypto\";");
        jdbcTemplateForNewDatabase.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
        jdbcTemplateForNewDatabase.execute("CREATE EXTENSION IF NOT EXISTS \"pg_trgm\";");
    }

    private JdbcTemplate getJdbcTemplate(final String databaseName) {
        return new JdbcTemplate(getDataSource(databaseName));
    }

    private DataSource getDataSource(final String databaseName) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUsername(databaseName);
        dataSource.setPassword(databaseName);
        dataSource.setDriverClassName(POSTGRES_DRIVER_CLASS_NAME);
        dataSource.setUrl(getJdbcUrl(databaseName));
        return dataSource;
    }

    private String getJdbcUrl(final String databaseName) {
        return String.format("jdbc:postgresql://%s:%s/%s", this.postgresEndpoint.getHostName(), this.postgresEndpoint.getPort(), databaseName);
    }
}
