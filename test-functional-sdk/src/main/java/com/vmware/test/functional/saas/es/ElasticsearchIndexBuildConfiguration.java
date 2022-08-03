/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.es;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local Elasticsearch index creation.
 */
@Builder
@Data
public class ElasticsearchIndexBuildConfiguration {

    private List<ElasticsearchIndexSettings> indicesToCreate;
}
