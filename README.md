
# 🗃️ Java Relational DB Engine – From Scratch

This project is a **fully functional relational database server** implemented in Java from the ground up. It parses a custom SQL-like query language, supports persistent file-based storage, and handles advanced operations like JOINs and query parsing — all without using external libraries or frameworks for parsing or persistence.

Specification in Backus Naur Form:
```
<Command>         ::=  <CommandType> ";"
<CommandType>     ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>
<Use>             ::=  "USE " [DatabaseName]
<Create>          ::=  <CreateDatabase> | <CreateTable>
<CreateDatabase>  ::=  "CREATE " "DATABASE " [DatabaseName]
<CreateTable>     ::=  "CREATE " "TABLE " [TableName] | "CREATE " "TABLE " [TableName] "(" <AttributeList> ")"
<Drop>            ::=  "DROP " "DATABASE " [DatabaseName] | "DROP " "TABLE " [TableName]
<Alter>           ::=  "ALTER " "TABLE " [TableName] " " <AlterationType> " " [AttributeName]
<Insert>          ::=  "INSERT " "INTO " [TableName] " VALUES" "(" <ValueList> ")"
<Select>          ::=  "SELECT " <WildAttribList> " FROM " [TableName] | "SELECT " <WildAttribList> " FROM " [TableName] " WHERE " <Condition> 
<Update>          ::=  "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition> 
<Delete>          ::=  "DELETE " "FROM " [TableName] " WHERE " <Condition>
<Join>            ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]
[Digit]           ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
[Uppercase]       ::=  "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
[Lowercase]       ::=  "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
[Letter]          ::=  [Uppercase] | [Lowercase]
[PlainText]       ::=  [Letter] | [Digit] | [PlainText] [Letter] | [PlainText] [Digit]
[Symbol]          ::=  "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
[Space]           ::=  " "
<NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>
<NameValuePair>   ::=  [AttributeName] "=" [Value]
<AlterationType>  ::=  "ADD" | "DROP"
<ValueList>       ::=  [Value] | [Value] "," <ValueList>
[DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]
[IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence] 
[FloatLiteral]    ::=  [DigitSequence] "." [DigitSequence] | "-" [DigitSequence] "." [DigitSequence] | "+" [DigitSequence] "." [DigitSequence]
[BooleanLiteral]  ::=  "TRUE" | "FALSE"
[CharLiteral]     ::=  [Space] | [Letter] | [Symbol] | [Digit]
[StringLiteral]   ::=  "" | [CharLiteral] | [StringLiteral] [CharLiteral]
[Value]           ::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
[TableName]       ::=  [PlainText]
[AttributeName]   ::=  [PlainText]
[DatabaseName]    ::=  [PlainText]
<WildAttribList>  ::=  <AttributeList> | "*"
<AttributeList>   ::=  [AttributeName] | [AttributeName] "," <AttributeList>
<Condition>       ::=  "(" <Condition> ")" | <FirstCondition> <BoolOperator> <SecondCondition> | [AttributeName] <Comparator> [Value]
<FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"
<SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"
<BoolOperator>    ::= "AND" | "OR"
<Comparator>      ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "
```
---

## 🚀 Features

- ✅ Custom SQL-like query language (as per provided BNF)
- ✅ Fully file-backed persistent data storage in `.tab` format
- ✅ Query commands supported:
  - `CREATE`, `USE`, `SELECT`, `INSERT`, `UPDATE`, `ALTER`, `DELETE`, `DROP`, `JOIN`
- ✅ Relational JOINs with composite column display
- ✅ Case-insensitive keywords, whitespace-tolerant parsing
- ✅ Custom **type system** (beyond assignment spec) for:
  - Distinguishing integers, floats, booleans, and strings
  - Type-safe comparisons and operations
- ✅ Graceful error handling with `[ERROR]` and `[OK]` response tags
- ✅ Maven-based test suite with extensible `JUnit` testing

---

## 🧠 Why This Matters

While the assignment focused on implementing a minimal database server, this version goes **beyond the brief**:
- Introduced a **type-aware architecture** for robust comparisons and future extensibility.
- Built all parsing and processing logic manually — no external parser generators.
- Prioritized code clarity and test coverage as part of a production-like design mindset.

---

## 🛠️ Tech Stack

- **Java 17**
- **Maven** for build and test
- **JUnit 5** for automated testing
- No frameworks or database libraries – 100% from-scratch design

---

## 📦 Project Structure

```
src/
├── main/java/edu/uob/         # Core DB engine code (parser, engine, file IO, types)
├── test/java/edu/uob/         # JUnit tests
├── resources/                 # Sample `.tab` data files and configurations
└── databases/                 # File system storage for persistent databases
```

---

## 💡 Getting Started

### 1. 🔧 Compile the Project

```bash
./mvnw clean install
```

### 2. 🖥️ Run the Server

```bash
./mvnw exec:java@server
```

This starts the DBServer on port `8888`, ready to accept SQL-like queries.

### 3. 🧪 Connect the Client

In a separate terminal window:

```bash
./mvnw exec:java@client
```

You can now begin sending SQL-like queries to the server.

---

## 💬 Example Queries

```sql
CREATE DATABASE peopleDB;
USE peopleDB;
CREATE TABLE users (name, age, isStudent);
INSERT INTO users VALUES ('Alice', 22, TRUE);
SELECT * FROM users;
```

```sql
UPDATE users SET age=23 WHERE name=='Alice';
DELETE FROM users WHERE isStudent==FALSE;
DROP TABLE users;
```

### Extended Feature Example – Type-Aware Comparison

```sql
SELECT * FROM users WHERE age > 20;
```

This query uses type inference to perform proper numeric comparison, made possible by the extended type system.

---

## ✅ Testing

Run all test cases:

```bash
./mvnw test
```

The test suite includes:
- Standard query validation (CREATE, INSERT, SELECT, etc.)
- Error message handling and malformed input cases
- Type-specific behavior (custom feature)

---

## 📌 Notes

- All data is stored under the `databases/` directory in `txt` format.
- Server ensures platform independence using `File.separator`.
- Only one client is expected to connect at a time (single-threaded server).
- Responses follow the `[OK]` / `[ERROR]` convention for automated evaluation.

---

## 🧠 Learning Outcomes

This project demonstrates:
- Manual parsing of a formal grammar (BNF-based SQL subset)
- File-based database design
- Command dispatch and interpreter design
- Exception-safe, modular server architecture
- Type-safe computation and operator overloading
- Custom protocol and client-server communication

---

## 📬 Contact

If you're interested in this project or would like to collaborate, feel free to open an [issue](https://github.com/ShrirangL/SQL-Server/issues) or connect via [LinkedIn](https://www.linkedin.com/in/shrirang-lokhande-b402bb15b/).

---
