package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractSqlSplitTest {

    protected List<String> split(String sql) {
        return new SimpleSqlSplitter(SqlDialect.GENERIC).split(sql);
    }

    protected void assertStatementCount(String sql, int expected) {
        List<String> statements = split(sql);
        assertEquals(expected, statements.size(),
                "Expected " + expected + " statements, got " + statements.size());
    }

    protected void assertContains(String statement, String expected) {
        assertTrue(statement.contains(expected),
                "Expected statement to contain: " + expected);
    }
}