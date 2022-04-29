/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.dynamodb;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local DynamoDb tables creation.
 */
@Builder
@Data
public class DynamoDbTablesSpecs {

    private List<DynamoDbTableSettings> tablesToCreate;
}
