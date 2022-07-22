/*
 * Copyright 2021 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local.aws.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecordBuilder;

/**
 * Transforms SQL types into another format.
 */
final class SqlTransformer {

    private SqlTransformer() {
    }

    // cyclomatic complexity 39
    // CHECKSTYLE:OFF
    static void extractResult(int sqlType,
            String sqlColumnName,
            ResultSet resultSet,
            GenericRecordBuilder genericRecordBuilder) throws SQLException {

        switch (sqlType) {
        case Types.BOOLEAN:
            genericRecordBuilder.set(sqlColumnName, resultSet.getBoolean(sqlColumnName));
            break;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getInt(sqlColumnName));
            break;
        case Types.BIGINT:
        case Types.ROWID:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getLong(sqlColumnName));
            break;
        case Types.REAL:
        case Types.FLOAT:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getFloat(sqlColumnName));
            break;
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getDouble(sqlColumnName));
            break;
        case Types.DATE:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getDate(sqlColumnName).getTime());
            break;
        case Types.TIME:
        case Types.TIME_WITH_TIMEZONE:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getTime(sqlColumnName).getTime());
            break;
        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getTimestamp(sqlColumnName).getTime());
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.NULL:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
        case Types.REF:
        case Types.DATALINK:
        case Types.NCLOB:
        case Types.REF_CURSOR:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getByte(sqlColumnName));
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.SQLXML:
        default:
            genericRecordBuilder.set(sqlColumnName,  resultSet.getString(sqlColumnName));
        }
    }
    // CHECKSTYLE:ON

    // cyclomatic complexity 39
    // CHECKSTYLE:OFF
    static void extractSchema(int sqlType,
            String sqlColumnName,
            SchemaBuilder.FieldAssembler<Schema> schemaFieldAssembler) {
        switch (sqlType) {
        case Types.BOOLEAN:
            schemaFieldAssembler.optionalBoolean(sqlColumnName);
            break;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            schemaFieldAssembler.optionalInt(sqlColumnName);
            break;
        case Types.BIGINT:
        case Types.ROWID:
        case Types.DATE:
        case Types.TIME:
        case Types.TIME_WITH_TIMEZONE:
        case Types.TIMESTAMP:
        case Types.TIMESTAMP_WITH_TIMEZONE:
            schemaFieldAssembler.optionalLong(sqlColumnName);
            break;
        case Types.REAL:
        case Types.FLOAT:
            schemaFieldAssembler.optionalFloat(sqlColumnName);
            break;
        case Types.DOUBLE:
        case Types.DECIMAL:
        case Types.NUMERIC:
            schemaFieldAssembler.optionalDouble(sqlColumnName);
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.NULL:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
        case Types.REF:
        case Types.DATALINK:
        case Types.NCLOB:
        case Types.REF_CURSOR:
            schemaFieldAssembler.optionalBytes(sqlColumnName);
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.SQLXML:
        default:
            schemaFieldAssembler.optionalString(sqlColumnName);
        }
    }
    // CHECKSTYLE:ON
}
