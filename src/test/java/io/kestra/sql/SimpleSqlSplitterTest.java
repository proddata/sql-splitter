package io.kestra.sql;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleSqlSplitterTest {

    @Test
    void basicSplit() {
        String sql = """
            SELECT 1;
            SELECT 'hello; world';
            BEGIN
               SELECT 2;
            END;
            """;

        List<String> result = SimpleSqlSplitter.split(sql);

        assertEquals(3, result.size());
        assertTrue(result.get(0).contains("SELECT 1"));
        assertTrue(result.get(1).contains("SELECT 'hello; world'"));
        assertTrue(result.get(2).contains("BEGIN"));
    }
}