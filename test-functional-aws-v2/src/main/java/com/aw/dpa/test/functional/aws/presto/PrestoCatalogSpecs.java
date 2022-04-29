/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.presto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Configuration for local presto catalog creation.
 */
@Builder
@Data
public class PrestoCatalogSpecs {

    @Singular
    private List<PrestoCatalogSettings> catalogs;

}
