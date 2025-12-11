package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OracleSplitTest {

    @Test
    void handlesSlashDelimiterAfterBlock() {
        String sql = """
            BEGIN
               NULL;
            END;
            /
            SELECT * FROM dual;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.ORACLE, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).startsWith("BEGIN"));
    }

    @Test
    void ignoresSlashInsideString() {
        String sql = """
            SELECT 'a/b' FROM dual;
            SELECT 1 FROM dual;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.ORACLE, sql);

        assertEquals(2, out.size());
    }
}