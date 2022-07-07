/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.aws.lambda.context.TestContext;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.FullContext.class))
@FunctionalTest
public abstract class AbstractFullContextTest extends AbstractTestNGSpringContextTests {
}
