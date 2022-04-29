/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.IntSupplier;

import javax.net.ssl.SSLServerSocketFactory;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Allows us to defer dynamic port allocation until the port number is requested.
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public final class PortSupplier implements IntSupplier {
    int port = -1;

    @Override
    public int getAsInt() {
        if (this.port < 0) {
            try (ServerSocket socket = SSLServerSocketFactory.getDefault().createServerSocket(0)) {
                socket.setReuseAddress(true);
                this.port = socket.getLocalPort();
                log.info("Dynamically allocated port: [{}]", this.port);
            } catch (final IOException ioException) {
                log.error("Error finding a free dynamic port", ioException);
            }
        }
        return this.port;
    }
}

