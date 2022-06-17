/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.lambda.sam.process;

import software.amazon.awssdk.services.lambda.model.Runtime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.CollectionUtils;

import com.vmware.test.functional.saas.FunctionalTestExecutionSettings;
import com.vmware.test.functional.saas.LocalServiceEndpoint;
import com.vmware.test.functional.saas.SmartLifecyclePhases;
import com.vmware.test.functional.saas.aws.local.lambda.sam.data.SamFunctionTemplateData;
import com.vmware.test.functional.saas.aws.local.lambda.sam.data.SamTemplateGenerator;
import com.vmware.test.functional.saas.environment.CalculateEnvironmentUtils;
import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;
import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to start AWS SAM local lambda process.
 * AWS SAM local start-lambda command will be used to start and execute lambda described
 * in a SAM {@code template.yaml} file.
 */
@Slf4j
public class SamProcessControl implements SmartLifecycle {

    private static final String DEFAULT_JAVA_OPTIONS = "-XX:MaxHeapSize=2834432k -XX:MaxMetaspaceSize=163840k";
    private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
    // SAM requires a few seconds to open start-lambda endpoint to listen to
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(5);
    private static final String TEMPLATE_FILE_NAME = "template.yaml";
    private static final String LAMBDA_LOG_FILE_NAME = "lambda.log";
    private static final String SAM_COMMAND_LOGS_FILE_NAME = "sam.log";
    private static final long SAM_VERSION_COMMAND_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private final CommandLine cmd = new CommandLine(System.getProperty("lambda.sam.command", "/usr/local/bin/sam"));
    private final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    private final SamCustomExecuteWatchdog executorWatchdog = new SamCustomExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    @Getter
    private final AtomicBoolean running = new AtomicBoolean();

    // Sam process specific properties to be set required to start sam local start-lambda process
    private final LocalServiceEndpoint lambdaEndpoint;
    private final List<LambdaFunctionSpecs> lambdaFunctionSpecs;
    private final boolean debugModeEnabled;
    private final String debugPort;
    private final String[] additionalCommandLineArgs;
    private final FunctionalTestExecutionSettings functionalTestExecutionSettings;

    private final String samLogsBaseDir;
    private final String samTemplatesBaseDir;
    @Getter
    private final String lambdaLogFile;
    private final String samLogFile;
    @Getter
    private final String templateFile;

    @Setter(AccessLevel.PACKAGE)
    private Supplier<Executor> executorSupplier;

    @Builder
    public SamProcessControl(@NonNull final LocalServiceEndpoint lambdaEndpoint,
            @NonNull final List<LambdaFunctionSpecs> lambdaFunctionSpecs,
            final boolean debugModeEnabled,
            final String debugPort,
            final String[] additionalCommandLineArgs,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        this.lambdaEndpoint = lambdaEndpoint;
        this.lambdaFunctionSpecs = lambdaFunctionSpecs;
        this.debugModeEnabled = debugModeEnabled;
        this.debugPort = debugPort;
        this.additionalCommandLineArgs = Objects.requireNonNullElseGet(additionalCommandLineArgs, () -> new String[] { "--skip-pull-image" });
        this.functionalTestExecutionSettings = functionalTestExecutionSettings;
        this.executorSupplier = DefaultExecutor::new;

        // directories where sam template and logs will be put
        final String samBaseDir = "./target/sam-" + System.currentTimeMillis();
        this.samTemplatesBaseDir = samBaseDir + "/lambda/templates";
        this.templateFile = this.samTemplatesBaseDir + "/" + TEMPLATE_FILE_NAME;

        this.samLogsBaseDir = samBaseDir + "/logs";
        this.lambdaLogFile = this.samLogsBaseDir + "/" + LAMBDA_LOG_FILE_NAME;
        this.samLogFile = this.samLogsBaseDir + "/" + SAM_COMMAND_LOGS_FILE_NAME;
    }

    private static String getCommandAsString(final CommandLine command, final long pid) {
        final String pidString = pid == -1 ? "pid [unknown] " : "pid [" + pid + "] ";
        return pidString + command.getExecutable() + " " + String.join(" ", command.getArguments());
    }

    /**
     * Execute AWS SAM local start-lambda command to prepare and start lambda described
     * in a SAM {@code template.yaml} file.
     */
    @Override
    public void start() {
        if (this.functionalTestExecutionSettings != null && this.functionalTestExecutionSettings.shouldSkip(this)) {
            log.info("Skip starting sam process");
            return;
        }

        if (CollectionUtils.isEmpty(this.lambdaFunctionSpecs)) {
            log.info("No lambda function specs provided. Sam process will not be started.");
            return;
        }

        checkSamVersion();

        if (!this.running.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
            throw new RuntimeException("SAM local start-lambda process ALREADY started.");
        }

        // Set up Sam execution template and logs directories before starting the command
        final File tmpLogFileDir = new File(this.samLogsBaseDir);
        if (!tmpLogFileDir.exists()) {
            Preconditions.checkArgument(tmpLogFileDir.mkdirs(), "SAM logs folder was not created.");
        } else {
            log.warn("Sam Logs Base Dir [{}] already exists. Directory will be overridden.", this.samLogsBaseDir);
        }

        final File tmpTemplateFileDir = new File(this.samTemplatesBaseDir);
        if (!tmpTemplateFileDir.exists()) {
            Preconditions.checkArgument(tmpTemplateFileDir.mkdirs(), "SAM templates folder was not created.");
        } else {
            log.warn("Sam Templates Base Dir [{}] already exists. Directory will be overridden.", this.samTemplatesBaseDir);
        }

        // create sam template.yaml file based on provided list of lambda function specs
        generateSamTemplateFile();

        log.debug("Starting SAM local start-lambda command for template.yaml [{}].", this.templateFile);
        doStart();
        log.info("SAM local start-lambda command started on [http://localhost:{}]", this.lambdaEndpoint.getPort());
    }

    private void generateSamTemplateFile() {
        final List<SamFunctionTemplateData> functionTemplateDataList = this.lambdaFunctionSpecs.stream().map(function -> {
            // Calculate the lambda environment
            final Map<String, String> envVariables = new HashMap<>();
            if (Runtime.JAVA8.toString().equals(function.getRuntime())) {
                throw new RuntimeException(Runtime.JAVA8 + " is not a valid lambda runtime. Pass java11 as runtime "
                        + "to start lambda locally.");
            }
            // Setting default values for jvm MaxHeapSize and MaxMetaspaceSize
            if (function.getRuntime() == null || Runtime.JAVA11.toString().equals(function.getRuntime())) {
                envVariables.put("_JAVA_OPTIONS", DEFAULT_JAVA_OPTIONS);
            }
            if (function.getEnvironmentSupplier() != null) {
                envVariables.putAll(CalculateEnvironmentUtils
                        .calculateEnv(function.getEnvironmentSupplier().get(), function.getEnvironment(),
                                function.getNamespace()));
            }

            return SamFunctionTemplateData.builder()
                    .name(function.getFunctionName())
                    .codeUri(function.getLambdaCodeDir())
                    .handlerClass(function.getHandlerClass())
                    .timeout(function.getTimeoutInSeconds())
                    .environment(envVariables)
                    .memorySize(function.getMemorySize())
                    .runtime(function.getRuntime())
                    .build();
        }).collect(Collectors.toList());

        // generate SAM template file describing the lambda to be invoked
        SamTemplateGenerator.generate(functionTemplateDataList, this.templateFile);
    }

    private void doStart() {
        try {
            createSAMCommand();
            final PumpStreamHandler psh = new PumpStreamHandler(new SamCustomLogOutputStream(this.samLogFile));
            final Executor executor = this.executorSupplier.get();
            executor.setStreamHandler(psh);
            executor.setWatchdog(this.executorWatchdog);
            log.info("Executing command [{}]", getCommandAsString(this.cmd, this.executorWatchdog.getPid()));
            executor.execute(this.cmd, this.resultHandler);

            pauseUntilSAMProcessIsRunning();
        } catch (final IOException exn) {
            throw new IllegalStateException("Failed to run " + getCommandAsString(this.cmd, this.executorWatchdog.getPid()), exn);
        } catch (final InterruptedException exn) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to run " + getCommandAsString(this.cmd, this.executorWatchdog.getPid()), exn);
        }
    }

    /**
     * Stop SAM local start-lambda process.
     */
    @Override
    public void stop() {
        if (!this.running.compareAndSet(Boolean.TRUE, Boolean.FALSE) || !isSAMProcessActive()) {
            log.warn("SAM local start-lambda process ALREADY stopped");
            return;
        }

        try {
            log.info("Stopping SAM local start-lambda process: [{}]...", this.executorWatchdog.getPid());

            if (!this.executorWatchdog.isWatching()) {
                log.info("SAM local start-lambda process has already been destroyed [{}]", this.executorWatchdog.getPid());
                return;
            }

            this.executorWatchdog.destroyProcess();
        } finally {
            log.info("Stopping SAM local start-lambda process: [{}]... DONE", this.executorWatchdog.getPid());
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    private boolean isSAMProcessActive() {
        return !this.resultHandler.hasResult();
    }

    private void pauseUntilSAMProcessIsRunning() throws InterruptedException {
        final long expiry = STARTUP_TIMEOUT.toMillis() + Clock.systemDefaultZone().millis();

        // wait until startupTimeout period and until process is active
        while (Clock.systemDefaultZone().millis() < expiry && isSAMProcessActive()) {
            // check if SAM local port is open
            if (isSAMEndpointListening(this.lambdaEndpoint.getPort())) {
                log.info("Evaluating health check for [{}]... SUCCESS", getCommandAsString(this.cmd, this.executorWatchdog.getPid()));
                return; // the port is open and used by SAM
            }
            log.info("Evaluating health check for [{}]... FAILED (delaying 1 second)", this.executorWatchdog.getPid());
            // Wait before re-attempting the health check.
            Thread.sleep(ONE_SECOND);
        }

        if (!isSAMProcessActive()) {
            if (this.resultHandler.getExitValue() == 0) {
                // process has exited successfully
                throw new RuntimeException(
                        "SAM local process [" + getCommandAsString(this.cmd, this.executorWatchdog.getPid())
                                + "] has exited with exit code = 0 unexpectedly.");
            }
            // process was terminated and exited with exception
            throw new RuntimeException(
                    "SAM local process [" + getCommandAsString(this.cmd, this.executorWatchdog.getPid())
                            + "] has exited with code = " + this.resultHandler.getExitValue()
                            + " with exception: " + this.resultHandler.getException()
                            + "\n. Check log file located in [" + this.samLogFile + "] for error messages.");
        }

        // process is still active but wait strategies did not complete within startup period
        throw new RuntimeException("SAM local process [" + getCommandAsString(this.cmd, this.executorWatchdog.getPid())
                + "] was started but something went wrong with connecting to SAM local start-lambda port [http://localhost:" + this.lambdaEndpoint.getPort()
                + "].");
    }

    private void createSAMCommand() {
        this.cmd.addArgument("local");
        this.cmd.addArgument("start-lambda");
        this.cmd.addArguments(new String[] { "--template", this.templateFile });
        this.cmd.addArguments(new String[] { "--host", "0.0.0.0" });
        this.cmd.addArguments(new String[] { "--port", String.valueOf(this.lambdaEndpoint.getPort()) });
        if (Objects.nonNull(this.lambdaEndpoint.getContainerConfig())) {
            this.cmd.addArguments(new String[] { "--docker-network", this.lambdaEndpoint.getContainerConfig().getNetworkInfo().getName() });
        }
        this.cmd.addArguments(this.additionalCommandLineArgs);

        this.cmd.addArguments(new String[] { "--log-file", this.lambdaLogFile });

        if (this.debugModeEnabled) {
            this.cmd.addArguments(new String[] { "-d", this.debugPort });
            this.cmd.addArgument("--debug");
        }
    }

    private boolean isSAMEndpointListening(final int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return socket.isConnected();
        } catch (final Exception e) {
            log.info("SAM local start-lambda endpoint is not listening on port [{}]. Exception thrown : [{}]", port, e.getMessage());
            return false;
        }
    }

    private void checkSamVersion() {
        final CommandLine getSamVersion = new CommandLine(this.cmd).addArgument("--version");
        log.info("Running command [{}]", getCommandAsString(getSamVersion, -1));
        final Executor executor = this.executorSupplier.get();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(SAM_VERSION_COMMAND_TIMEOUT);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(outputStream));
        try {
            executor.execute(getSamVersion);
        } catch (final IOException ex) {
            throw new RuntimeException(
                    String.format("Failed running [%s] to verify its version. Process killed due to timeout: [%s]",
                            getCommandAsString(getSamVersion, -1), watchdog.killedProcess()),
                    ex);
        }
        SamProcessControlHelper.verifySamVersion(outputStream.toString(StandardCharsets.UTF_8));
    }

    /*
     * It does not matter at which phase sam is started as at moment as lambda container is started during invoke()
     * in the functional tests and the service dependencies that lambda might talk to are already configured.
     */
    @Override
    public final int getPhase() {
        return SmartLifecyclePhases.SAM_PROCESS_CONTROL.getPhase();
    }
}
