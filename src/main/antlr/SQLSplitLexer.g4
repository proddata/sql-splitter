lexer grammar SQLSplitLexer;

// Whitespace
WS : [ \t\r\n]+ ;

// Comments
LINE_COMMENT        : '--' ~[\r\n]* ;
BLOCK_COMMENT       : '/*' .*? '*/' ;

// Strings
SINGLE_QUOTE_STRING : '\'' ( '\'\'' | ~'\'' )* '\'' ;
DOUBLE_QUOTE_STRING : '"'  ( '""'   | ~'"'   )* '"' ;

// PostgreSQL / Snowflake dollar-quoted strings: $$…$$ or $tag$…$tag$
DOLLAR_QUOTE
    : '$' TAG? '$' (~'$')* '$' TAG? '$'
    ;
fragment TAG : [A-Za-z_][A-Za-z_0-9]* ;

// Procedural block keywords
BEGIN_KW            : [Bb][Ee][Gg][Ii][Nn] ;
END_KW              : [Ee][Nn][Dd] ;

// Delimiters
SEMICOLON           : ';' ;
SLASH_DELIMITER     : '/' ;

// Fallback keyword and other tokens
KEYWORD             : [A-Za-z_][A-Za-z_0-9]* ;
OTHER               : . ;