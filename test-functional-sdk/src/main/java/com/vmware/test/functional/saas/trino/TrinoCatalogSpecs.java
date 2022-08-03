/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
