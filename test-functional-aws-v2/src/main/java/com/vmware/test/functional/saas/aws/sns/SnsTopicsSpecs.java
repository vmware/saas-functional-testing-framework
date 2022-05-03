/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
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

