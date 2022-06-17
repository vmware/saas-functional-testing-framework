/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.dbms;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Jdbc Template Factory used for making operation on Postgres db.
 * Provides local {@link JdbcTemplate}. To be used by Functional tests.
 */
public class JdbcTemplateFactory implements FactoryBean<JdbcTemplate> {

    private final DataSource dataSource;

    public JdbcTemplateFactory(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public JdbcTemplate getObject() {
        return new JdbcTemplate(this.dataSource);
    }

    @Override
    public Class<?> getObjectType() {
        return JdbcTemplate.class;
    }
}
