/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.aws.lambda;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link LambdaService}.
 */
@Slf4j
public class LambdaServiceImpl<R, T> implements LambdaService<R, T>, ApplicationContextAware, InitializingBean {

    @NonNull
    private final LambdaClient lambdaClient;
    @NonNull
    private final Function<T, SdkBytes> recordMapper;
    @NonNull
    private final Function<InvokeResponse, R> invokeResponseMapper;
    private LambdaLogExtractor lambdaLogExtractor;
    private LambdaRequestContextFactory requestContextFactory;

    @Setter
    private ApplicationContext applicationContext;
    private final ExecutorService executor = Executors.newWorkStealingPool();

    @Builder
    public LambdaServiceImpl(@NonNull final LambdaClient lambdaClient,
            @NonNull final Function<T, SdkBytes> recordMapper,
            @NonNull final Function<InvokeResponse, R> invokeResponseMapper) {

        this.lambdaClient = lambdaClient;
        this.recordMapper = recordMapper;
        this.invokeResponseMapper = invokeResponseMapper;
    }

    @Override
    public void afterPropertiesSet() {
        this.requestContextFactory = this.applicationContext.getBean(LambdaRequestContextFactory.class);
        this.lambdaLogExtractor = this.applicationContext.getBean(LambdaLogExtractor.class);
    }

    @Override
    @SneakyThrows
    public R invoke(final String functionName, final T payload) {
        final InvokeResponse invokeResponse = invokeLambdaFunction(functionName, payload);
        return this.invokeResponseMapper.apply(invokeResponse);
    }

    @Override
    @SneakyThrows
    public void invoke(final String functionName, final T payload, final Consumer<R> responseConsumer) {
        final InvokeResponse invokeResponse = invokeLambdaFunction(functionName, payload);
        if (responseConsumer != null) {
            responseConsumer.accept(this.invokeResponseMapper.apply(invokeResponse));
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "https://github.com/spotbugs/spotbugs/issues/756")
    @SneakyThrows
    private InvokeResponse invokeLambdaFunction(final String functionName, final T payload) {
        final InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .payload(this.recordMapper.apply(payload))
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .build();

        final Future<InvokeResponse> invokeResponse;
        try (LambdaRequestContext requestContext = this.requestContextFactory.newRequestContext(functionName)) {
            log.info("Sending InvokeRequest [{}] using LambdaClient [{}] within LambdaRequestContext [{}].",
                    invokeRequest,
                    System.identityHashCode(this.lambdaClient),
                    requestContext.getRequestId());
            // adding lambda log result to the invokeResponse as sam does not support it
            invokeResponse = this.executor.submit(() -> this.lambdaClient.invoke(invokeRequest).toBuilder()
                        .logResult(this.lambdaLogExtractor.getLambdaLogsForRequestContext(requestContext))
                        .build());
        }
        return invokeResponse.get();
    }
}
