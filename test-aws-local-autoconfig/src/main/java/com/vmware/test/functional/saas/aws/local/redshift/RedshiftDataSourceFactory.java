/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.redshift;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

/**
 * Redshift DataSource Factory used for creating Redshift data source.
 * Provides local {@link DataSource}. To be used by Functional tests.
 */
@RequiredArgsConstructor
public class RedshiftDataSourceFactory implements FactoryBean<DataSource> {

    private final ServiceEndpoint redshiftEndpoint;
    private final RedshiftDbSettings redshiftDbSettings;
    private final RedshiftDataSourceConfig redshiftDataSourceConfig;

    @Override
    public DataSource getObject() {
        return dataSource();
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    private DataSource dataSource() {

        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(this.redshiftDataSourceConfig.getDriverClassName());
        config.setJdbcUrl(String.format(this.redshiftDataSourceConfig.getJdbcUrlFormat(),
                this.redshiftEndpoint.getPort(), this.redshiftDbSettings.getDbName()));
        config.setPoolName(this.redshiftDbSettings.getDbName() + "-pool");
        config.setUsername(this.redshiftDbSettings.getDbName());
        config.setPassword(this.redshiftDbSettings.getDbName());
        if (this.redshiftDataSourceConfig.getAdditionalDatasourceConfig() != null) {
            this.redshiftDataSourceConfig.getAdditionalDatasourceConfig().forEach(config::addDataSourceProperty);
        }
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }
}
