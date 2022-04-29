/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Test application configuration properties.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Qualifier("dpaTestApp")
public class DpaTestApp {

    @Value("${app.apphome}")
    private String apphome;

    @Value("${app.executable.jar}")
    private String executableJar;
}
