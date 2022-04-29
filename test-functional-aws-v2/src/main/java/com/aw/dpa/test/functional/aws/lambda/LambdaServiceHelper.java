/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.lambda;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.AbstractMessage;
import com.nimbusds.jose.util.StandardCharset;

import lombok.extern.slf4j.Slf4j;

/**
 * Lambda Service Helper.
 */
@Slf4j
public final class LambdaServiceHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LambdaServiceHelper() {

    }

    /**
     * Verifies that result log from Lambda invocation matches an expected message.
     *
     * @param expectedResultRegEx Expected message to match the result log.
     * @param lambdaResultString Result log from Lambda invocation.
     * @return {@code true} if the given string is contained within the result log, {@code false} otherwise
     */
    public static boolean matchesExpectedResultMessage(final String expectedResultRegEx, final String lambdaResultString) {
        final Pattern pattern = Pattern.compile("(.*)" + expectedResultRegEx + "(.*)", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(lambdaResultString);
        final boolean matches = matcher.matches();
        log.debug("Lambda Result: [{}], ResultRegEx: [{}] match found ? [{}]", lambdaResultString, expectedResultRegEx, matches);
        return matches;
    }

    /**
     * Transforms a String record to SdkBytes. This is used as payload for Lambda function invocation.
     *
     * @param record String record.
     * @return SdkBytes
     */
    public static SdkBytes mapStringRecord(final String record) {
        return SdkBytes.fromString(record, StandardCharset.UTF_8);
    }

    /**
     * Transforms an AbstractMessage record to ByteBuffer. This is used as "data", when preparing a payload for a Lambda function.
     *
     * @param record AbstractMessage record.
     * @return ByteBuffer
     */
    public static ByteBuffer mapAbstractMessageRecord(final AbstractMessage record) {
        return ByteBuffer.wrap(record.toByteArray());
    }

     /**
     * Transforms a generic record to ByteBuffer. This is used as "data", when preparing a payload for a Lambda function.
     *
     * @param record generic record.
     * @param <T> generic type
     * @return ByteBuffer
     */
    public static <T> ByteBuffer mapGenericRecord(final T record) throws JsonProcessingException {
        final String recordAsString = OBJECT_MAPPER.writeValueAsString(record);
        return ByteBuffer.wrap(recordAsString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extract result payload from Lambda invocation response.
     *
     * @param responseData Response from Lambda invocation.
     * @return SdkBytes
     */
    public static SdkBytes extractResultPayload(final InvokeResponse responseData) {
        log.debug("Invoke lambda response code: {}", responseData.statusCode().toString());
        final SdkBytes lambdaResultPayload = responseData.payload();
        log.info("Lambda invocation result payload: {}", lambdaResultPayload);
        return lambdaResultPayload;
    }

    /**
     * Extract lambda log result from Lambda invocation response.
     *
     * @param responseData Response from Lambda invocation.
     * @return String
     */
    public static String extractLogResult(final InvokeResponse responseData) {
        log.debug("Invoke lambda response code: {}", responseData.statusCode().toString());
        final String lambdaLogResult = responseData.logResult();
        log.debug("Lambda invocation result log: {}", lambdaLogResult);
        return lambdaLogResult;
    }

    /**
     * Transforms a SdkBytes record to String.
     *
     * @param payload SdkBytes payload.
     * @return String
     */
    public static String extractSdkBytesPayloadToString(final SdkBytes payload) {
        return payload.asString(StandardCharsets.UTF_8);
    }

}
