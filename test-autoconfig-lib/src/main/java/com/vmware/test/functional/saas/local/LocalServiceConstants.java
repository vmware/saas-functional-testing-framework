/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

/**
 * Local Service Constants.
 */
public final class LocalServiceConstants {

    private LocalServiceConstants() {
    }
    public static final String LOCALSTACK_VERSION = "0.12.5";

    public static final String LOCALSTACK_IMAGE_NAME = String.format("localstack/localstack:%s", LOCALSTACK_VERSION);

    public static final int LOCALSTACK_DEFAULT_SERVICE_PORT = 4566;

    public static final String LOCALSTACK_ENDPOINT = "localStackEndpoint";
}
