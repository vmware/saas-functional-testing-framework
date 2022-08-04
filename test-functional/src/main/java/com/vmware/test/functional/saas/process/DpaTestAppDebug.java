/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Debug options for a test application.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Qualifier("dpaTestAppDebug")
public class DpaTestAppDebug {

    @Value("${dpa.test.app.debug.mode.enable:false}")
    private String debugModeEnable;

    @Value("${dpa.test.app.debug.port:8998}")
    private String debugPort;

    @Value("${dpa.test.app.debug.suspend:n}")
    private String debugSuspend;
}
