package io.kestra.sql;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BlockSplitTest {

    @Test
    void postgresFunctionWithDollarQuotes() {
        String sql = """
            CREATE FUNCTION test_fn()
            RETURNS void AS $$
            BEGIN
                PERFORM do_something();
                RAISE NOTICE 'hello;';
            END;
            $$ LANGUAGE plpgsql;

            SELECT 1;
            """;

        List<String> out = SimpleSqlSplitter.split(sql);
        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("CREATE FUNCTION"));
        assertTrue(out.get(1).contains("SELECT 1"));
    }

    @Test
    void oracleProcedureBlock() {
        String sql = """
            BEGIN
                INSERT INTO table_a VALUES (1);
                UPDATE table_b SET x = 1 WHERE y = 2;
            END;
            SELECT * FROM table_a;
            """;

        List<String> out = SimpleSqlSplitter.split(sql);
        assertEquals(2, out.size());
        assertTrue(out.get(0).startsWith("BEGIN"));
        assertTrue(out.get(1).startsWith("SELECT"));
    }

    @Test
    void nestedBlocks() {
        String sql = """
            BEGIN
                BEGIN
                    SELECT 1;
                END;
                SELECT 2;
            END;
            SELECT 3;
            """;

        List<String> out = SimpleSqlSplitter.split(sql);

        assertEquals(2, out.size());
        assertTrue(out.get(0).contains("SELECT 2;"));
        assertTrue(out.get(1).contains("SELECT 3;"));
    }
}