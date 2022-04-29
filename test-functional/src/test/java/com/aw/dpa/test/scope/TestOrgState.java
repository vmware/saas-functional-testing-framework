/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.scope;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.ToString;

/**
 * Holds state data used in scope tests.
 */
@ToString
final class TestOrgState {

    private static final TestOrgState SINGLETON_INSTANCE = new TestOrgState();

    private final Set<String> orgIds = Collections
            .synchronizedSet(new HashSet<>());

    private final Set<String> orgIdsToCleanup = Collections
            .synchronizedSet(new HashSet<>());

    private TestOrgState() {
    }

    static TestOrgState getSingletonInstance() {
        return TestOrgState.SINGLETON_INSTANCE;
    }

    static TestOrgState newInstance() {
        return new TestOrgState();
    }

    int getOrgIdsSize() {
        return this.orgIds.size();
    }

    boolean addOrgId(final String orgId) {
        return this.orgIds.add(orgId);
    }

    void cleanupOrgId(final String orgId) {
        this.orgIdsToCleanup.add(orgId);
    }

    boolean allClear() {
        return this.orgIds.equals(this.orgIdsToCleanup);
    }

    void clearAll() {
        this.orgIds.clear();
        this.orgIdsToCleanup.clear();
    }
}
