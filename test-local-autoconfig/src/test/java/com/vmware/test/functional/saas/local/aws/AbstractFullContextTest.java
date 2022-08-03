/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.vmware.test.functional.saas.FunctionalTest;
import com.vmware.test.functional.saas.local.context.TestContext;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.FullTestContext.class))
@FunctionalTest
public abstract class AbstractFullContextTest extends AbstractTestNGSpringContextTests  {
}
