/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.lambda;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.aw.dpa.test.FunctionalTest;
import com.aw.dpa.test.aws.local.lambda.context.TestContext;

@ContextHierarchy(@ContextConfiguration(classes = TestContext.FullContext.class))
@FunctionalTest
public abstract class AbstractFullContextTest extends AbstractTestNGSpringContextTests {
}
