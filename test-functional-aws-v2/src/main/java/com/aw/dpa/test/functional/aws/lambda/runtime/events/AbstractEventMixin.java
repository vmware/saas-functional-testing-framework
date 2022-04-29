/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.lambda.runtime.events;

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

