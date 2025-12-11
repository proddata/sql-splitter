package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OracleSplitTest {

    @Test
    void simpleBeginEndBlock() {
        String sql = """
            BEGIN
              NULL;
            END;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.ORACLE, sql);

        assertEquals(1, out.size());
        String stmt = out.get(0).trim();
        assertTrue(stmt.startsWith("BEGIN"));
        assertTrue(stmt.endsWith("END;"));
    }

    @Test
    void complexForLoopBlock() {
        String sql = """
            BEGIN
              FOR record IN (
                SELECT ROWNUM n
                FROM (
                  SELECT 1 just_a_column
                  FROM dual
                  GROUP BY CUBE(1,2,3,4,5,6,7,8,9)
                )
                WHERE ROWNUM <= 20
              )
              LOOP
                dbms_output.put_line(record.n);
              END LOOP;
            END;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.ORACLE, sql);

        assertEquals(1, out.size());
        String stmt = out.get(0).trim();
        assertTrue(stmt.startsWith("BEGIN"));
        assertTrue(stmt.contains("END LOOP;"));
        assertTrue(stmt.endsWith("END;"));
    }

    @Test
    void twoBlocksSeparatedBySlash() {
        String sql = """
            BEGIN
              NULL;
            END;
            /
            BEGIN
              NULL;
            END;
            """;

        List<String> out = SqlTestSupport.split(SqlDialect.ORACLE, sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("END;"));
        assertTrue(out.get(1).contains("END;"));
    }
}