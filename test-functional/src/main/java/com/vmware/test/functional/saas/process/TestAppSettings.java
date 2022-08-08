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
 * Test application configuration properties.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Qualifier("testAppSettings")
public class TestAppSettings {

    private String apphome;

    private String executableJar;
}
