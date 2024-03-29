/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.common;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Generic wrapper class for storing data and its transformed equivalent.
 *
 * @param <D> Type of the original data.
 * @param <T> Type of the transformed data.
 */
@ToString
public class DataWrapper<D, T> {

    @Getter
    private final D data;
    private T transformedData;
    @NonNull
    private final ThrowingFunction<D, T> dataMapper;

    public DataWrapper(@NonNull final ThrowingFunction<D, T> dataMapper, final D data) {
        this.dataMapper = dataMapper;
        this.data = data;
    }

    /**
     * Returns the result or applying {@code dataMapper} to {@code data}.
     *
     * @return The transformed data.
     */
    public T getTransformedData() {
        if (this.transformedData == null) {
            this.transformedData = this.dataMapper.apply(this.data);
        }
        return this.transformedData;
    }
}
