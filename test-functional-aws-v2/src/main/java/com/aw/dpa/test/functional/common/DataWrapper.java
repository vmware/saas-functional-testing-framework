/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.common;

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
