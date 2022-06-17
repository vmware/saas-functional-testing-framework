/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.aws.local.context.TestContext;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.FullTestContext.class))
@FunctionalTest
public abstract class AbstractFullContextTest extends AbstractTestNGSpringContextTests  {
}
