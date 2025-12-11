package io.kestra.sql.dialect;

import java.util.Set;

public interface SqlDialectConfig {

    /** Should semicolon split statements? Usually yes. */
    default boolean semicolonIsDelimiter() {
        return true;
    }

    /** Does this dialect support dollar-quoted strings (Postgres)? */
    default boolean supportsDollarQuotes() {
        return false;
    }

    /** Does the dialect support BEGIN … END blocks for procedural code? */
    default boolean supportsBeginEndBlocks() {
        return false;
    }

    /** DB2-specific BEGIN ATOMIC blocks */
    default boolean supportsAtomicBlocks() {
        return false;
    }

    /** SQL Server — enable GO splitting (line-based batch separator) */
    default boolean supportsGoDelimiter() {
        return false;
    }

    /** Oracle — slash (/) delimiter on its own line */
    default boolean supportsSlashDelimiter() {
        return false;
    }

    /** Return a set of keywords that open procedural blocks */
    default Set<String> blockStartKeywords() {
        return Set.of();
    }

    /** Return a set of keywords that close procedural blocks */
    default Set<String> blockEndKeywords() {
        return Set.of();
    }
}