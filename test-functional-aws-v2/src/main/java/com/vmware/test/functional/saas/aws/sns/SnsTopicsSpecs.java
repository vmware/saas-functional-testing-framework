/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sns;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local SNS topics creation.
 */
@Builder
@Data
public class SnsTopicsSpecs {

    private List<String> topicsToCreate;
}

