/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.common;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument, produces a result or throws an exception.
 * @param <T> argument type
 * @param <R> result type
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {

    /**
     * Gets a result allowing exception.
     *
     * @return a result
     */
    @Override
    default R apply(T elem) {
        try {
            return applyThrows(elem);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies this function to the given argument.
     *
     * @param elem the function argument
     * @return the function result
     * @throws Exception function exception
     */
    R applyThrows(T elem) throws Exception;
}
