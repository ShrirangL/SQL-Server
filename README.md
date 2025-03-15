This is an implementation of limited SQL server in java based on specifications provided in BNF below:

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

The assignment is provided as part of marked coursework for Object Oriented Programming in Java by University of Bristol.
The original requiremnets did not require the implementation of types checking for columns of a table but I have implemented it.
First column is autogeneted primary key of type integer which is non-recyclable after deleteion of entries from a table.
Error handling has been implemented to help the user with what needs to be corrected in the query to get the desired result.
Unit test are incorporated to check the accuracy and performance of SQL server.
