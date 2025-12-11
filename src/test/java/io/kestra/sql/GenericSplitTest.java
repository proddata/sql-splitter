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
    }

    @Test
    void ignoresSemicolonInString() {
        String sql = "SELECT 'a;b;c'; SELECT 2;";

        List<String> out = SqlTestSupport.split(SqlDialect.GENERIC, sql);

        assertEquals(2, out.size());
    }
}