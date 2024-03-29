/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import java.util.ArrayList;
import java.util.List;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.AbstractResourceCreator;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract lifecycle control that provision a database management system with the provided {@link GenericDbmsSettings settings}
 * when started.
 * @param <S> Type of Dbms Settings to search for in the context.
 */
@Slf4j
public abstract class AbstractDatabaseCreator<S extends GenericDbmsSettings> extends AbstractResourceCreator {

    private final Class<S> dbmsSettingsClass;

    public AbstractDatabaseCreator(final FunctionalTestExecutionSettings functionalTestExecutionSettings,
            final Class<S> dbmsSettingsClass) {
        super(functionalTestExecutionSettings);
        this.dbmsSettingsClass = dbmsSettingsClass;
    }

    @Override
    protected void doStart() {
        final List<S> dbmsSettingsList = new ArrayList<>(getContext().getBeansOfType(this.dbmsSettingsClass).values());
        if (!dbmsSettingsList.isEmpty()) {
            dbmsSettingsList.forEach(dbmsSettings -> {
                log.info("Creating database [{}] from dbms settings class [{}]", dbmsSettings.getDbName(), this.dbmsSettingsClass);
                this.createDatabase(dbmsSettings);
            });
        }
    }

    protected abstract void createDatabase(S dbmsSettings);
}
