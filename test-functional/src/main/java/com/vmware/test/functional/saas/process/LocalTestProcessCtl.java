/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.Lifecycle;
import org.springframework.core.env.Environment;

import com.vmware.test.functional.saas.environment.CalculateEnvironmentUtils;
import com.vmware.test.functional.saas.process.output.LocalProcessStreamHandler;
import com.vmware.test.functional.saas.process.wait.strategy.WaitStrategy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to start local processes.
 * ProcessBuilder command should be created and passed with (if) required properties.
 * For example:{@code (java -jar -DcustomProperty some/custom/path/to/jar.jar}.
 * Wait condition {@link WaitStrategy} should be passed in order to start the service
 * and wait until it is ready to serve  the requests. Environment ({@link Map}) may be provided
 * deferred ({@link Supplier}) so that env computation may be delayed.
 */
@Slf4j
@Builder
public final class LocalTestProcessCtl implements Lifecycle {

    private static final int DESTROY_PROCESS_WAIT_TIME_MILLIS = 10000;
    private static final long THREE_SECONDS = TimeUnit.SECONDS.toMillis(3);

    @Getter
    protected final WaitStrategy waitingFor;

    @Getter
    @Builder.Default
    private final LocalTestProcessContext localTestProcessContext = LocalTestProcessContext.builder().build();

    @Getter(AccessLevel.PACKAGE)
    private final Supplier<CommandLine> command;
    private final Runnable preStartCallback;
    @Getter(AccessLevel.PACKAGE)
    private final Supplier<Map<String, String>> environmentSupplier;
    @Getter(AccessLevel.PACKAGE)
    private final String debugModeEnable;
    @Getter(AccessLevel.PACKAGE)
    private final String debugPort;
    @Getter(AccessLevel.PACKAGE)
    private final String debugSuspendMode;
    private final Environment environment;
    @Getter(AccessLevel.PACKAGE)
    private final String namespace;

    private final ExecuteWatchdog executorWatchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    private final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

    @Builder.Default
    private final Duration startupTimeout = Duration.ofSeconds(120);

    @Builder.Default
    private final UUID instanceId = UUID.randomUUID();

    @Builder.Default
    private final AtomicReference<Boolean> running = new AtomicReference<>(Boolean.FALSE);

    @Override
    public void start() {
        if (Objects.isNull(this.command)) {
            throw new RuntimeException("Command to start local process not specified. Create CommandLine and pass to LocalTestProcessCtl.Builder");
        }
        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            throw new RuntimeException("Local Test Process ALREADY started: [" + getLocalProcessAsString() + "]");
        }

        final PumpStreamHandler psh = new PumpStreamHandler(new LocalProcessStreamHandler(log, this.localTestProcessContext.getLogOutput()), this.errorStream);
        final Executor executor = new DefaultExecutor();
        executor.setStreamHandler(psh);
        executor.setWatchdog(this.executorWatchdog);

        try {
            if (this.preStartCallback != null) {
                this.preStartCallback.run();
            }

            if (log.isInfoEnabled()) {
                log.info("Starting local process: [{}]", getLocalProcessAsString());
            }

            final CommandLine commandLineArguments = setDebugArguments();

            Map<String, String> currentProcessEnv = null;
            if (this.environmentSupplier != null) {
                currentProcessEnv = CalculateEnvironmentUtils.calculateEnv(this.environmentSupplier.get(), this.environment, this.namespace);
            }
            executor.execute(commandLineArguments, currentProcessEnv, this.resultHandler);
            pauseUntilProcessHealthCheckSucceeds();
        } catch (final IOException e) {
            throw new RuntimeException("Process [" + getLocalProcessAsString() + "] was not started.", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process [" + getLocalProcessAsString() + "] was not started.", e);
        }
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            log.warn("Local Test Process ALREADY stopped");
            return;
        }
        try {
            log.info("Stopping local process: [{}]...", getLocalProcessCommandExecutable());

            if (!this.executorWatchdog.isWatching()) {
                log.info("Process has already been destroyed [{}]", getLocalProcessAsString());
                return;
            }

            this.executorWatchdog.destroyProcess();
            try {
                this.resultHandler.waitFor(DESTROY_PROCESS_WAIT_TIME_MILLIS);
            } catch (final InterruptedException exn) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(exn);
            }

            if (isProcessActive()) {
                log.error("Process was not destroyed [{}]", getLocalProcessAsString());
            }
        } finally {
            log.info("Stopping local process: [{}]... DONE", getLocalProcessCommandExecutable());
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    private boolean isProcessActive() {
        return !this.resultHandler.hasResult();
    }

    private void pauseUntilProcessHealthCheckSucceeds() throws InterruptedException, UnsupportedEncodingException {
        final long expiry = this.startupTimeout.toMillis() + Clock.systemDefaultZone().millis();

        // wait until startupTimeout period and until process is active
        while (Clock.systemDefaultZone().millis() < expiry && isProcessActive()) {
            // check if wait strategies completed successfully
            if (this.waitingFor != null) {
                if (hasWaitCompleted()) {
                    return; // the process is running
                }
                log.info("Evaluating health check for [{}]... FAILED (delaying 3 seconds)", getLocalProcessCommandExecutable());
                // Wait a few seconds before re-attempting the health check.
                Thread.sleep(THREE_SECONDS);
            } else {
                return; // we have no health check configured for the process. Assume it is running.
            }
        }

        if (!isProcessActive()) {
            if (this.resultHandler.getExitValue() == 0) {
                if (hasWaitCompleted()) {
                    return;
                }
                // process has exited successfully but wait strategies did not complete
                throw new RuntimeException(
                        "Process [" + getLocalProcessAsString()
                                + "] has exited with exit code = 0 before wait strategies completed successfully");
            }
            // process was terminated and exited with exception
            throw new RuntimeException(
                    "Process [" + getLocalProcessAsString()
                            + "] has exited with code = " + this.resultHandler.getExitValue()
                            + " with exception: " + this.resultHandler.getException()
                            + "\nProcess error output: \n"
                            + this.errorStream.toString(StandardCharsets.UTF_8.name()));
        }

        // process is still active but wait strategies did not complete within startup period
        throw new RuntimeException("Process [" + getLocalProcessAsString()
                + "] was started but wait strategies did not complete successfully within startup time period of "
                + this.startupTimeout.getSeconds() + " seconds. Failed waiting for : \n"
                + this.localTestProcessContext.getWaitStrategiesLogResult()
                + "\nProcess error output:\n"
                + this.errorStream.toString(StandardCharsets.UTF_8.name()));
    }

    private boolean hasWaitCompleted() {
        log.info("Evaluating health check for [{}]...", getLocalProcessCommandExecutable());
        if (this.waitingFor.hasCompleted(this.localTestProcessContext)) {
            log.info("Evaluating health check for [{}]... SUCCESS", getLocalProcessCommandExecutable());
            return true;
        }
        return false;
    }

    private String getLocalProcessCommandExecutable() {
        return this.instanceId + " " + this.command.get().getExecutable();
    }

    private String getLocalProcessAsString() {
        String envVariables = "";
        if (this.environmentSupplier != null) {
            envVariables = this.environmentSupplier.get().entrySet()
                    .stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(" "));
            envVariables += " ";
        }

        return envVariables
                + getLocalProcessCommandExecutable()
                + " "
                + String.join(" ", this.command.get().getArguments());
    }

    private CommandLine setDebugArguments() {
        final CommandLine cmd = new CommandLine(this.command.get().getExecutable());
        if (Boolean.parseBoolean(this.debugModeEnable)
                && StringUtils.isNotBlank(this.debugPort)) {
            final String suspendMode = StringUtils.equals(this.debugSuspendMode, "y") ? this.debugSuspendMode : "n";
            final String[] arguments = {
                    "-Xdebug",
                    String.format("-Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%s", suspendMode, this.debugPort),
            };
            cmd.addArguments(arguments);
            return cmd.addArguments(this.command.get().getArguments());
        }
        return this.command.get();
    }

    /**
     * A dummy class to make Javadoc and lombok play nicely together.
     */
    public static class LocalTestProcessCtlBuilder {

    }
}
