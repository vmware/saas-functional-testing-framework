/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */
package com.vmware.test.functional.saas.aws.kinesis;

import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

import com.vmware.test.functional.saas.common.DataWrapper;

/**
 * AWS Kinesis stream reader which reads and filters the records based on a filtering function.
 *
 * @param <D> The type of the data stored in the {@link KinesisClientRecord}'s data field.
 */
public interface FilteringKinesisStreamReader<D> extends KinesisStreamReader {

    /**
     * Reads all new records available in the stream and returns a filtered result list.
     *
     * @param filterCondition Filtering function.
     *
     * @return List of {@link KinesisClientRecord}
     */
    List<KinesisClientRecord> getRecords(Predicate<DataWrapper<KinesisClientRecord, D>> filterCondition);

    /**
     * This method is the same as {@link FilteringKinesisStreamReaderImpl#pollForRecordsUntil(Duration, Duration, Predicate, Predicate)},
     * but the {@code pollInterval} and {@code pollDuration} arguments have default values.
     *
     * @param filterCondition Filtering function.
     * @param returnCondition Return Condition function.
     *
     * @return List of {@link KinesisClientRecord}
     */
    List<KinesisClientRecord> pollForRecordsUntil(Predicate<DataWrapper<KinesisClientRecord, D>> filterCondition,
            Predicate<List<DataWrapper<KinesisClientRecord, D>>> returnCondition);

    /**
     * Continuously reads new records from the stream, filtering them using the filter function, and stores them in a temporary result list.
     * After each iteration of reading new records it tests the temporary result list with the returnCondition and returns it if the condition
     * was satisfied.
     *
     * @param pollInterval Interval between each read for records.
     * @param pollTimeout Poll timeout.
     * @param filterCondition Filtering function.
     * @param returnCondition Return Condition function.
     *
     * @return List of {@link KinesisClientRecord}
     */
    List<KinesisClientRecord> pollForRecordsUntil(Duration pollInterval,
            Duration pollTimeout,
            Predicate<DataWrapper<KinesisClientRecord, D>> filterCondition,
            Predicate<List<DataWrapper<KinesisClientRecord, D>>> returnCondition);
}
