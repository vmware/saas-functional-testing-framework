/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.dynamodb;

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
