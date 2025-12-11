package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DuckDbSplitTest {

    @Test
    void simpleMultiStatementScript() {
        String sql = """
            CREATE TABLE t (id INTEGER, name VARCHAR);
            INSERT INTO t VALUES (1, 'alice');
            SELECT * FROM t;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.DUCKDB, sql);

        assertEquals(3, out.size());
        assertTrue(out.get(0).startsWith("CREATE TABLE"));
    }

    @Test
    void semicolonInsideString() {
        String sql = """
            INSERT INTO t VALUES ('a;b;c');
            SELECT 1;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.DUCKDB, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("'a;b;c'"));
    }

    @Test
    void semicolonInsideComments() {
        String sql = """
            -- this should not split ;
            CREATE TABLE x (id int);
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.DUCKDB, sql);

        assertEquals(1, out.size());
        assertTrue(out.get(0).startsWith("CREATE"));
    }
}