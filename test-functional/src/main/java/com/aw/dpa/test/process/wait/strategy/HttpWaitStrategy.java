/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.process.wait.strategy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Supplier;

import com.aw.dpa.test.process.LocalTestProcessContext;

/**
 * Wait Strategy for local process url health check.
 */
public class HttpWaitStrategy implements WaitStrategy {

    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 10000;
    private final Supplier<String> urlSupplier;

    HttpWaitStrategy(final Supplier<String> urlSupplier) {
        this.urlSupplier = urlSupplier;
    }

    @Override
    public boolean hasCompleted(final LocalTestProcessContext localTestProcessContext) {
        return checkConnectionOkStatus(this.urlSupplier.get());
    }

    private static boolean checkConnectionOkStatus(final String url) {
        try {
            final URL urlObj = new URL(url);
            final URLConnection connection = urlObj.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            final HttpURLConnection httpConnection = (HttpURLConnection)connection;
            final int responseCode = httpConnection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (final IOException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("WaitStrategy for url health check : %s %n", this.urlSupplier.get());
    }
}
