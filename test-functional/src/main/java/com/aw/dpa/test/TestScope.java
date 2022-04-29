/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.aw.dpa.test.environment.operations.Disposable;

/**
 * A special scope used for beans that will be recycled for every test class.
 * To be used in Functional test context initialization to manage annotated beans lifecycle.
 * Destruction callbacks, if provided, will be executed on remove.
 * {@code com.aw.dpa.test.environment.operations.Disposable} beans will be executed
 * right after the test execution.
 */
public class TestScope implements Scope {

    public static final String SCOPE_TEST_CLASS = "testClass";

    private static class ScopedInstances {
        private final Map<String, Object> scopedObjects = new HashMap<>();
        private final Map<String, Runnable> destructionCallbacks = new HashMap<>();
    }

    private final ThreadLocal<ScopedInstances> scopedInstances = ThreadLocal
            .withInitial(ScopedInstances::new);

    TestScope() {
    }

    /**
     * Removes all scoped objects and registered destruction callbacks.
     */
    public void removeAll() {
        this.scopedInstances.get().destructionCallbacks.values().forEach(Runnable::run);
        this.scopedInstances.get().destructionCallbacks.clear();
        this.scopedInstances.get().scopedObjects.clear();
        this.scopedInstances.remove();
    }

    /**
     * Teardown all scoped objects of type {@link Disposable}.
     */
    public void teardownAll() {
        this.scopedInstances.get().scopedObjects.values().stream()
                .filter(Disposable.class::isInstance)
                .map(Disposable.class::cast)
                .forEach(Disposable::teardown);
    }

    @Override
    public Object get(final String name, final ObjectFactory<?> objectFactory) {
        if (!this.scopedInstances.get().scopedObjects.containsKey(name)) {
            this.scopedInstances.get().scopedObjects.put(name, objectFactory.getObject());
        }
        return this.scopedInstances.get().scopedObjects.get(name);
    }

    @Override
    public Object remove(final String name) {
        this.scopedInstances.get().destructionCallbacks.get(name).run();
        this.scopedInstances.get().destructionCallbacks.remove(name);
        return this.scopedInstances.get().scopedObjects.remove(name);
    }

    @Override
    public void registerDestructionCallback(final String name, final Runnable callback) {
        this.scopedInstances.get().destructionCallbacks.put(name, callback);
    }

    @Override
    public Object resolveContextualObject(final String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
