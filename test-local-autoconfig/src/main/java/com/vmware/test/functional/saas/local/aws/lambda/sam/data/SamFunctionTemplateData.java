/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.data;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * AWS SAM Template Data used in SAM {@code template.yaml} files.
 */
@Data
@Builder
public class SamFunctionTemplateData {

    private String codeUri;
    private Map<String, String> environment;
    private String name;
    private Integer timeout;
    private String handlerClass;
    private Integer memorySize;
    private String runtime;

}
