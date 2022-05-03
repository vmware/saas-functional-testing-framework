/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.process.wait.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vmware.test.functional.saas.process.LocalTestProcessContext;

/**
 * Class used for providing different {@link WaitStrategy} for process verification.
 * <p>
 * The caller obtains an instance of {@link HttpWaitStrategy} class using
 * {@code forHttp} method. It accept both URL string or URL supplier. Use the former if the URL value
 * can be determined early enough and the latter when the URL computation must be delayed. A typical example of supplier use is
 * when we want to calculate a random server port just before the process has been started thus we ensure no one else can
 * allocate the port after it is calculated and before the server is bound to it.
 * </p>
 */
public final class WaitStrategyBuilder {

    private final List<WaitStrategy> waitStrategyList = new ArrayList<>();

    public WaitStrategyBuilder() {
    }

    /**
     * Creates HttpWaitStrategy for URL health check.
     *
     * @param urlSupplier the path to check
     * @return the WaitStrategy
     */
    public WaitStrategyBuilder forHttp(final Supplier<String> urlSupplier) {
        this.waitStrategyList.add(new HttpWaitStrategy(urlSupplier));
        return this;
    }

    /**
     * Creates HttpWaitStrategy for URL health check.
     *
     * @param url the URL to check
     * @return the WaitStrategy
     */
    public WaitStrategyBuilder forHttp(final String url) {
        this.waitStrategyList.add(new HttpWaitStrategy(() -> url));
        return this;
    }

    /**
     * Creates LogStreamMessageWaitStrategy for log message verification.
     *
     * @param regex the regex pattern to check for
     * @return LogMessageWaitStrategy
     */
    public WaitStrategyBuilder forLogMessagePattern(final String regex) {
        this.waitStrategyList.add(new LogStreamMessageWaitStrategy().withRegEx(regex));
        return this;
    }

    /**
     * Builds {@link CompositeWaitStrategy} for multiple wait verifications.
     *
     * @return WaitStrategy
     */
    public WaitStrategy build() {
        return new CompositeWaitStrategy(this.waitStrategyList);
    }

    private static class CompositeWaitStrategy implements WaitStrategy {

        final List<WaitStrategy> waitStrategies;
        final List<WaitStrategy> completedWaitStrategiesCache = new ArrayList<>();

        CompositeWaitStrategy(final List<WaitStrategy> waitStrategies) {
            this.waitStrategies = waitStrategies;
        }

        @Override
        public boolean hasCompleted(final LocalTestProcessContext localTestProcessContext) {
            for (WaitStrategy waitStrategy : this.waitStrategies) {
                if (!this.completedWaitStrategiesCache.contains(waitStrategy)) {
                    if (waitStrategy.hasCompleted(localTestProcessContext)) {
                        this.completedWaitStrategiesCache.add(waitStrategy);
                    }
                }
            }
            localTestProcessContext.getWaitStrategiesLogResult().set(getFailedWaitStrategiesAsString());

            // all wait strategies completed successfully
            return this.completedWaitStrategiesCache.size() == this.waitStrategies.size();
        }

        private String getFailedWaitStrategiesAsString() {
            final StringBuilder failedWaitStrategiesInfo = new StringBuilder();

            this.waitStrategies.forEach(waitStrategy -> {
                if (!this.completedWaitStrategiesCache.contains(waitStrategy)) {
                    failedWaitStrategiesInfo.append(waitStrategy.toString());
                }
            });

            return failedWaitStrategiesInfo.toString();
        }
    }
}
