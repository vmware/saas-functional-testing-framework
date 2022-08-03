/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.trino;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.common.AbstractResourceAwaitingInitializer;

import lombok.extern.slf4j.Slf4j;

import static org.awaitility.Awaitility.await;

/**
 * Lifecycle control that verifies trino catalogs, provided by {@link TrinoCatalogSpecs},
 * exist when started.
 */
@Slf4j
public class TrinoCatalogAwaitingInitializer extends AbstractResourceAwaitingInitializer {

    private final JdbcTemplate jdbcTemplate;

    public TrinoCatalogAwaitingInitializer(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final JdbcTemplate jdbcTemplate) {
        super(functionalTestExecutionSettings);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void doStart() {
        final List<TrinoCatalogSpecs> trinoCatalogSpecs = new ArrayList<>(getContext().getBeansOfType(TrinoCatalogSpecs.class).values());
        if (!trinoCatalogSpecs.isEmpty()) {
            log.debug("Verifying Trino catalogs exist from {}", trinoCatalogSpecs);
            trinoCatalogSpecs.stream()
                    .map(TrinoCatalogSpecs::getCatalogs)
                    .flatMap(Collection::stream)
                    .distinct()
                    .forEach(this::verifyCatalog);
        }
    }

    private void verifyCatalog(final TrinoCatalogSettings trinoCatalogSettings) {
        await().until(() -> checkCatalogExists(trinoCatalogSettings));
        log.info("Verified Trino Catalog [{}] exists", trinoCatalogSettings.getName());
    }

    private boolean checkCatalogExists(final TrinoCatalogSettings trinoCatalogSettings) {
        final String showSchemasStmt = "show catalogs";

        final List<String> databases = this.jdbcTemplate.query(showSchemasStmt, resultSetObj -> {
            final List<String> databasesList = new ArrayList<>();
            while (resultSetObj.next()) {
                if (resultSetObj.getString(1).equals(trinoCatalogSettings.getName())) {
                    databasesList.add(resultSetObj.getString(1));
                }
            }
            return databasesList;
        });
        return !CollectionUtils.isEmpty(databases);
    }
}
