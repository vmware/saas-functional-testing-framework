/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda.constants;

/**
 * Test Data.
 */
public final class TestData {

    public static final String INVOKE_LAMBDA_REQUEST_INPUT = "{\"key1\":\"value1\"}";
    public static final String TEST_INVALID_LAMBDA_HANDLER_CLASS_NAME = "some invalid handler name";
    public static final String LAMBDA_EXPECTED_RESULT = "\\[\"value1\"\\]";
    public static final String LAMBDA_EXPECTED_LOG = "\\{key1=value1\\}";

    private TestData() {

    }
}
