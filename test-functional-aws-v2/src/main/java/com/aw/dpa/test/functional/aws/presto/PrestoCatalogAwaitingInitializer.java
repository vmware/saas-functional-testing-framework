/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.presto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.functional.aws.AbstractAwsResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies presto catalogs, provided by {@link PrestoCatalogSpecs},
 * exist when started.
 */
@Slf4j
public class PrestoCatalogAwaitingInitializer extends AbstractAwsResourceAwaitingInitializer {

    private final JdbcTemplate jdbcTemplate;

    public PrestoCatalogAwaitingInitializer(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final JdbcTemplate jdbcTemplate) {
        super(functionalTestExecutionSettings);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void doStart() {
        final List<PrestoCatalogSpecs> prestoCatalogSpecs = new ArrayList<>(getContext().getBeansOfType(PrestoCatalogSpecs.class).values());
        if (!prestoCatalogSpecs.isEmpty()) {
            log.debug("Verifying Presto catalogs exist from {}", prestoCatalogSpecs);
            prestoCatalogSpecs.stream()
                    .map(PrestoCatalogSpecs::getCatalogs)
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(this::verifyCatalog);
        }
    }

    private void verifyCatalog(final PrestoCatalogSettings prestoCatalogSettings) {
        await().until(() -> checkCatalogExists(prestoCatalogSettings));
        log.info("Verified Presto Catalog [{}] exists", prestoCatalogSettings.getName());
    }

    private boolean checkCatalogExists(final PrestoCatalogSettings prestoCatalogSettings) {
        final String showSchemasStmt = "show catalogs";

        final List<String> databases = this.jdbcTemplate.query(showSchemasStmt, resultSetObj -> {
            final List<String> databasesList = new ArrayList<>();
            while (resultSetObj.next()) {
                if (resultSetObj.getString(1).equals(prestoCatalogSettings.getName())) {
                    databasesList.add(resultSetObj.getString(1));
                }
            }
            return databasesList;
        });
        return !CollectionUtils.isEmpty(databases);
    }
}
