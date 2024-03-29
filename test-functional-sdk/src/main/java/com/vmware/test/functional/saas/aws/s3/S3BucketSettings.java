/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.s3;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

/**
 * Local s3Bucket configuration properties.
 */
@Builder
@Data
public class S3BucketSettings {

    private final String name;

    @Value("#{T(com.vmware.test.functional.saas.aws.s3.S3BucketSettings).lookupBaseUrl(s3Endpoint)}")
    private String endpoint;

    /**
     * Get s3 bucket url.
     *
     * @return the s3 bucket url
     */
    public String getUrl() {
        Preconditions.checkNotNull(this.endpoint, "Endpoint may not be null");
        Preconditions.checkNotNull(this.name, "Name may not be null");
        return StringUtils.appendIfMissing(this.endpoint, "/") + this.name;
    }

    /**
     * Resolve the configured s3 base url form the environment.
     *
     * @param s3Endpoint s3 endpoint
     * @return configured base url
     */
    public static String lookupBaseUrl(final ServiceEndpoint s3Endpoint) {
        return s3Endpoint.getEndpoint();
    }
}
