/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.util.HadoopOutputFile;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

/**
 * Implements {@code UNLOAD} command.
 */
@AllArgsConstructor
class UnloadCommand implements ExecutionCommand<Boolean> {

    static Pattern pattern = Pattern.compile("UNLOAD\\s+\\('(.+?)'\\)\\s+TO\\s+'(.+?)'(.*)", Pattern.CASE_INSENSITIVE);
    private static final String PART_00 = "0000_part_00";

    private final ExecutionCommandParser.ExecutionCommandContext context;
    private final String query;
    private final String s3Bucket;
    private final boolean parquetFormatRequired;
    private final boolean manifestRequired;

    static Optional<UnloadCommand> parse(final String sql, final ExecutionCommandParser.ExecutionCommandContext context) {
        final Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) {
            return Optional.empty();
        }

        final String queryPart = matcher.group(1);
        final String s3Part = matcher.group(2);
        final String theRest = matcher.group(3);

        return Optional.of(new UnloadCommand(
                context,
                queryPart,
                s3Part,
                theRest.contains("PARQUET"),
                theRest.contains("MANIFEST")));
    }

    @SneakyThrows
    @Override
    public PreparedStatement prepareStatement(final Connection connection) {
        return connection.prepareStatement(this.query);
    }

    @SneakyThrows
    @Override
    public Boolean execute(final Statement statement) {
        try (ResultSet resultSet = statement.executeQuery(this.query)) {
            return doWithResultSet(resultSet);
        }
    }

    @SneakyThrows
    @Override
    public Boolean execute(final PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return doWithResultSet(resultSet);
        }
    }

    private boolean doWithResultSet(final ResultSet resultSet)
            throws URISyntaxException, IOException, SQLException {
        final URI url = new URI(this.s3Bucket);
        final String bucketName = url.getHost();
        // remove leading slash
        final String namePrefix = url.getPath().substring(1);
        final String manifestName = namePrefix + "manifest";

        final AwsCredentials awsCredentials = AwsBasicCredentials
                .create(this.context.getDefaultAwsAccessKey(), this.context.getDefaultAwsSecretAccessKey());

        final S3Client client = S3Client.builder().credentialsProvider(
                StaticCredentialsProvider.create(awsCredentials))
                .endpointOverride(URI.create(this.context.getS3EndpointUrl()))
                .region(Region.of(this.context.getDefaultRegion()))
                .build();

        if (this.parquetFormatRequired) {
            putParquetObjectFromResultSet(bucketName, namePrefix, client, resultSet);
        } else {
            throw new UnsupportedOperationException("only queries that define explicitly parquet format are supported");
        }

        if (this.manifestRequired) {
            final String manifestContent = String.format("{\"entries\":[{\"url\":\"%s%s\"}]}", this.s3Bucket, PART_00);
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(manifestName)
                            .build(),
                    RequestBody.fromString(manifestContent));
        }
        return true;
    }

    private static Schema schemaFromSqlMetadata(final ResultSetMetaData metaData)
            throws SQLException {
        final SchemaBuilder.FieldAssembler<Schema> schemaFieldAssembler = SchemaBuilder
                .record("GenericObject").fields();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            // type
            SqlTransformer.extractSchema(metaData.getColumnType(i), metaData.getColumnName(i), schemaFieldAssembler);
        }
        return schemaFieldAssembler.endRecord();
    }

    private void putParquetObjectFromResultSet(final String bucketName,
            final String namePrefix,
            final S3Client client,
            final ResultSet resultSet) throws IOException, SQLException {
        final String objectName = namePrefix + PART_00;
        final Path tmpDir = Files.createTempDirectory(null);
        final Path tempFile = tmpDir.resolve(objectName);
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            final Schema schema = schemaFromSqlMetadata(metaData);
            try (ParquetWriter<GenericRecord> writer =
                         buildWriter(tempFile.toAbsolutePath().toString(), schema)) {
                while (resultSet.next()) {
                    final GenericRecordBuilder genericRecordBuilder = new GenericRecordBuilder(schema);
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        SqlTransformer.extractResult(
                                metaData.getColumnType(i),
                                metaData.getColumnName(i), resultSet,
                                genericRecordBuilder);
                    }
                    writer.write(genericRecordBuilder.build());
                }
            }
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectName)
                            .build(),
                    tempFile);
        } finally {
            tmpDir.toFile().deleteOnExit();
            tempFile.toFile().deleteOnExit();
        }
    }

    private ParquetWriter<GenericRecord> buildWriter(final String outputFilePath,
            final Schema schema) throws IOException {
        final Configuration conf = new Configuration();
        return AvroParquetWriter.<GenericRecord>builder(
                HadoopOutputFile.fromPath(new org.apache.hadoop.fs.Path(outputFilePath), conf))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
    }
}
