/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.process;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.springframework.core.io.ClassPathResource;

/**
 * Class used for creating CommandLine command used in testing local process start.
 */
public final class TestCommand {

    private static final String RESOURCES_FILE = "resources.txt";

    private TestCommand() {
    }

    public static CommandLine createCommand() {
        return createCommand(-1);
    }

    public static CommandLine createCommand(final int port) {
        final File resource;
        try {
            resource = new ClassPathResource(RESOURCES_FILE).getFile();
        } catch (final IOException e) {
            throw new RuntimeException("Resource not found " + RESOURCES_FILE, e);
        }
        final StringBuilder optionsBuilder = new StringBuilder();
        if (port != -1) {
            optionsBuilder.append(String.format("-D%s=%s", TestApp.APP_PORT, port));
        }
        final String options = optionsBuilder.length() == 0 ? null : optionsBuilder.toString();
        return new CommandLine("java")
                .addArgument(options)
                .addArgument("-classpath")
                .addArgument(resource.getParent())
                .addArgument(TestApp.class.getCanonicalName());
    }
}
