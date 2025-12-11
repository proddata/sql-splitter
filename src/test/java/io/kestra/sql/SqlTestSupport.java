package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import java.util.List;

public class SqlTestSupport {

    public static List<String> split(SqlDialect dialect, String sql) {
        return new SimpleSqlSplitter(dialect).split(sql);
    }
}