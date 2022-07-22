/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
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
