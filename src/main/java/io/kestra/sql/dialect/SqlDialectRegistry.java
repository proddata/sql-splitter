package io.kestra.sql.dialect;

import java.util.Set;

public class SqlDialectRegistry {

    public static SqlDialectConfig config(SqlDialect dialect) {
        return switch (dialect) {

            case POSTGRES -> new SqlDialectConfig() {
                @Override public boolean supportsDollarQuotes() { return true; }
                @Override public boolean supportsBeginEndBlocks() { return true; }
                @Override public Set<String> blockStartKeywords() { return Set.of("BEGIN"); }
                @Override public Set<String> blockEndKeywords() { return Set.of("END"); }
            };

            case ORACLE -> new SqlDialectConfig() {
                @Override public boolean supportsBeginEndBlocks() { return true; }
                @Override public boolean supportsSlashDelimiter() { return true; }
                @Override public Set<String> blockStartKeywords() { return Set.of("BEGIN"); }
                @Override public Set<String> blockEndKeywords() { return Set.of("END"); }
            };

            case SQLSERVER -> new SqlDialectConfig() {
                @Override public boolean supportsGoDelimiter() { return true; }
            };

            case DB2, AS400 -> new SqlDialectConfig() {
                @Override public boolean supportsAtomicBlocks() { return true; }
                @Override public boolean supportsBeginEndBlocks() { return true; }
                @Override public Set<String> blockStartKeywords() { return Set.of("BEGIN", "BEGIN ATOMIC"); }
                @Override public Set<String> blockEndKeywords() { return Set.of("END"); }
            };

            case SNOWFLAKE -> new SqlDialectConfig() {
                @Override public boolean supportsDollarQuotes() { return true; }
            };

            case MYSQL, MARIADB, DUCKDB, REDSHIFT, SQLITE, TRINO,
                 CLICKHOUSE, DREMIO, PINOT, VERTICA, VECTORWISE,
                 DRUID, GENERIC -> new SqlDialectConfig() {
            };

        };
    }
}