# Mini SQL Database:

## Introduction:

To implement anything related to a database we need first to get an input as an SQL statement then we should be able to validate that input and give it a logical meaning so that it can be processed. 

## Implementation:

In our small program we have crafted the following procedures:

```
Input --> Tokeniz ---> Parse ---> Excute a logic (or sometimes not "Syntax Error") according to the result of the previos steps
```



from the above explanation as expected we have a `public class Tokenizer` and a `public class Parser`

each of those have many methods to allow them to work properly.

But what about the input ? well we have two options:

1. From a text file (recommended) where the program load a file then execute all the statements that are inside that file then give the chance to the user to enter any statement he wants.
2. The second one is simply letting the user enter the queries by hand.

Before talking about the the execution of the queries we have to talk about the data structures behind the tables, well this is a DSA Class after all :)

we have implemented a `public class Schema` which works as Schema for our database in it we defined 

a `HashMap<String, Table > TableSheme = new HashMap<>();` this is to allow fast retrieval of the tables where the keys are the names and the values are actual tables.

then comes the  `public class Table` where we defined `private LinkedHashMap<String, LinkedList> Attributes;` 

we chose this structure as it will keep the natural order of insertion we judged that this might be important

this LinkedHashMap will contain a single column name as a keys to a Linked list of values



we should also mention that our parser will create an Abstract LinkedList Syntax `LinkedList<SqlStatementNode> ParsedNodes = new LinkedList<>();` where instead of a normal parse tree we only populate our LinkedList with nodes that are needed to the excution of the SQL commands

this will not affect anything as our parser will check for syntax errors and will only create nodes that are needed for a logical excution of the statment 

we also check the statments during the excution for example: doese the table exist or not or the column exist or not etc...

## Grammar and Parser:

### Missing features:

So far non :D

### Syntax Rules:

Comments are **not supported.**

Double quotes syntax is **not supported**.

This syntax in table names is **not supported**: `Customers.CustomerName | c.CustomerName`  as it will make the parser tree bigger in size we decided to skip it.

Subqueries are **not supported.** (As requested)

`DISTINCT ` is not allowed with the `DELETE` command.

Input **can** be separated by a space or escape characters. Our tokenizer trims those.

String literals **accept** escape characters.

Concatenation is **not supported**.

Exponent of the following Pattern in real numbers is **not supported**: `([eE][-+]?[0-9]+)` example:

 `e-10 | E6`.

The following operators are **not supported**:

```
Similar_To : ~
Not_Similar_To : !~
Similar_To_Case_Insensitive : ~*
Not_Similar_To_Case_Insensitive : !~*
```

Only Underscore:`_` and digits:`[0-9]` are allowed in identifiers but not at the beginning of it.

The command `ORDER BY` accept one value only as this syntax is not accepted:

```
SELECT * FROM Weather ORDER BY City, Date
```



###  Added features:

Order specification: `DESC, ASC` 

Comparators ( = | (><| != | ~= | ^=) | < | <=| > | >= )

----
## Statements Execution:

So far our program can execute the following commands:

- [x] **CREAT TABLE**
- [x] **DROP TABLE**
- [x] **INSERT INTO** 
      - [x] inserting a single complete row. (all columns)
      - [x] inserting a single partial row. (selected columns)
- [x] **UPDATE** 
      - [x] updating all the values in the column
      - [ ] update specific cells in the column (requires WHERE query).
- [x] **SELECT **
      - [x] *
      - [x] specific columns order not important


- [ ] **WHERE**
- [ ] **DISTINCT**
- [ ] **DELETE**
- [ ] **ORDER BY**
- [ ] **AND & OR**
- [ ] comparators ( = | (><| != | ~= | ^=) | < | <=| > | >= )
- [x] checking the schema.
- [ ] allowing the user to type the statements on multiple lines
- [ ] making a data type checking including length checking
- [ ] Implementing primary key features

###  Added features:

Requirements are not all completed  yet.
