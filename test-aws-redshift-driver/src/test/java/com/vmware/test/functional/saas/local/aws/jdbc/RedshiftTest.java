/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.test.functional.saas.AbstractFunctionalTests;
import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.Service;
import com.vmware.test.functional.saas.ServiceDependencies;
import com.vmware.test.functional.saas.local.aws.AwsSettings;
import com.vmware.test.functional.saas.local.aws.redshift.RedshiftDataSourceConfig;
import com.vmware.test.functional.saas.local.aws.redshift.RedshiftDataSourceFactory;
import com.vmware.test.functional.saas.local.aws.redshift.RedshiftDbSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSettings;
import com.vmware.test.functional.saas.aws.s3.S3BucketSpecs;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests for redshift {@code UNLOAD} and {@code COPY} SQL commands.
 */
@ContextHierarchy(
        @ContextConfiguration(classes = RedshiftTest.TestContext.class))
@Slf4j
public class RedshiftTest extends AbstractFunctionalTests {

    private static final String CREATE_TABLE_TMP_S3_COPY = "CREATE TABLE IF NOT EXISTS tmp_s3_copy (id VARCHAR(256) NOT NULL, magic_num INTEGER)";
    private static final String QUERY_TEST_DATA_CMD = "SELECT id, magic_num FROM tmp_s3_copy WHERE id=? ORDER BY magic_num";
    private static final String CREATE_TABLE_TMP_S3_UNLOAD = "CREATE TABLE IF NOT EXISTS tmp_s3_unload (test_id VARCHAR(256) NOT NULL)";
    private static final String INSERT_INTO_TMP_S3_UNLOAD = "INSERT INTO tmp_s3_unload VALUES(?)";
    private static final String CREATE_TMP_TBL_VARCHAR_MAX = "CREATE TABLE tmp_tbl_varchar_max (test_guid VARCHAR(MAX) NOT NULL,"
            + "_test_guid VARCHAR(MAX), test_id INTEGER)";
    private static final String ALTER_TMP_TBL_VARCHAR_MAX = "ALTER TABLE tmp_tbl_varchar_max ADD newColumn varchar(max)";
    private static final String SELECT_TMP_TBL_VARCHAR_MAX = "SELECT * FROM pg_catalog.pg_tables WHERE tablename='tmp_tbl_varchar_max'";
    private static final String CREATE_TMP_TBL_DISTSTYLE = "CREATE TABLE tmp_tbl_diststyle (test_guid VARCHAR(MAX) NOT NULL,"
            + "_test_guid VARCHAR(MAX), test_id INTEGER)"
            + " diststyle key"
            + " distkey (_airwatch_device_guid)";
    private static final String SELECT_TMP_TBL_DISTSTYLE = "SELECT * FROM pg_catalog.pg_tables WHERE tablename='tmp_tbl_diststyle'";
    private static final String CREATE_TMP_TABLE = "CREATE TABLE IF NOT EXISTS %s (device_id VARCHAR(MAX) NOT NULL, name varchar(MAX) not null)";
    private static final String REDSHIFT_SQL_CREATE_TABLE_TEMP_DUPLICATE_CLEANUP = "SELECT distinct device_id, "
            + "LISTAGG( name, ',' ) WITHIN GROUP (ORDER BY name) OVER(PARTITION BY device_id) FROM %s order by device_id";
    private static final String POSTGRES_SQL_CREATE_TABLE_TEMP_DUPLICATE_CLEANUP = "SELECT distinct device_id, "
            + "STRING_AGG( name, ',') OVER(PARTITION BY device_id) FROM %s group by device_id,name order by device_id ";
    private static final String ENTITY_TABLE_NAME = "airwatch_device";

    public static class RedshiftToPostgresQueriesMap extends AbstractMap<String, String> {

        @Override
        public Set<Entry<String, String>> entrySet() {
            return Set.of(new SimpleImmutableEntry<>(
                    String.format(REDSHIFT_SQL_CREATE_TABLE_TEMP_DUPLICATE_CLEANUP, ENTITY_TABLE_NAME),
                    String.format(POSTGRES_SQL_CREATE_TABLE_TEMP_DUPLICATE_CLEANUP, ENTITY_TABLE_NAME)));
        }
    }

    @Configuration
    @PropertySource("classpath:redshift-driver.properties")
    @ServiceDependencies({ Service.REDSHIFT, Service.S3 })
    public static class TestContext {

        @Bean
        RedshiftDbSettings redshiftDbSettings() {
            return RedshiftDbSettings.builder()
                    .dbName("testdbname")
                    .build();
        }

        @Bean
        @ConfigurationProperties(prefix = "custom-redshift-db-settings")
        @Lazy
        RedshiftDataSourceConfig redshiftDataSourceConfig(@Lazy final ServiceEndpoint s3Endpoint,
                final AwsSettings awsSettings) {
            return RedshiftDataSourceConfig.builder()
                    .additionalDatasourceConfig(Map.of(
                            TestRedshiftDriver.S3_ENDPOINT_URL, s3Endpoint.getEndpoint(),
                            TestRedshiftDriver.DEFAULT_AWS_ACCESS_KEY, awsSettings.getTestAccessKey(),
                            TestRedshiftDriver.DEFAULT_AWS_SECRET_ACCESS_KEY, awsSettings.getTestSecretKey(),
                            TestRedshiftDriver.DEFAULT_AWS_REGION, awsSettings.getTestDefaultRegion(),
                            TestRedshiftDriver.QUERY_MAP_CLASS, RedshiftTest.RedshiftToPostgresQueriesMap.class.getName(),
                            TestRedshiftDriver.SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS, "query_group"))
                    .build();
        }

        @Bean
        @Lazy
        RedshiftDataSourceFactory redshiftDataSourceFactory(@Lazy final ServiceEndpoint redshiftEndpoint,
                final RedshiftDbSettings redshiftDbSettings,
                final RedshiftDataSourceConfig redshiftDataSourceConfig) {
            return new RedshiftDataSourceFactory(redshiftEndpoint, redshiftDbSettings, redshiftDataSourceConfig);
        }

        @Bean
        @Lazy
        JdbcTemplate redshiftJbcTemplate(@Lazy final RedshiftDataSourceFactory redshiftDataSourceFactory) {
            return new JdbcTemplate(redshiftDataSourceFactory.getObject());
        }

        @Bean
        @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
        S3BucketSettings s3BucketSettings() {
            return S3BucketSettings.builder()
                    .name("test1")
                    .build();
        }

        @Bean
        S3BucketSpecs s3BucketSpecs(final S3BucketSettings s3BucketSettings) {
            return S3BucketSpecs.builder()
                    .bucket(s3BucketSettings)
                    .build();
        }
    }

    @Autowired
    private S3BucketSettings s3BucketSettings;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private JdbcTemplate redshiftJdbcTemplate;

    @BeforeClass(alwaysRun = true)
    public void createTable() {
        // Create test table in redshift
        this.redshiftJdbcTemplate.execute(CREATE_TABLE_TMP_S3_UNLOAD);
        this.redshiftJdbcTemplate.execute(CREATE_TABLE_TMP_S3_COPY);
        this.redshiftJdbcTemplate.execute(String.format(CREATE_TMP_TABLE, ENTITY_TABLE_NAME));
    }

    @Test
    public void localRedshiftVarcharMaxDataTypeParsed() {
        final PreparedStatementCreator psc = new PreparedStatementCreatorFactory(CREATE_TMP_TBL_VARCHAR_MAX)
                .newPreparedStatementCreator(Collections.emptyList());
        // create table using ps
        this.redshiftJdbcTemplate.execute(psc, PreparedStatement::execute);
        // alter created table using statement
        this.redshiftJdbcTemplate.execute(ALTER_TMP_TBL_VARCHAR_MAX);

        final PreparedStatementCreator selectTblPsc = new PreparedStatementCreatorFactory(SELECT_TMP_TBL_VARCHAR_MAX)
                .newPreparedStatementCreator(Collections.emptyList());
        final Boolean result = this.redshiftJdbcTemplate.execute(selectTblPsc, PreparedStatement::execute);
        assertThat(result, notNullValue());
        assertThat("Table does not exist but was expected to.", result);
    }

    @Test
    public void createTableWithDiststyleAndDistkeyIsSuccessful() {
        final PreparedStatementCreator psc = new PreparedStatementCreatorFactory(CREATE_TMP_TBL_DISTSTYLE)
                .newPreparedStatementCreator(Collections.emptyList());
        // create table using ps
        this.redshiftJdbcTemplate.execute(psc, PreparedStatement::execute);

        final PreparedStatementCreator selectTblPsc = new PreparedStatementCreatorFactory(SELECT_TMP_TBL_DISTSTYLE)
                .newPreparedStatementCreator(Collections.emptyList());
        final Boolean result = this.redshiftJdbcTemplate.execute(selectTblPsc, PreparedStatement::execute);
        assertThat("Table does not exist but was expected to.", result);
    }

    @Test
    public void localRedshiftSpecificSqlStatements() {
        final int[] batchUpdate = this.redshiftJdbcTemplate.batchUpdate(
                String.format(REDSHIFT_SQL_CREATE_TABLE_TEMP_DUPLICATE_CLEANUP, ENTITY_TABLE_NAME));

        assertThat(batchUpdate, is(not(nullValue())));
        assertThat(batchUpdate.length, is(1));
        assertThat(batchUpdate[0], is(0));
    }

    @Test
    public void localRedshiftBatchCopyGroup() throws IOException {
        final String name = String.format("tmp_copy_batch_%s", UUID.randomUUID());
        final String testDataId = String.format("id_batch_%s", UUID.randomUUID());
        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name)
                        .build(),
                RequestBody.fromBytes(getCompressedData(
                        createTestDataJson(testDataId, 1, 2))));

        this.redshiftJdbcTemplate.batchUpdate(
                "set query_group to abc",
                createCopyTestQuery(name),
                "reset query_group");

        final List<List<Object>> results = getTestDataById(testDataId);
        assertThat(results, is(not(empty())));
        assertThat(results.size(), is(2));
        assertThat(results.get(0), is(List.of(testDataId, 1)));
        assertThat(results.get(1), is(List.of(testDataId, 2)));
    }

    @Test
    public void localRedshiftSetQueryGroup() {
        try (MockedStatic<ExceptionUtils> utilities = Mockito.mockStatic(ExceptionUtils.class)) {
            // execute set query_group
            this.redshiftJdbcTemplate.execute("set query_group to abc");
            utilities.verify(() -> ExceptionUtils.handleSqlException(
                    ArgumentMatchers.argThat(this::verifySqlExceptionMessage),
                    ArgumentMatchers.argThat(this::verifyConfigProperties)));
        }
    }

    @Test
    public void localRedshiftResetQueryGroup() {
        try (MockedStatic<ExceptionUtils> utilities = Mockito.mockStatic(ExceptionUtils.class)) {
            utilities.when(() -> ExceptionUtils.handleSqlException(
                    ArgumentMatchers.argThat(this::verifySqlExceptionMessage),
                    ArgumentMatchers.argThat(this::verifyConfigProperties)))
                    .thenReturn(true);
            // execute reset query_group
            final PreparedStatementCreator psc = new PreparedStatementCreatorFactory("reset query_group")
                    .newPreparedStatementCreator(Collections.emptyList());
            final Boolean res = this.redshiftJdbcTemplate.execute(psc, PreparedStatement::execute);
            assertThat(res, is(true));
        }
    }

    @Test
    public void localRedshiftCopyByPrefix() throws IOException {
        final String name = String.format("tmp_copy_%s", UUID.randomUUID());
        final String testDataId = String.format("id_%s", UUID.randomUUID());

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name + "_suffix2")
                        .build(),
                RequestBody.fromBytes(getCompressedData(
                        createTestDataJson(testDataId, 3, 4))));

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name + "_suffix1")
                        .build(),
                RequestBody.fromBytes(getCompressedData(
                        createTestDataJson(testDataId, 1, 2))));

        // execute copy query
        this.redshiftJdbcTemplate.execute(createCopyTestQuery(name));

        final List<List<Object>> results = getTestDataById(testDataId);
        assertThat(results, is(not(empty())));
        assertThat(results.size(), is(4));
        assertThat(results.get(0), is(List.of(testDataId, 1)));
        assertThat(results.get(1), is(List.of(testDataId, 2)));
        assertThat(results.get(2), is(List.of(testDataId, 3)));
        assertThat(results.get(3), is(List.of(testDataId, 4)));
    }

    @Test
    public void localRedshiftCopyManifest() throws IOException {
        final String name = String.format("tmp_copy_%s", UUID.randomUUID());
        final String testDataId = String.format("id_%s", UUID.randomUUID());

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name + "_include")
                        .build(),
                RequestBody.fromBytes(getCompressedData(
                        createTestDataJson(testDataId, 3, 4))));

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name + "_exclude")
                        .build(),
                RequestBody.fromBytes(getCompressedData(
                        createTestDataJson(testDataId, 1, 2))));

        this.s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(this.s3BucketSettings.getName())
                        .key(name + "manifest")
                        .build(),
                RequestBody.fromBytes(createTestDataManinfest(name).getBytes(StandardCharsets.UTF_8)));
        // execute copy query
        this.redshiftJdbcTemplate.execute(createCopyTestQueryManifest(name + "manifest"));

        final List<List<Object>> results = getTestDataById(testDataId);
        assertThat(results, is(not(empty())));
        assertThat(results.size(), is(2));
        assertThat(results.get(0), is(List.of(testDataId, 3)));
        assertThat(results.get(1), is(List.of(testDataId, 4)));
    }

    @Test
    public void localRedshiftUnloadUsingPreparedStatement() throws IOException {
        final String testData = insertTestData();

        final String namePrefix = String.format("name_prefix_%s_", UUID.randomUUID());
        final String manifest = namePrefix + "manifest";
        final String unloadQuery = createUnloadTestPreparedStatement(namePrefix);
        // execute unload query to a s3 bucket.
        final PreparedStatementCreator psc = new PreparedStatementCreatorFactory(unloadQuery, Types.VARCHAR)
                .newPreparedStatementCreator(Collections.singletonList(testData));
        this.redshiftJdbcTemplate.execute(psc, PreparedStatement::execute);

        verifyUnload(testData, namePrefix, manifest);
    }

    @Test
    public void localRedshiftUnload() throws IOException {
        final String testData = insertTestData();

        final String namePrefix = String.format("name_prefix_%s_", UUID.randomUUID());
        final String manifest = namePrefix + "manifest";
        final String unloadQuery = createUnloadTestQueryWithLiteralValue(namePrefix, testData);
        // execute unload query to a s3 bucket.
        this.redshiftJdbcTemplate.execute(unloadQuery);

        verifyUnload(testData, namePrefix, manifest);
    }

    private String createUnloadTestPreparedStatement(final String namePrefix) {
        return createUnloadTestQuery(namePrefix, "?");
    }

    private String createUnloadTestQueryWithLiteralValue(final String namePrefix, final String literalValue) {
        return createUnloadTestQuery(namePrefix, "'" + literalValue + "'");
    }

    private String createUnloadTestQuery(final String namePrefix, final String parameterStr) {
        // S3 bucket path consists of URL and name prefix, e.g. "name_prefix_0000_part_00"
        return "UNLOAD ('SELECT test_id FROM tmp_s3_unload WHERE test_id=" + parameterStr + "') "
                + "TO 's3://" + this.s3BucketSettings.getName() + "/" + namePrefix + "' "
                // authorization section
                + "IAM_ROLE 'arn:aws:iam::test-account:role/test' "
                // options section
                + "MANIFEST, PARQUET";
    }

    private String createCopyTestQueryManifest(final String namePrefix) {
        return createCopyTestQuery(namePrefix) + " manifest";
    }

    private String createCopyTestQuery(final String namePrefix) {
        return "COPY tmp_s3_copy FROM "
                + "'s3://"  + this.s3BucketSettings.getName() + "/" + namePrefix + "' "
                + "IAM_ROLE 'arn:aws:iam::test-account:role/test' "
                + "format as json 'auto' GZIP";
    }

    private String createTestDataJson(final String testDataId, final int... magicNumbers) {
        return "{" + "    \"id\": \"" + testDataId + "\","
                + "    \"magic_num\": " + magicNumbers[0] + "}" + "{" + "    \"id\": \""
                + testDataId + "\"," + "    \"magic_num\": " + magicNumbers[1] + "}";
    }

    private String createTestDataManinfest(final String name) {
        return "{  \n" + "   \"entries\":[  \n" + "      {  \n"
                + "         \"url\":\"s3://" + this.s3BucketSettings.getName()
                + "/" + name + "_include" + "\",\n"
                + "         \"mandatory\":true\n" + "      }\n" + "   ]\n"
                + "}";
    }

    private String insertTestData() {
        final String testData = "testData_" + UUID.randomUUID();
        this.redshiftJdbcTemplate.batchUpdate(INSERT_INTO_TMP_S3_UNLOAD, Collections.singletonList(new Object[] {testData}));
        return testData;
    }

    private void verifyUnload(final String testData, final String namePrefix, final String manifest)
            throws IOException {
        // get the relevant files from the s3 bucket
        final ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(this.s3BucketSettings.getName())
                .build();
        final List<String> relevantObjects = this.s3Client.listObjectsV2(listObjectsRequest).contents()
                .stream()
                .map(S3Object::key)
                // for a troubleshooting mostly
                .peek(log::debug)
                .filter(e -> e.startsWith(namePrefix))
                .collect(Collectors.toList());
        // manifest plus exactly one part
        assertThat(relevantObjects.size(), is(2));

        // download the actual result by objectkey
        final String objKey = relevantObjects.stream()
                .filter(((Predicate<String>)manifest::equals).negate())
                .findFirst()
                .orElseThrow();

        final Path tempDirectory = Files.createTempDirectory(null);
        final Path downloadLocation = tempDirectory.resolve("content.tmp");
        try {
            this.s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(this.s3BucketSettings.getName())
                            .key(objKey)
                            .build(),
                    downloadLocation);

            // read parquet
            final String resultData = readParquetRecords(downloadLocation).stream()
                    .map(this::getTestColumnValue)
                    .peek(log::debug)
                    .findFirst()
                    .orElseThrow();
            // verify the data is what we need.
            assertThat(resultData, is(testData));
        } finally {
            tempDirectory.toFile().deleteOnExit();
            downloadLocation.toFile().deleteOnExit();
        }
    }

    private List<Map<String, Object>> readParquetRecords(final Path parquetTmpFilePath)
            throws IOException {
        final List<Map<String, Object>> parquetRecords;
        final HadoopInputFile file = HadoopInputFile.fromPath(
                new org.apache.hadoop.fs.Path(parquetTmpFilePath.toString()),
                new org.apache.hadoop.conf.Configuration());
        try (ParquetReader<GenericRecord> parquetReader = AvroParquetReader.<GenericRecord>builder(file).build()) {
            parquetRecords = Stream.iterate(parquetReader.read(), Objects::nonNull, r -> record(parquetReader))
                    .map(this::toMap)
                    .collect(Collectors.toList());
        }
        return parquetRecords;
    }

    private String getTestColumnValue(final Map<String, Object> parquetRecord) {
        return parquetRecord.get("test_id").toString();
    }

    private byte[] getCompressedData(final String testDataJson) throws IOException {
        final byte[] dataToCompress = testDataJson.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(dataToCompress.length)) {
            try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                zipStream.write(dataToCompress);
            }
            return byteStream.toByteArray();
        }
    }

    private boolean verifySqlExceptionMessage(final InvocationTargetException exception) {
        final ServerErrorMessage serverErrorMessage = ((PSQLException)exception.getTargetException()).getServerErrorMessage();
        return serverErrorMessage != null && "unrecognized configuration parameter \"query_group\"".equals(serverErrorMessage.getMessage())
                && "set_config_option".equals(serverErrorMessage.getRoutine());
    }

    private boolean verifyConfigProperties(final Properties properties) {
        return "query_group".equals(properties.getProperty(TestRedshiftDriver.SKIP_ERRORS_ON_MISSING_CONFIG_OPTIONS));
    }

    private List<List<Object>> getTestDataById(final String testDataId) {
        return this.redshiftJdbcTemplate.query(
                QUERY_TEST_DATA_CMD,
                ps -> ps.setString(1, testDataId),
                (rs, rowNum) -> List.of(rs.getString("id"), rs.getInt("magic_num")));
    }

    @SneakyThrows
    private GenericRecord record(final ParquetReader<GenericRecord> parquetReader) {
        return parquetReader.read();
    }

    private Map<String, Object> toMap(final GenericRecord record) {
        return record.getSchema().getFields().stream()
                .map(field -> Map.entry(field.name(), record.get(field.name())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
