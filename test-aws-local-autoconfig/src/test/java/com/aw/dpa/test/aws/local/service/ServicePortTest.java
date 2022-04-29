/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Verify there are no duplicate service ports {@link Service}.
 */
public class ServicePortTest {

    @Test
    public void listServicePorts() {

        final List<Integer> ports = Arrays.stream(Service.values())
                .map(Service::getPort)
                .collect(Collectors.toList());

        final List<Integer> distinctPorts = ports
                .stream()
                .distinct()
                .collect(Collectors.toList());
        assertThat(ports.size(), is(distinctPorts.size()));
    }
}
