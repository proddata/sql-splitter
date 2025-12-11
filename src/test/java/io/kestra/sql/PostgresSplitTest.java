package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PostgresSplitTest {

    @Test
    void splitsSimpleStatements() {
        String sql = """
            SELECT 1;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.POSTGRES, sql);

        assertEquals(2, out.size());
    }

    @Test
    void handlesBeginEndBlocks() {
        String sql = """
            DO $$
            BEGIN
                PERFORM 1;
            END;
            $$;
            SELECT 1;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.POSTGRES, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).startsWith("DO $$"));
    }

    @Test
    void dollarQuotedStringWithSemicolons() {
        String sql = """
            SELECT $$ hello; world; $$;
            SELECT 2;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.POSTGRES, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("hello; world"));
    }
}