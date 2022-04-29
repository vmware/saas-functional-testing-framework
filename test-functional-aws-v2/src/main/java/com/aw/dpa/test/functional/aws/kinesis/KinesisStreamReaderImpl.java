/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */

package com.aw.dpa.test.functional.aws.kinesis;

import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.kinesis.retrieval.AggregatorUtil;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link KinesisStreamReader}.
 *
 * When instantiating {@link KinesisStreamReaderImpl}, shardIteratorList initializes our reading position in the stream.
 * Current implementation gets shardIteratorList of type LATEST which allows to read records right after this.
 *
 * Call to getRecords() should be made as many times as necessary to get the expected result or or until we are not seeing any more records
 *
 * <p>Note: This {@link KinesisStreamReader} implementation will not return correct records list if split shard, merge shard or update shard operation
 * is done during the test (using this) lifecycle. Assuming this is used only for testing purposes, such use case is not valid at this point.</p>
 */
@Slf4j
public class KinesisStreamReaderImpl implements KinesisStreamReader {

    private final KinesisClient kinesisClient;
    @Getter
    private final String streamName;
    private List<String> shardIteratorList = new ArrayList<>();

    public KinesisStreamReaderImpl(final KinesisClient kinesisClient, final String streamName) {
        Preconditions.checkNotNull(kinesisClient, "Specified Kinesis client cannot be null");
        Preconditions.checkState(StringUtils.isNotBlank(streamName), "Stream reference cannot be null/empty");

        this.kinesisClient = kinesisClient;
        this.streamName = streamName;
        getShardIteratorList(streamName);
    }

    /**
     * Gets a list of AWS Kinesis Stream shard iterators.
     *
     * @param stream Stream name
     */
    private void getShardIteratorList(final String stream) {
        final DescribeStreamResponse describeStreamResponse = this.kinesisClient.describeStream(
                DescribeStreamRequest
                        .builder()
                        .streamName(stream)
                        .build());

        final List<Shard> shardsList = describeStreamResponse.streamDescription().shards();

        if (CollectionUtils.isEmpty(shardsList)) {
            log.debug("Shard list for stream [{}] is empty.", stream);
            return;
        }

        for (Shard shard : shardsList) {
            final GetShardIteratorResponse shardIteratorResponse = this.kinesisClient.getShardIterator(GetShardIteratorRequest.builder()
                    .shardId(shard.shardId())
                    .streamName(stream)
                    .shardIteratorType(ShardIteratorType.LATEST)
                    .build());
            if (shardIteratorResponse.shardIterator() != null) {
                this.shardIteratorList.add(shardIteratorResponse.shardIterator());
            }
        }
    }

    /**
     * Reads data records from AWS Kinesis Stream sharditeratorList.
     * Gets records AWS operation is called with shardIteratorType=LATEST which
     * read from the latest messages - current message that just came into the stream
     * and all the incoming messages from that time onwards.
     * Method deaggregates the retrieved Kinesis records list into a
     * list of KPL user records.
     */
    @Override
    public List<KinesisClientRecord> getRecords() {
        final List<KinesisClientRecord> recordList = new ArrayList<>();
        final List<String> nextShardIteratorList = new ArrayList<>();

        for (String s : this.shardIteratorList) {
            final GetRecordsResponse getRecordsResponse = this.kinesisClient.getRecords(GetRecordsRequest.builder()
                    .shardIterator(s)
                    .build());
            log.debug("Found [{}] records using shard iterator [{}]", getRecordsResponse.records().size(), s);

            final List<KinesisClientRecord> kinesisClientRecords = getRecordsResponse.records().stream()
                    .map(record ->
                            KinesisClientRecord.builder()
                                .sequenceNumber(record.sequenceNumber())
                                .approximateArrivalTimestamp(record.approximateArrivalTimestamp())
                                .data(ByteBuffer.wrap(record.data().asByteArray()))
                                .partitionKey(record.partitionKey())
                                .encryptionType(record.encryptionType())
                                .build())
                    .collect(Collectors.toList());
            log.debug("Converted [{}] Records to [{}] Kinesis client records.", getRecordsResponse.records().size(), kinesisClientRecords.size());

            recordList.addAll(new AggregatorUtil().deaggregate(kinesisClientRecords));
            nextShardIteratorList.add(getRecordsResponse.nextShardIterator());
        }
        this.shardIteratorList = nextShardIteratorList;

        if (recordList.isEmpty()) {
            log.debug("Found 0 records in [{}] stream", this.streamName);
            return Collections.emptyList();
        }
        return recordList;
    }
}
