
> [!CAUTION]
> **This project was generated with ChatGPT for demo purposes.  
Review and harden before using in production.**

# SQL Splitter (Java + ANTLR)

A lightweight, dialect-aware SQL statement splitter for Java.
It focuses on *splitting SQL scripts*, not parsing or validating SQL.



## 📦 Project Structure

```sh
sql-splitter/
  src/
    main/
      antlr/         # ANTLR lexer grammar
        SQLSplitLexer.g4
      java/
        io/kestra/sql/SimpleSqlSplitter.java
    test/
      java/          # JUnit tests for PG + Oracle blocks
        BlockSplitTest.java
  build.gradle.kts   # Gradle build with ANTLR + JUnit
  ```


## 🧠 How It Works (Short)

1. **ANTLR lexer** identifies strings, comments, dollar quotes, keywords, and delimiters.  
2. **Java state machine** tracks procedural nesting (`BEGIN`/`END`).  
3. A semicolon splits statements **only when block depth == 0**.  

This makes the splitter safe for functions, procedures, and nested blocks.

---

## 🚀 Example

Input:

```sql
CREATE FUNCTION test_fn() RETURNS void AS $$
BEGIN
  PERFORM do_something();
END;
$$ LANGUAGE plpgsql;

SELECT 1;
```

Output:

```js
[
  "CREATE FUNCTION … LANGUAGE plpgsql;",
  "SELECT 1;"
]
```