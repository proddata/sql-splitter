package io.kestra.sql;

import io.kestra.sql.grammar.SQLSplitLexer;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class SimpleSqlSplitter {

    public static List<String> split(String script) {
        SQLSplitLexer lexer = new SQLSplitLexer(CharStreams.fromString(script));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int blockDepth = 0;
        boolean inDollarQuote = false;

        for (Token token : tokens.getTokens()) {
            int type = token.getType();
            String text = token.getText();

            // PostgreSQL dollar-quoted block: treat as opaque
            if (type == SQLSplitLexer.DOLLAR_QUOTE) {
                current.append(text);
                continue; // no delimiters inside
            }

            // Track procedural blocks (Postgres + Oracle)
            if (type == SQLSplitLexer.BEGIN_KW) {
                blockDepth++;
            } else if (type == SQLSplitLexer.END_KW) {
                blockDepth = Math.max(0, blockDepth - 1);
            }

            boolean safeDelimiter =
                    (type == SQLSplitLexer.SEMICOLON)
                            && blockDepth == 0;

            if (safeDelimiter) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(text);
            }
        }

        if (!current.toString().trim().isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }
}