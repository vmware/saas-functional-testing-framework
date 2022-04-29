/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.aws.local.sns;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.aw.dpa.test.FunctionalTestExecutionSettings;
import com.aw.dpa.test.aws.local.AbstractAwsResourceCreator;
import com.aw.dpa.test.functional.aws.sns.SnsTopicsSpecs;

import lombok.extern.slf4j.Slf4j;

/**
 * Lifecycle control that creates provided {@link SnsTopicsSpecs SNS topics}
 * when started.
 */
@Slf4j
public class SnsResourceCreator extends AbstractAwsResourceCreator {

    private final SnsClient snsClient;

    public SnsResourceCreator(final SnsFactory snsFactory,
            final FunctionalTestExecutionSettings functionalTestExecutionSettings) {
        super(functionalTestExecutionSettings);
        this.snsClient = snsFactory.getObject();
    }

    @Override
    protected void doStart() {
        final List<SnsTopicsSpecs> snsTopicsSpecs = new ArrayList<>(getContext().getBeansOfType(SnsTopicsSpecs.class).values());
        if (!snsTopicsSpecs.isEmpty()) {
            initializeTopics(snsTopicsSpecs);
        }
    }

    private void initializeTopics(final List<SnsTopicsSpecs> snsTopicsSpecs) {
        if (log.isDebugEnabled()) {
            log.debug("Creating topics {}", snsTopicsSpecs.stream()
                    .map(SnsTopicsSpecs::getTopicsToCreate)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
        }

        final List<String> listAllTopicArns = this.snsClient.listTopics().topics().stream().map(Topic::topicArn).collect(Collectors.toList());
        snsTopicsSpecs.stream()
                .map(SnsTopicsSpecs::getTopicsToCreate)
                .flatMap(Collection::stream)
                .distinct()
                .filter(topicName -> listAllTopicArns.stream().noneMatch(t -> StringUtils.endsWith(t, ":" + topicName)))
                .forEach(this::createTopic);
    }

    private void createTopic(final String topicName) {
        final CreateTopicRequest createTopicRequest = CreateTopicRequest.builder()
                .name(topicName)
                .build();

        this.snsClient.createTopic(createTopicRequest);
        log.info("Topic [{}] created", topicName);
    }
}

