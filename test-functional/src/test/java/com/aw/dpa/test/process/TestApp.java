/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.process;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple Test Application used for testing local process start.
 */
public final class TestApp {

    public static final String TEST_ENV_VAR_1 = "app.env.var1";
    public static final String TEST_ENV_VAR_2 = "app.env.var2";
    public static final String APP_PORT = "app.port";
    public static final String IMMEDIATE = "app.immediate";
    // keep running for 30 seconds by default
    private static final int ITERATIONS = 60;

    private TestApp() {
    }

    public static void main(final String[] args) {
        final String appPortString = System.getProperty(APP_PORT);
        if (appPortString != null) {
            final int appPort = Integer.parseInt(appPortString);
            final Thread serverThread = new Thread(() -> runHealthServer(appPort));
            serverThread.setDaemon(true);
            serverThread.start();
        }
        final String appEnvVar1 = System.getenv(TEST_ENV_VAR_1);
        final String appEnvVar2 = System.getenv(TEST_ENV_VAR_2);

        if (appEnvVar1 != null) {
            System.out.println("Test App Log Line : " + appEnvVar1);
        }

        if (appEnvVar2 != null) {
            System.out.println("Test App Log Line : " + appEnvVar2);
        }

        final boolean immediate = Boolean.parseBoolean(System.getenv(IMMEDIATE));
        if (immediate) {
            return;
        }

        int i = 0;
        while (i < ITERATIONS) {
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Test App Log Line : " + i++);
        }
    }

    private static void runHealthServer(final int port) {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            server.createContext("/", new MyHandler());
            server.setExecutor(null);
            server.start();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            final byte [] response = new byte[0];
            t.sendResponseHeaders(200, response.length);
            final OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }
}
