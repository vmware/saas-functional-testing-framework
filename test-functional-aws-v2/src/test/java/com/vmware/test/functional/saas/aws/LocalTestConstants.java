/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws;

import com.vmware.test.functional.saas.PortSupplier;

/**
 * Constants for local test infrastructure.
 */
public final class LocalTestConstants {

    public static final String TEST_ACCESS_KEY_ID = "test_access_key_id";
    public static final String TEST_SECRET_KEY_ID = "test_secret_key_id";

    public static final String KINESALITE_DOCKER_IMAGE = "instructure/kinesalite:latest";
    public static final int KINESALITE_PORT = 4567;
    public static final int KINESALITE_HOST_PORT = new PortSupplier().getAsInt();

    public static final String LOCALSTACK_DOCKER_IMAGE_NAME = "localstack/localstack:0.12.5";
    public static final int LAMBDA_HOST_PORT = new PortSupplier().getAsInt();
    public static final int LAMBDA_SERVICE_PORT = 4566;

    private LocalTestConstants() {

    }
}
