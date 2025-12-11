package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericSplitTest {

    @Test
    void splitsSimpleSemicolonStatements() {
        String sql = "SELECT 1; SELECT 2;";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("SELECT 1"));
        assertTrue(out.get(1).contains("SELECT 2"));
    }

    @Test
    void ignoresSemicolonInString() {
        String sql = "SELECT 'a;b;c'; SELECT 2;";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);

        assertEquals(2, out.size());
    }

    @Test
    void ignoresSemicolonInDoubleQuotedIdentifier() {
        String sql = """
            SELECT "weird;identifier", col FROM t;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void ignoresSemicolonInBacktickIdentifier() {
        String sql = """
            SELECT `col;name`, id FROM table;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void ignoresSemicolonInBracketIdentifier() {
        String sql = """
            SELECT [id;name], x FROM some_table;
            SELECT 5;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void multilineStatements() {
        String sql = """
            SELECT
                id,
                name,
                'value;still string'
            FROM
                customers;
            SELECT 1;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void semicolonInsideCommentShouldNotSplit() {
        String sql = """
            -- semicolon ; inside comment
            SELECT 1;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(1, out.size());
    }

    @Test
    void blockCommentWithSemicolonShouldNotSplit() {
        String sql = """
            /* block comment ; inside */
            SELECT 1;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(1, out.size());
    }

    @Test
    void multipleCommentsEverywhere() {
        String sql = """
            -- beginning comment ;
            /* block comment before */
            SELECT /* inline comment; */ 1;
            /* block comment after */
            SELECT 2; -- trailing comment
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void emptyStatementsShouldBeIgnored() {
        String sql = ";;;SELECT 1;; ; ; SELECT 2;;;;";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("SELECT 1"));
        assertTrue(out.get(1).contains("SELECT 2"));
    }

    @Test
    void unicodeCharactersIncluded() {
        String sql = """
            SELECT 'hello 🦄 ; world';
            SELECT '€100; still ok';
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }

    @Test
    void semicolonInMalformedButQuotedStrings() {
        // SQL sometimes appears malformed but valid enough for splitting…
        String sql = """
            SELECT 'unclosed string ; still no split
            FROM t;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);

        // Your lexer treats '...' as a STRING token until next '
        // Safe enough for test.
        assertEquals(2, out.size());
    }

    @Test
    void noSemicolonMeansOneStatement() {
        String sql = "SELECT 1";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(1, out.size());
    }

    @Test
    void weirdFormattingShouldStillWork() {
        String sql = "  \n\n   SELECT   1   ;\n\t  SELECT   2 \t ;";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);
        assertEquals(2, out.size());
    }
}