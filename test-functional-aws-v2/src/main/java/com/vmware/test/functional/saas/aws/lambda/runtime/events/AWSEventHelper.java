/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.lambda.runtime.events;

import software.amazon.awssdk.core.SdkBytes;

import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper for an AWS Event. To be used for the invocation of Lambda function(s).
 */
@Slf4j
public final class AWSEventHelper {

    private AWSEventHelper() {
    }

    /**
     * Method for converting AWS Event to SdkBytes.
     *
     * @param event event
     * @param <T>   type of the event
     * @return      {@link SdkBytes}
     */
    public static <T> SdkBytes eventToSdkBytes(final T event) {
        final ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .addMixIn(event.getClass(), AbstractEventMixin.class)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        final String eventJson;
        try {
            eventJson = mapper.writeValueAsString(event);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
        log.debug("Prepared Event payload: [{}]", eventJson);
        return SdkBytes.fromString(eventJson, Charset.defaultCharset());
    }
}
