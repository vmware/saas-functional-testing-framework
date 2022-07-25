/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local.aws.lambda.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;

import com.vmware.test.functional.saas.aws.lambda.LambdaFunctionSpecs;
import com.vmware.test.functional.saas.aws.lambda.LambdaRequestContext;
import com.vmware.test.functional.saas.aws.lambda.LambdaRequestContextFactory;
import com.google.common.base.Preconditions; // CHECKSTYLE DISABLE RegexpSinglelineJava FOR 0 LINES

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * LambdaRequestContextFactory for Sam specific contexts.
 */
@Slf4j
public class SamLambdaRequestContextFactory implements LambdaRequestContextFactory {

    private final Map<String, ReentrantLock> lambdaCodeDirReentrantLockMap = new HashMap<>();
    private final String samLambdaLogFile;
    private final List<LambdaFunctionSpecs> lambdaFunctionSpecsList;

    @Value("${sam.lambda.context.initialization.timeout.seconds:60}")
    private int lambdaContextInitializedTimeoutSeconds;

    SamLambdaRequestContextFactory(final List<LambdaFunctionSpecs> lambdaFunctionSpecsList,
            final String samLambdaLogFile) {
        this.lambdaFunctionSpecsList = lambdaFunctionSpecsList;
        this.samLambdaLogFile = samLambdaLogFile;
        verifyLambdaSpecs(lambdaFunctionSpecsList);
        initializeLockPerCodeDir(lambdaFunctionSpecsList);
    }

    void verifyLambdaSpecs(final List<LambdaFunctionSpecs> lambdaFunctionSpecs) {
        final List<String> lambdaNames = new ArrayList<>();
        final List<String> repeatedLambdaNames = new ArrayList<>();
        lambdaFunctionSpecs.forEach(spec -> {
            if (lambdaNames.contains(spec.getFunctionName())) {
                repeatedLambdaNames.add(spec.getFunctionName());
            } else {
                lambdaNames.add(spec.getFunctionName());
            }
        });
        Preconditions.checkState(repeatedLambdaNames.isEmpty(),
                "Found LambdaFunctionSpecs with matching function names in test context for lambda names [%s]. "
                + "Lambda functions should have unique names.", repeatedLambdaNames);
    }

    void initializeLockPerCodeDir(final List<LambdaFunctionSpecs> lambdaFunctionSpecs) {
        lambdaFunctionSpecs.forEach(spec ->
                this.lambdaCodeDirReentrantLockMap.computeIfAbsent(
                        spec.getLambdaCodeDir(),
                        String -> new ReentrantLock(true))
        );
    }

    @Override
    @SneakyThrows
    public LambdaRequestContext newRequestContext(final String lambdaName) {
        log.info("Creating LambdaRequestContext for [{}]", lambdaName);
        final LambdaFunctionSpecs spec = getLambdaFunctionSpecs(lambdaName);
        final SamLambdaRequestContext lambdaRequestContext = SamLambdaRequestContext.builder()
                .threadLock(this.lambdaCodeDirReentrantLockMap.get(spec.getLambdaCodeDir()))
                .functionCodeDir(spec.getLambdaCodeDir())
                .functionName(spec.getFunctionName())
                .waitForLogTimeoutSeconds(getWaitForLogTimeout(spec))
                .lambdaLogFile(this.samLambdaLogFile)
                .build();
        lambdaRequestContext.initialize();
        return lambdaRequestContext;
    }

    private LambdaFunctionSpecs getLambdaFunctionSpecs(final String lambdaName) {
        final Optional<LambdaFunctionSpecs> lambdaFunctionSpecsOptional = this.lambdaFunctionSpecsList.stream()
                .filter(spec -> spec.getFunctionName().equals(lambdaName))
                .findFirst();
        if (lambdaFunctionSpecsOptional.isEmpty()) {
            throw new RuntimeException(String.format(
                    "Can't create LambdaRequestContext for lambda [%s] because no [%s] was found for it in the current test context. Available [%s]: %s",
                    lambdaName, LambdaFunctionSpecs.class.getSimpleName(), LambdaFunctionSpecs.class.getSimpleName(), this.lambdaFunctionSpecsList));
        }
        return lambdaFunctionSpecsOptional.get();
    }

    private int getWaitForLogTimeout(final LambdaFunctionSpecs spec) {
        if (spec.getTimeoutInSeconds() != null) {
            return Math.min(this.lambdaContextInitializedTimeoutSeconds, spec.getTimeoutInSeconds());
        }
        return this.lambdaContextInitializedTimeoutSeconds;
    }
}
