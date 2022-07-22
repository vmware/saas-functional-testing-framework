/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.lambda.runtime.events;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A mixin used for preparing events with records field.
 */
public abstract class AbstractEventMixin {

    @JsonProperty("Records")
    abstract List<?> getRecords();

    @JsonProperty("Records")
    abstract void setRecords(List<?> records);
}

