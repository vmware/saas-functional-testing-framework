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
@Qualifier("testAppDebugSettings")
public class TestAppDebugSettings {

    private boolean debugModeEnable = false;

    private int debugPort = 8998;

    private String debugSuspend = "n";
}
