/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.scope;

import com.vmware.test.functional.saas.environment.operations.Disposable;

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

