/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.lambda;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.core.env.Environment;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Configuration for local lambda functions creation.
 * AWS SAM template file is generated as a result of these required fields. Template is required by SAM to start the lambda locally.
 * Environment ({@link Map}) may be provided deferred ({@link Supplier}) so that env computation may be delayed.
 */
@Builder
@Data
public class LambdaFunctionSpecs {

    private final Environment environment;
    private final Supplier<Map<String, String>> environmentSupplier;
    @NonNull
    private final String functionName;
    @Builder.Default
    private final Integer timeoutInSeconds = 20;
    @NonNull
    private final String handlerClass;
    @NonNull
    private final String lambdaCodeDir;
    @Builder.Default
    private final Integer memorySize = 128;
    private final String namespace;
    @Builder.Default
    private final String runtime = "java11";
}
