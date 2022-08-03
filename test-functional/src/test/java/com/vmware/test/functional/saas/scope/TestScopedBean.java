/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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

