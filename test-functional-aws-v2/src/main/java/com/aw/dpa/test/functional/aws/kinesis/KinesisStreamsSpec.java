/*
 * Copyright 2020 VMware, Inc.
 * All rights reserved.
 */
package com.aw.dpa.test.functional.aws.kinesis;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for local kinesis streams creation.
 */
@Builder
@Data
public class KinesisStreamsSpec {

    private List<String> streamsToCreate;
}
