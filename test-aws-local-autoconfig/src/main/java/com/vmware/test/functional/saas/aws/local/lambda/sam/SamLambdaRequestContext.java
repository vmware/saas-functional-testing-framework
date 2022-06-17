/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.local.lambda.sam;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.vmware.test.functional.saas.aws.lambda.LambdaRequestContext;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.vmware.test.functional.saas.aws.local.lambda.sam.SamLambdaLogsHelper.TEST_AWS_LOCAL_REQUEST_ID;
import static com.vmware.test.functional.saas.aws.logback.LambdaAppender.REQID_FILE;
import static com.vmware.test.functional.saas.aws.local.lambda.sam.SamLambdaLogsHelper.isFromRequest;

/**
 * Sam lambda request context class.
 */
@Slf4j
class SamLambdaRequestContext implements LambdaRequestContext {

    @NonNull
    final String functionName;

    @NonNull
    final String functionCodeDir;

    final int waitForLogTimeoutSeconds;

    @NonNull
    final String lambdaLogFile;

    @NonNull
    final ReentrantLock threadLock;

    private FileLock fileLock;

    @Getter
    private String requestId;

    @Builder
    SamLambdaRequestContext(@NonNull final String functionName,
            @NonNull final String functionCodeDir,
            final int waitForLogTimeoutSeconds,
            @NonNull final String lambdaLogFile,
            @NonNull final ReentrantLock threadLock) {
        this.functionName = functionName;
        this.functionCodeDir = functionCodeDir;
        this.waitForLogTimeoutSeconds = waitForLogTimeoutSeconds;
        this.lambdaLogFile = lambdaLogFile;
        this.threadLock = threadLock;
    }

    @Override
    @SneakyThrows
    public void close() {
        log.info("Closing SamLambdaRequestContext [{}]", this.requestId);
        try {
            if (this.fileLock != null) {
                waitForLogLineForRequestContext();
            }
        } finally {
            if (this.fileLock != null) {
                this.fileLock.release();
                log.info("Released file lock [{}]", this.fileLock);
            }
            this.threadLock.unlock();
            log.info("Released thread lock [{}]", this.threadLock);
        }
    }

    void initialize() throws IOException {
        log.info("Initializing SamLambdaRequestContext with [function name: {}, code dir: {}, log file: {}]",
                this.functionName,
                this.functionCodeDir,
                this.lambdaLogFile);
        acquireLocks();
        generateRequestId();
    }

    private void generateRequestId() throws IOException {
        final String newRequestId = UUID.randomUUID().toString();
        log.info("{} [{}] generated", TEST_AWS_LOCAL_REQUEST_ID, newRequestId);
        final Path codeDirPath = Path.of(this.functionCodeDir);
        final Path reqidFile = codeDirPath.resolve(REQID_FILE);
        if (codeDirPath.toFile().isDirectory()) {
            Files.write(reqidFile, newRequestId.getBytes(StandardCharsets.UTF_8));
        }
        log.info("{} [{}] written to file [{}]", TEST_AWS_LOCAL_REQUEST_ID, newRequestId, reqidFile);
        this.requestId = newRequestId;
    }

    private void acquireLocks() throws IOException {
        this.threadLock.lock();
        log.info("Acquired thread lock [{}] ", this.threadLock);
        final Path codeDirPath = Path.of(this.functionCodeDir);
        final FileOutputStream fileOutputStream = new FileOutputStream(codeDirPath.resolve("reqidlock").toFile());
        final FileChannel channel = fileOutputStream.getChannel();
        this.fileLock = channel.lock();
        log.info("Acquired file lock [{}]", this.fileLock);
    }

    /**
     * Blocks current thread and reads the lambda logs until it finds a line that contains the requestId.
     * This means the lambda has loaded its context and the requestId for this invocation, meaning the file
     * is free for modification for the next requestId.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "sam.log file always has a parent")
    @SneakyThrows
    private void waitForLogLineForRequestContext() {
        log.info("Waiting for [{}] to initialize its context with RequestId [{}]", this.functionName, this.requestId);
        try (BufferedReader reader = new BufferedReader(new FileReader(this.lambdaLogFile, StandardCharsets.UTF_8))) {
            final long expiry = Duration.ofSeconds(this.waitForLogTimeoutSeconds).toMillis() + Clock.systemDefaultZone().millis();
            while (Clock.systemDefaultZone().millis() < expiry) {
                final String logLine = reader.readLine();
                if (logLine != null && isFromRequest(logLine, this.requestId)) {
                    log.info("Found log line with RequestId [{}]: [{}]", this.requestId, logLine);
                    return;
                }
            }
        }
        final String samLogFile = Path.of(this.lambdaLogFile).getParent().resolve("sam.log").toString();
        log.error("Could not find log line with requestId [{}] for lambda [{}] in log file [{}]. Check Sam logs in [{}] for more info.",
                this.requestId, this.functionName, this.lambdaLogFile, samLogFile);
    }
}
