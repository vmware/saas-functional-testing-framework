/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.local.aws;

import software.amazon.awssdk.awscore.exception.AwsServiceException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Application listener that will provide some extra logging for the AWS service
 * faults that can happen during test bed provisioning.
 */
@Slf4j
public class ErrorLoggingSpringApplicationRunListener implements SpringApplicationRunListener {

    public ErrorLoggingSpringApplicationRunListener(final SpringApplication app, final String[] args) {
    }

    @Override
    public void failed(final ConfigurableApplicationContext context, final Throwable exception) {
        if (exception.getCause() instanceof AwsServiceException) {
            log.error("Caught AwsServiceException with details: {}", ((AwsServiceException)exception.getCause()).awsErrorDetails());
        }
    }
}
