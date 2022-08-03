/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.utils;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.ListAliasesResponse;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.CollectionUtils;

import com.vmware.test.functional.saas.ServiceEndpoint;
import com.vmware.test.functional.saas.local.aws.kms.KmsHealthHelper;

import io.trino.jdbc.TrinoDriver;

import static org.awaitility.Awaitility.await;

/*
 * Utility methods used for checking started services in docker containers.
 */
public final class ServiceDependenciesHealthHelper {

    private ServiceDependenciesHealthHelper() {
    }

    public static boolean isKmsHealthy(final KmsClient awskmsClient) {
        final String testAliasName = "alias/testing";
        await().until(() -> KmsHealthHelper.checkHealth(awskmsClient, testAliasName));

        final ListAliasesResponse listAliasesResponse = awskmsClient.listAliases();
        if (listAliasesResponse.hasAliases()) {
            return listAliasesResponse.aliases()
                    .stream()
                    .anyMatch(a -> a.aliasName().equals(testAliasName));
        }
        return false;
    }

    public static boolean isRedisHealthy(final RedisTemplate<String, String> redisTemplate) {
        final String redisHashKey = "test_redis_key";
        final String redisTestObject = "test_redis_obj";

        redisTemplate.opsForList().leftPush(redisHashKey, redisTestObject);
        return redisTestObject.equals(redisTemplate.opsForList().index(redisHashKey, 0));
    }

    public static boolean isTrinoHealthy(final ServiceEndpoint trinoEndpoint, final String catalog) {
        final String testDb = "test_trino_db";
        final String dropTestSchemaStmt = "drop schema if exists " + catalog + "." + testDb;
        final String createTestSchemaStmt = "create schema " + catalog + "." + testDb;
        final String showSchemasStmt = "show schemas";

        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(TrinoDriver.class);
        dataSource.setUrl("jdbc:trino://localhost:" + trinoEndpoint.getPort() + "/" + catalog);
        dataSource.setUsername("test");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute(dropTestSchemaStmt);
        jdbcTemplate.execute(createTestSchemaStmt);
        final List<String> databases = jdbcTemplate.query(showSchemasStmt, resultSetObj -> {
            final List<String> databasesList = new ArrayList<>();
            while (resultSetObj.next()) {
                if (resultSetObj.getString(1).equals(testDb)) {
                    databasesList.add(resultSetObj.getString(1));
                }
            }
            return databasesList;
        });
        return !CollectionUtils.isEmpty(databases);
    }

    public static boolean isPostgresHealthy(final ServiceEndpoint postgresEndpoint, final String databaseName) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:" + postgresEndpoint.getPort() + "/postgres");
        dataSource.setUsername("postgres");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String selectDatabaseQuery = String.format("SELECT COUNT(datname) FROM pg_database where datname = '%s';", databaseName);
        return Boolean.TRUE.equals(jdbcTemplate.query(selectDatabaseQuery, resultSet -> {
            resultSet.next();
            return resultSet.getInt(1) == 1;
        }));
    }
}
