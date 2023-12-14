/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

/**
 * Local Service Constants.
 */
public final class LocalstackConstants {

    private LocalstackConstants() {
    }
    public static final String LOCALSTACK_VERSION = "0.13.3";

    public static final String LOCALSTACK_IMAGE_NAME = String.format("localstack/localstack:%s", LOCALSTACK_VERSION);

    public static final int LOCALSTACK_DEFAULT_SERVICE_PORT = 4566;

    public static final String LOCALSTACK_ENDPOINT = "localStackEndpoint";
}
