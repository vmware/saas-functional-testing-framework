/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.scope;

import com.aw.dpa.test.environment.operations.Disposable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data class used in scope tests.
 */
@Data
@AllArgsConstructor
public class TestScopedBean implements Disposable {

    private String orgId;

    @Override
    public void teardown() {
        TestOrgState.getSingletonInstance().cleanupOrgId(this.orgId);
    }
}

