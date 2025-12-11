package io.kestra.sql;

import io.kestra.sql.dialect.SqlDialect;
import io.kestra.sql.dialect.SqlDialectConfig;
import io.kestra.sql.dialect.SqlDialectRegistry;
import io.kestra.sql.grammar.SQLSplitLexer;
import org.antlr.v4.runtime.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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

        Deque<String> blockStack = new ArrayDeque<>();   // BEGIN, LOOP, IF, CASE
        boolean hasContent = false;
        boolean endJustClosed = false;                   // END that just closed OUTERMOST block

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int type  = token.getType();
            String text  = token.getText();
            String upper = text.toUpperCase();

            // Stop at EOF
            if (type == Token.EOF) {
                break;
            }

            // Skip leading comments entirely
            if (!hasContent &&
                    (type == SQLSplitLexer.LINE_COMMENT || type == SQLSplitLexer.BLOCK_COMMENT)) {
                continue;
            }

            // Dollar-quoted blocks (Postgres / Snowflake)
            if (type == SQLSplitLexer.DOLLAR_QUOTE && config.supportsDollarQuotes()) {
                current.append(text);
                hasContent = true;
                continue;
            }

            // -----------------------
            // Block stack management
            // -----------------------
            if (config.supportsBeginEndBlocks()) {
                // BEGIN (generic)
                if (upper.equals("BEGIN")) {
                    blockStack.push("BEGIN");
                    endJustClosed = false;
                }
                // LOOP (PL/SQL, etc.)
                else if (upper.equals("LOOP")) {
                    blockStack.push("LOOP");
                    endJustClosed = false;
                }
                // IF / CASE blocks
                else if (upper.equals("IF")) {
                    blockStack.push("IF");
                    endJustClosed = false;
                } else if (upper.equals("CASE")) {
                    blockStack.push("CASE");
                    endJustClosed = false;
                }
                // END, END LOOP, END IF, END CASE
                else if (upper.equals("END")) {
                    String nextUpper = "";
                    if (i + 1 < tokens.size()) {
                        nextUpper = tokens.get(i + 1).getText().toUpperCase();
                    }

                    String top = blockStack.peek();

                    if (top != null) {
                        // END LOOP
                        if (nextUpper.equals("LOOP") && top.equals("LOOP")) {
                            blockStack.pop();
                        }
                        // END IF
                        else if (nextUpper.equals("IF") && top.equals("IF")) {
                            blockStack.pop();
                        }
                        // END CASE
                        else if (nextUpper.equals("CASE") && top.equals("CASE")) {
                            blockStack.pop();
                        }
                        // Plain END; closes whatever is on the stack (typically BEGIN)
                        else if (!nextUpper.equals("LOOP")
                                && !nextUpper.equals("IF")
                                && !nextUpper.equals("CASE")) {
                            blockStack.pop();
                        }

                        // endJustClosed is only relevant if we just closed the OUTERMOST block
                        endJustClosed = blockStack.isEmpty();
                    } else {
                        endJustClosed = false;
                    }
                }
            }

            // -----------------------
            // SQL Server GO delimiter
            // -----------------------
            if (config.supportsGoDelimiter() && isGoDelimiter(token, script)) {
                flushCurrent(result, current);
                hasContent = false;
                endJustClosed = false;
                blockStack.clear();   // conservative reset for safety
                continue;
            }

            // -----------------------
            // Oracle slash delimiter (/)
            // -----------------------
            if (config.supportsSlashDelimiter() && isSlashDelimiter(token, script)) {
                flushCurrent(result, current);
                hasContent = false;
                endJustClosed = false;
                blockStack.clear();
                continue;
            }

            // -----------------------
            // Semicolon handling
            // -----------------------
            if (type == SQLSplitLexer.SEMICOLON && config.semicolonIsDelimiter()) {
                if (blockStack.isEmpty()) {
                    if (endJustClosed) {
                        // This semicolon directly follows END of outermost block:
                        // do NOT split, just make the block END; one statement.
                        current.append(text);
                        endJustClosed = false;
                    } else {
                        // Top-level delimiter: split
                        flushCurrent(result, current);
                        hasContent = false;
                        endJustClosed = false;
                    }
                } else {
                    // Inside any block (BEGIN/LOOP/IF/CASE): never split
                    current.append(text);
                    // keep endJustClosed as-is
                }
                continue;
            }

            // Normal tokens → append
            current.append(text);

            // Mark that we've seen content (anything but pure whitespace/comments)
            if (type != SQLSplitLexer.WS &&
                    type != SQLSplitLexer.LINE_COMMENT &&
                    type != SQLSplitLexer.BLOCK_COMMENT) {
                hasContent = true;
                // For any non-structural token after END, we no longer care about endJustClosed
                // but since we only use endJustClosed in conjunction with an immediate semicolon,
                // and we handle that above, we don't strictly need to reset it here.
            }
        }

        // Trailing statement
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

    private boolean isGoDelimiter(Token token, String script) {
        String text = token.getText();
        if (!text.equalsIgnoreCase("GO")) {
            return false;
        }
        String line = extractLine(token, script).trim();
        return line.equalsIgnoreCase("GO");
    }

    private boolean isSlashDelimiter(Token token, String script) {
        if (!"/".equals(token.getText())) {
            return false;
        }
        String line = extractLine(token, script).trim();
        return "/".equals(line);
    }

    private String extractLine(Token token, String script) {
        int start = token.getStartIndex();
        int stop  = token.getStopIndex();

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