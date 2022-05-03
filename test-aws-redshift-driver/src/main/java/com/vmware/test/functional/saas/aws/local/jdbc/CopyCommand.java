/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.aws.local.jdbc;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements {@code COPY} commands.
 */
@Data
@AllArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "appears a false positive")
@Slf4j
class CopyCommand implements ExecutionCommand<Boolean> {

    private static final String GET_COLUMNS_QUERY_COLUMN_LABEL = "COLUMN_NAME";
    private static final String JSON_FORMAT_OPTION_AUTO = "auto";
    private static final String INSERT_INTO_FORMAT = "INSERT INTO %s (%s) VALUES (%s)";

    private static Pattern pattern = Pattern.compile("COPY\\s+(?<tableName>.+?)\\s+FROM\\s+'(?<datasource>.+?)'(?<options>.*)", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOptionsFormat = Pattern.compile("(.+?)JSON\\s+(AS\\s)?'(?<jsonOption>.+?)'", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOptionsCompression = Pattern.compile("(.+?)(?<compression>GZIP)", Pattern.CASE_INSENSITIVE);
    private static Pattern patternOptionsManifest = Pattern.compile("(.+?)(?<manifest>MANIFEST)", Pattern.CASE_INSENSITIVE);

    private final ExecutionCommandParser.ExecutionCommandContext context;
    private final String tableName;
    private final String dataSource;
    private final boolean jsonFormatRequired;
    private final boolean gzipCompressionRequired;
    private final boolean manifestRequired;

    static Optional<CopyCommand> parse(final String sql, final ExecutionCommandParser.ExecutionCommandContext context) {
        final Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) {
            return Optional.empty();
        }
        final String options = matcher.group("options");

        final String tableName = matcher.group("tableName");
        final String dataSource = matcher.group("datasource");

        return Optional.of(new CopyCommand(
                context,
                tableName,
                dataSource,
                patternOptionsFormat.matcher(options).find(),
                patternOptionsCompression.matcher(options).find(),
                patternOptionsManifest.matcher(options).find()
        ));
    }

    @Override
    public PreparedStatement prepareStatement(final Connection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean execute(final PreparedStatement preparedStatement) {
        throw new UnsupportedOperationException();
    }

    @SneakyThrows
    @Override
    public Boolean execute(final Statement statementUnused) {
        final List<String> columns = new LinkedList<>();
        try (ResultSet rs = this.context.getConnection().getMetaData().getColumns(null, null, this.tableName, "%")) {
            while (rs.next()) {
                columns.add(rs.getString(GET_COLUMNS_QUERY_COLUMN_LABEL));
            }
        }
        final PreparedStatement insertTestDataPrepStmt = this.context.getConnection().prepareStatement(String.format(
                INSERT_INTO_FORMAT,
                this.tableName,
                String.join(",", columns),
                columns.stream()
                        .map(v -> "?")
                        .collect(Collectors.joining(","))));
        try (insertTestDataPrepStmt) {
            // S3 data binding
            final S3Client client = S3Client.builder().credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials
                            .create(this.context.getDefaultAwsAccessKey(), this.context.getDefaultAwsSecretAccessKey())))
                    .endpointOverride(URI.create(this.context.getS3EndpointUrl()))
                    .region(Region.of(this.context.getDefaultRegion()))
                    .build();
            if (this.manifestRequired) {
                final String manifestStr = client.getObjectAsBytes(buildGetS3ManifestRequest()).asUtf8String();
                final ObjectMapper mapper = new ObjectMapper();
                final Manifest manifest = mapper.readValue(manifestStr, new TypeReference<Manifest>() { });
                manifest.getEntries().stream()
                        .map(ManifestEntry::getUrl)
                        .map(URI::create)
                        .map(this::buildGetS3ManifestEntryRequest)
                        .map(request -> getS3ObjectInputStream(client, request))
                        .forEach(is -> bindDataValuesToColumns(insertTestDataPrepStmt, columns, is));
            } else {
                final URI dataSourceUri = URI.create(this.dataSource);
                final InputStream objectIs = new SequenceInputStream(Collections.enumeration(
                        buildGetS3ObjectRequestsByPrefix(client, dataSourceUri.getHost(), getS3FileNamePart(dataSourceUri))
                                .stream()
                                .map(request -> getS3ObjectInputStream(client, request))
                                .collect(Collectors.toList())));
                bindDataValuesToColumns(insertTestDataPrepStmt, columns, objectIs);
            }
            insertTestDataPrepStmt.executeBatch();
        }
        return false;
    }

    @SneakyThrows
    private void bindDataValuesToColumns(final PreparedStatement insertTestDataPrepStmt, final List<String> columns, final InputStream dataValuesInputStream) {
        if (this.jsonFormatRequired) {
            final ObjectMapper mapper = new ObjectMapper();
            try (Reader src = new InputStreamReader(dataValuesInputStream, StandardCharsets.UTF_8);
                    JsonParser parser = mapper.createParser(src)) {
                final MappingIterator<Map<String, Object>> rows = mapper
                        .readValues(parser, new TypeReference<Map<String, Object>>() { });
                while (rows.hasNext()) {
                    final Map<String, Object> row = rows.nextValue();
                    // shred the json attributes to columns
                    IntStream.range(0, columns.size())
                            .forEach(i -> shredJsonAttributesToColumns(
                                    insertTestDataPrepStmt, i + 1, columns.get(i), row));
                    insertTestDataPrepStmt.addBatch();
                }
            }
        } else {
            throw new UnsupportedOperationException("only queries that define explicitly json format are supported");
        }
    }

    @SneakyThrows
    private void shredJsonAttributesToColumns(
            final PreparedStatement copyRowPrepStmt,
            final int columnIndex,
            final String columnName,
            final Map<String, Object> row) {
        copyRowPrepStmt.setObject(columnIndex, row.get(columnName));
    }

    private GetObjectRequest buildGetS3ManifestRequest() {
        final URI uri = URI.create(this.dataSource);
        return GetObjectRequest.builder()
                .bucket(uri.getHost())
                .key(getS3FileNamePart(uri))
                .build();
    }

    private GetObjectRequest buildGetS3ManifestEntryRequest(final URI uri) {
        return GetObjectRequest.builder()
                .bucket(uri.getHost())
                .key(getS3FileNamePart(uri))
                .build();
    }

    private List<GetObjectRequest> buildGetS3ObjectRequestsByPrefix(final S3Client client,
            final String bucketName, final String fileNamePrefix) {
        final ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        return client.listObjectsV2(listObjectsRequest).contents()
                .stream()
                .map(S3Object::key)
                .peek(log::debug)
                .filter(e -> e.startsWith(fileNamePrefix))
                .map(k -> GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(k)
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private InputStream getS3ObjectInputStream(final S3Client client, final GetObjectRequest request) {
        InputStream objectIs = client.getObject(request, ResponseTransformer.toInputStream());
        if (this.gzipCompressionRequired) {
            objectIs = new GZIPInputStream(objectIs);
        }
        return objectIs;
    }

    private String getS3FileNamePart(final URI uri) {
        // remove leading slash
        return uri.getPath().substring(1);
    }

    /**
     * Manifest format.
     */
    @Data
    static class Manifest {
        private List<ManifestEntry> entries;
    }

    /**
     * Manifest entry.
     */
    @Data
    static class ManifestEntry {
        private String url;
        private boolean mandatory;
    }
}
