/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.infrastructure.tools;

import java.io.IOException;

import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.ServiceEndpoint;

import lombok.extern.slf4j.Slf4j;

/**
 * Test that hooks into the infrastructure contexts and provide more fine grained test control.
 * These are used to help developers write, execute and debug other functional tests.
 *
 * Note: These tests should not be used in the regular test runs.
 */
@Slf4j
public class InfrastructureTools extends AbstractFunctionalTests {

    /**
     * The test will block and wait until the developer sends halt signal to the process.
     * One example is when the developer wants to start just the services,
     * without executing any tests and leave the services up and running.
     * In general this test can be executed at the end of any given test sequence.
     * It will then block letting the developer collect some telemetry data from the life processes.
     *
     * @throws IOException - when I/O error
     */
    @Test(groups = "infra-tools:await-user-interaction", priority = Integer.MAX_VALUE)
    public void awaitUserInteraction() throws IOException {
        System.out.println("Hit Ctrl+C on the console to stop ...");
        while (true) {
            final int ch = System.in.read();
            if (ch == -1) {
                break;
            }
        }
    }

    @Test(groups = "infra-tools:report-allocated-ports", priority = Integer.MIN_VALUE)
    public void reportAllocatedPorts() {
        if (this.applicationContext == null) {
            return;
        }
        final String[] localServiceEndpointNames = this.applicationContext.getBeanNamesForType(ServiceEndpoint.class);
        for (String serviceName : localServiceEndpointNames) {
            final ServiceEndpoint serviceEndpoint = this.applicationContext.getBean(serviceName, ServiceEndpoint.class);
            log.info("service endpoint [{}] with URL [{}]", serviceName, serviceEndpoint.getEndpoint());
        }
    }
}
