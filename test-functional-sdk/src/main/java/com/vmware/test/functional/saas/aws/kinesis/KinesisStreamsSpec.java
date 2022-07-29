/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.kinesis;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local kinesis streams creation.
 */
@Builder
@Data
public class KinesisStreamsSpec {

    private List<String> streamsToCreate;
}
