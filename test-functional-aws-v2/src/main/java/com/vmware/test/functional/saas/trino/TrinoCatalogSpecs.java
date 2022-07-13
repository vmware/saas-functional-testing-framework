/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.trino;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Configuration for local trino catalog creation.
 */
@Builder
@Data
public class TrinoCatalogSpecs {

    @Singular
    private List<TrinoCatalogSettings> catalogs;

}
