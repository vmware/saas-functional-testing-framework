/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.utils;

import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public final class LambdaResponseUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LambdaResponseUtils() {

    }

    @SneakyThrows
    public static String getErrorMessageOrNull(final InvokeResponse invokeResponse) {
        if (invokeResponse.functionError() == null) {
            return null;
        }
        return OBJECT_MAPPER.readTree(invokeResponse.payload().asUtf8String()).get("errorMessage").asText();
    }
}
