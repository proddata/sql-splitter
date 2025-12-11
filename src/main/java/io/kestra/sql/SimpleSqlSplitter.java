package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import io.kestra.sql.dialect.SqlDialectConfig;
import io.kestra.sql.dialect.SqlDialectRegistry;
import io.kestra.sql.grammar.SQLSplitLexer;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class SimpleSqlSplitter {

    private final SqlDialectConfig config;

    public SimpleSqlSplitter(SqlDialect dialect) {
        this.config = SqlDialectRegistry.config(dialect);
    }

    public List<String> split(String script) {
        SQLSplitLexer lexer = new SQLSplitLexer(CharStreams.fromString(script));
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();

        List<Token> tokens = stream.getTokens();
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int blockDepth = 0;
        boolean hasContent = false;   // <-- NEW: tracks whether statement has real content

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int type = token.getType();
            String text = token.getText();

            // Skip EOF entirely (fixes <EOF> appearing in last statement)
            if (type == Token.EOF) {
                break;
            }

            // Skip leading comments before first real statement token
            if (!hasContent &&
                    (type == SQLSplitLexer.LINE_COMMENT || type == SQLSplitLexer.BLOCK_COMMENT)) {
                continue;
            }

            // Dollar-quoted blocks (Postgres, Snowflake)
            if (type == SQLSplitLexer.DOLLAR_QUOTE && config.supportsDollarQuotes()) {
                current.append(text);
                hasContent = true;
                continue;
            }

            // BEGIN / END block handling
            if (config.supportsBeginEndBlocks()) {
                if (isBeginBlock(i, tokens)) {
                    blockDepth++;
                } else if (text.equalsIgnoreCase("END")) {
                    blockDepth = Math.max(0, blockDepth - 1);
                }
            }

            // SQL Server GO
            if (config.supportsGoDelimiter() && isGoDelimiter(token, script)) {
                flushCurrent(result, current);
                hasContent = false;
                continue;
            }

            // Oracle slash delimiter
            if (config.supportsSlashDelimiter() && isSlashDelimiter(token, script)) {
                flushCurrent(result, current);
                hasContent = false;
                continue;
            }

            // Semicolon splitting when not inside BEGIN...END
            boolean safeSemicolon =
                    config.semicolonIsDelimiter()
                            && type == SQLSplitLexer.SEMICOLON
                            && blockDepth == 0;

            if (safeSemicolon) {
                flushCurrent(result, current);
                hasContent = false;
                continue;
            }

            // Append all other tokens (including whitespace!)
            current.append(text);

            // Mark content when encountering significant tokens
            if (type != SQLSplitLexer.WS
                    && type != SQLSplitLexer.LINE_COMMENT
                    && type != SQLSplitLexer.BLOCK_COMMENT) {
                hasContent = true;
            }
        }

        // trailing statement
        flushCurrent(result, current);

        return result;
    }

    private void flushCurrent(List<String> result, StringBuilder current) {
        String sql = current.toString().trim();

        if (sql.isEmpty() || sql.equals("<EOF>")) {
            current.setLength(0);
            return;
        }

        result.add(sql);
        current.setLength(0);
    }

    private boolean isBeginBlock(int index, List<Token> tokens) {
        Token token = tokens.get(index);
        String text = token.getText();

        if (!text.equalsIgnoreCase("BEGIN")) {
            return false;
        }

        // DB2 / AS400: BEGIN ATOMIC
        if (config.supportsAtomicBlocks() && index + 1 < tokens.size()) {
            Token next = tokens.get(index + 1);
            if (next.getText().equalsIgnoreCase("ATOMIC")) {
                return true;
            }
        }

        return true;
    }

    private boolean isGoDelimiter(Token token, String script) {
        String text = token.getText();
        if (!text.equalsIgnoreCase("GO")) {
            return false;
        }

        String line = extractLine(token, script).trim();
        return line.equalsIgnoreCase("GO");
    }

    private boolean isSlashDelimiter(Token token, String script) {
        String text = token.getText();
        if (!"/".equals(text)) {
            return false;
        }

        String line = extractLine(token, script).trim();
        return "/".equals(line);
    }

    private String extractLine(Token token, String script) {
        int start = token.getStartIndex();
        int stop = token.getStopIndex();

        int lineStart = start;
        while (lineStart > 0) {
            char c = script.charAt(lineStart - 1);
            if (c == '\n' || c == '\r') {
                break;
            }
            lineStart--;
        }

        int lineEnd = stop;
        while (lineEnd + 1 < script.length()) {
            char c = script.charAt(lineEnd + 1);
            if (c == '\n' || c == '\r') {
                break;
            }
            lineEnd++;
        }

        return script.substring(lineStart, lineEnd + 1);
    }
}