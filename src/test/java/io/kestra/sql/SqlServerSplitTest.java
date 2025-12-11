package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlServerSplitTest {

    @Test
    void splitsOnGoDelimiter() {
        String sql = """
            SELECT 1
            GO
            SELECT 2
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("SELECT 1"));
        assertTrue(out.get(1).contains("SELECT 2"));
    }

    @Test
    void goWithWhitespaceStillSplits() {
        String sql = """
            SELECT 1
                 GO     
            SELECT 2
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
    }

    @Test
    void goWithCommentStillSplits() {
        String sql = """
            SELECT 1;
            GO  -- comment allowed
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
    }

    @Test
    void lowercaseGoStillSplits() {
        String sql = """
            SELECT 1
            go
            SELECT 2
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
    }

    @Test
    void goMustBeOnOwnLineNotInline() {
        String sql = """
            SELECT 1 GO SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        // Nothing should split, this is one invalid-but-single statement
        assertEquals(1, out.size());
    }

    @Test
    void goInsideStringDoesNotSplit() {
        String sql = """
            SELECT 'hello GO world';
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
    }

    @Test
    void goInsideCommentDoesNotSplit() {
        String sql = """
            -- GO inside a comment should not split
            SELECT 1;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(2, out.size());
    }

    @Test
    void multipleGoDelimiters() {
        String sql = """
            SELECT 1;
            GO
            SELECT 2;
            GO
            SELECT 3;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.SQLSERVER, sql);

        assertEquals(3, out.size());
        assertTrue(out.get(0).contains("SELECT 1"));
        assertTrue(out.get(1).contains("SELECT 2"));
        assertTrue(out.get(2).contains("SELECT 3"));
    }
}