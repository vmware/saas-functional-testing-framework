/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.awaitility.Awaitility;

import com.aw.dpa.test.functional.common.DataWrapper;
import com.aw.dpa.test.functional.common.ThrowingFunction;

/**
 * Kinesis Stream Reader that reads and transforms the records using the {@code dataTransformer}, filters them with the given {@link Predicate}
 * and returns the filtered list of {@link KinesisClientRecord}s.
 * Reading from a stream can be done as a one-off operation, retrieving all currently available records with, or iteratively using a polling mechanism
 * until a returnCondition is met.
 * Any filtered records are discarded and won't appear in subsequent reads.
 */
public class FilteringKinesisStreamReaderImpl<T> extends KinesisStreamReaderImpl implements FilteringKinesisStreamReader<T> {

    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);

    private final ThrowingFunction<ByteBuffer, T> dataTransformer;
    public FilteringKinesisStreamReaderImpl(final KinesisClient kinesisClient,
            final String streamName,
            final ThrowingFunction<ByteBuffer, T> dataTransformer) {
        super(kinesisClient, streamName);
        this.dataTransformer = dataTransformer;
    }

    @Override
    public List<KinesisClientRecord> getRecords(final Predicate<DataWrapper<KinesisClientRecord, T>> filterCondition) {
        return getRecordsHelper(filterCondition)
                .map(DataWrapper::getData)
                .collect(Collectors.toList());
    }

    @Override
    public List<KinesisClientRecord> pollForRecordsUntil(final Predicate<DataWrapper<KinesisClientRecord, T>> filterCondition,
            final Predicate<List<DataWrapper<KinesisClientRecord, T>>> returnCondition) {
        return pollForRecordsUntil(DEFAULT_POLL_INTERVAL, DEFAULT_TIMEOUT, filterCondition, returnCondition);
    }

    @Override
    public List<KinesisClientRecord> pollForRecordsUntil(final Duration pollInterval,
            final Duration pollTimeout,
            final Predicate<DataWrapper<KinesisClientRecord, T>> filterCondition,
            final Predicate<List<DataWrapper<KinesisClientRecord, T>>> returnCondition) {
        final List<DataWrapper<KinesisClientRecord, T>> tempResult = new ArrayList<>();
        Awaitility.await(String.format("Polling for records from stream [%s]", this.getStreamName()))
                .pollInterval(pollInterval)
                .timeout(pollTimeout)
                .until(() -> {
                    tempResult.addAll(getRecordsHelper(filterCondition).collect(Collectors.toList()));
                    return returnCondition.test(tempResult);
                });
        return tempResult.stream().map(DataWrapper::getData).collect(Collectors.toList());
    }

    private Stream<DataWrapper<KinesisClientRecord, T>> getRecordsHelper(final Predicate<DataWrapper<KinesisClientRecord, T>> filterCondition) {
        return getRecords().stream()
                .map(r -> new DataWrapper<>(d -> this.dataTransformer.apply(r.data()), r))
                .filter(filterCondition);
    }
}
