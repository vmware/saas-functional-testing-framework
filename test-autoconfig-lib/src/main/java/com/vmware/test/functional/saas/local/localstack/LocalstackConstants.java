/*
 * Copyright 2022-2023 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.localstack;

/**
 * Local Service Constants.
 */
public final class LocalstackConstants {

    private LocalstackConstants() {
    }
    public static final String LOCALSTACK_VERSION = "0.12.5";

    public static final String LOCALSTACK_IMAGE_NAME = String.format("localstack/localstack:%s", LOCALSTACK_VERSION);

    public static final int LOCALSTACK_DEFAULT_SERVICE_PORT = 4566;

    public static final String LOCALSTACK_ENDPOINT = "localStackEndpoint";
}
