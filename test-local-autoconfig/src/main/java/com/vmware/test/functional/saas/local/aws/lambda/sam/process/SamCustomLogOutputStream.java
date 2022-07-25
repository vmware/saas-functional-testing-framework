/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.process;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.exec.LogOutputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * Custom class extending {@link LogOutputStream}. To be used for configuring how sam local process logs are collected.
 * Sends/redirects ("pumps") the script {@code stdout} to the application logs ({@code INFO} level).
 */
@Slf4j
class SamCustomLogOutputStream extends LogOutputStream {

    private final PrintWriter printWriter;

    SamCustomLogOutputStream(final String logfile) throws IOException {
        final FileWriter fileWriter = new FileWriter(logfile, StandardCharsets.UTF_8);
        this.printWriter = new PrintWriter(fileWriter);
    }

    @Override
    protected void processLine(final String line, final int logLevel) {
        log.debug(line);
        this.printWriter.print(line);
        this.printWriter.print("\n");
        this.printWriter.flush();
    }

    @Override
    public void close() {
        this.printWriter.close();
    }

    @Override
    public void flush() {
        this.printWriter.flush();
    }

}
