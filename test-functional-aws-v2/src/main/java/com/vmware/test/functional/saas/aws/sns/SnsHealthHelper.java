/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.aws.sns;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * SNS Health Helper.
 */
public final class SnsHealthHelper {

    private SnsHealthHelper() {

    }

    /**
     * SNS Health Helper - verifying created queues.
     *
     * @param snsClient {@link SnsClient}.
     * @param topicName The SNS topic name.
     * @return {@code true} if the topic exists, else {@code false}.
     */
    public static boolean checkHealth(final SnsClient snsClient, final String topicName) {
        final List<String> listAllTopicArns = snsClient.listTopics().topics().stream().map(Topic::topicArn).collect(Collectors.toList());
        return listAllTopicArns.stream().anyMatch(t -> StringUtils.endsWith(t, ":" + topicName));
    }
}
