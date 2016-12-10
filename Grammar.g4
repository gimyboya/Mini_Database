grammar Grammar;

options {language=Java;}

@header {
}

@members {
}


/*===============================================================================
  SQL statement (Start Symbol)
===============================================================================
*/

sql
  : statement (SEMI_COLON)? EOF
  ;

statement
  : data_statement
  | data_change_statement
  | schema_statement
  ;


data_change_statement //# TODO
  : insert_statement
  | delete_statement
  | update_statement
  ;

schema_statement //# TODO
  : create_table_statement
  | drop_table_statement
  ;

create_table_statement //# TODO
  : CREATE TABLE identifier (table_elements)?
  ;

table_elements //# TODO
  : LEFT_PAREN field_element (COMMA field_element)* RIGHT_PAREN
  ;

field_element //# TODO
  : name=identifier field_type
  ;

field_type //# TODO
  : data_type
  ;


/*
===============================================================================
  11.21 <Drop Table>
===============================================================================
*/

drop_table_statement //# TODO
  : DROP TABLE identifier
  ;

/*
===============================================================================
  5.2 <token and separator>

  Specifying lexical units (tokens and separators) that participate in SQL language
===============================================================================
*/

identifier //# TODO
  : Identifier
  | nonreserved_keywords
  ;

nonreserved_keywords //# TODO
  : INSERT
  | BY
  ;

/*
===============================================================================
  5.3 <literal>
===============================================================================
*/

unsigned_literal //# TODO
  : unsigned_numeric_literal
  | general_literal
  ;

general_literal //# TODO
  : Character_String_Literal
  | datetime_literal
  | boolean_literal
  ;

datetime_literal //# TODO
  : timestamp_literal
  | time_literal
  | date_literal
  ;

time_literal //# TODO
  : TIME time_string=Character_String_Literal
  ;

timestamp_literal //# TODO
  : TIMESTAMP timestamp_string=Character_String_Literal
  ;

date_literal //# TODO
  : DATE date_string=Character_String_Literal
  ;

boolean_literal //# TODO
  : TRUE | FALSE | UNKNOWN
  ;

/*
===============================================================================
  6.1 <data types>
===============================================================================
*/

data_type //# TODO
  : character_string_type
  | numeric_type
  | boolean_type
  | datetime_type
  ;

character_string_type //# TODO
  : CHARACTER type_length?
  | CHAR type_length?
  | VARCHAR type_length?
  | TEXT
  ;

type_length //# TODO
  : LEFT_PAREN NUMBER RIGHT_PAREN
  ;


numeric_type //# TODO
  : exact_numeric_type | approximate_numeric_type
  ;

exact_numeric_type //# TODO
  : NUMERIC
  | DECIMAL
  | DEC
  | INT
  | INTEGER
  ;

approximate_numeric_type //# TODO
  : FLOAT
  | REAL
  | DOUBLE
  ;


boolean_type //# TODO
  : BOOLEAN
  | BOOL
  ;

datetime_type //# TODO
  : DATE
  | TIME
  | TIMESTAMP
  ;


/*
===============================================================================
  6.3 <value_expression_primary>
===============================================================================
*/
value_expression_primary //# TODO
  : parenthesized_value_expression
  | nonparenthesized_value_expression_primary
  ;

parenthesized_value_expression //# TODO
  : LEFT_PAREN value_expression RIGHT_PAREN
  ;

nonparenthesized_value_expression_primary //# TODO
  : unsigned_value_specification //numbers
  | signed_numerical_literal // added signed numbers
  | identifier //column_reference
  | set_function_specification //aggregate functions
  ;

/*
===============================================================================
  6.4 <unsigned value specification>
===============================================================================
*/

unsigned_value_specification //# TODO
  : unsigned_literal
  ;

unsigned_numeric_literal //# TODO
  : NUMBER
  | REAL_NUMBER
  ;

signed_numerical_literal //# TODO
  : sign? unsigned_numeric_literal
  ;

/*
===============================================================================
  6.9 <set function specification>

  Invoke an SQL-invoked routine.
===============================================================================
*/
set_function_specification //# TODO
  : aggregate_function
  ;

aggregate_function //# TODO
  : COUNT LEFT_PAREN MULTIPLY RIGHT_PAREN
  | general_set_function
  ;

general_set_function //# TODO
  : set_function_type LEFT_PAREN DISTINCT? value_expression RIGHT_PAREN
  ;

set_function_type //# TODO
  : AVG
  | MAX
  | MIN
  | SUM
  | COUNT
  ;

/*
===============================================================================
  6.25 <value expression>
===============================================================================
*/
value_expression //# TODO
  : common_value_expression
  | row_value_expression
  | boolean_value_expression
  ;

common_value_expression //# TODO
  : numeric_value_expression
  | string_value_expression
  | NULL
  ;

/*
===============================================================================
  6.26 <numeric value expression>

  Specify a comparison of two row values.
===============================================================================
*/

numeric_value_expression //# TODO
  : left=term ((PLUS|MINUS) right=term)*
  ;

term //# TODO
  : left=factor ((MULTIPLY|DIVIDE|MODULAR) right=factor)*
  ;

factor //# TODO
  : (sign)? numeric_primary
  ;

numeric_primary //# TODO
  : value_expression_primary // deleted the casting
  ;

sign //# TODO
  : PLUS | MINUS
  ;

/*
===============================================================================
  6.28 <string value expression>
===============================================================================
*/

string_value_expression //# TODO
  : value_expression_primary
  ;

/*
===============================================================================
  6.34 <boolean value expression>
===============================================================================
*/

boolean_value_expression //# TODO
  : or_predicate
  ;

or_predicate //# TODO
  : and_predicate (OR or_predicate)*
  ;

and_predicate //# TODO
  : boolean_factor (AND and_predicate)*
  ;

boolean_factor //# TODO
  : boolean_test
  | NOT boolean_test
  ;

boolean_test //# TODO
  : boolean_primary is_clause?
  ;

is_clause //# TODO
  : IS NOT? t=truth_value //is not = ! | <>
  ;

truth_value //# TODO
  : TRUE | FALSE | UNKNOWN
  ;

boolean_primary //# TODO
  : predicate
  | boolean_predicand
  ;

boolean_predicand //# TODO
  : parenthesized_boolean_value_expression 
  | nonparenthesized_value_expression_primary
  ;

parenthesized_boolean_value_expression //# TODO
  : LEFT_PAREN boolean_value_expression RIGHT_PAREN
  ;

/*
===============================================================================
  7.2 <row value expression>
===============================================================================
*/
row_value_expression //# TODO
  : row_value_special_case
  | explicit_row_value_constructor
  ;

row_value_special_case //# TODO
  : nonparenthesized_value_expression_primary
  ;

explicit_row_value_constructor //# TODO
  : NULL
  ;

row_value_predicand //# TODO
  : row_value_special_case
  | row_value_constructor_predicand
  ;

row_value_constructor_predicand //# TODO
  : common_value_expression
  | boolean_predicand
  ;

/*
===============================================================================
  7.4 <table expression>
===============================================================================
*/

table_expression //# TODO
  : from_clause
    where_clause?
    orderby_clause?
  ;

/*
===============================================================================
  7.5 <from clause>
===============================================================================
*/

from_clause //# TODO
  : FROM table_reference_list
  ;

table_reference_list //# TODO
  :table_reference (COMMA table_reference)*
  ;

/*
===============================================================================
  7.6 <table reference>
===============================================================================
*/

table_reference //# TODO
    : identifier
  ;

column_name_list //# TODO
  :  identifier  ( COMMA identifier  )*
  ;


/*
===============================================================================
  7.8 <where clause>
===============================================================================
*/
where_clause //# TODO
  : WHERE search_condition
  ;

search_condition //# TODO
  : value_expression // instead of boolean_value_expression, we use value_expression for more flexibility.
  ;

/*
===============================================================================
  7.13 <query expression>
===============================================================================
*/

data_statement
  : SELECT DISTINCT? select_list table_expression?
  ;

select_list
  : select_sublist (COMMA select_sublist)*
  ;

select_sublist
  : derived_column
  | qualified_asterisk
  ;

derived_column
  : value_expression
  ;

qualified_asterisk
  : MULTIPLY
  ;

/*
===============================================================================
  8.1 <predicate>
===============================================================================
*/

predicate //# TODO
  : comparison_predicate
  | null_predicate
  ;

/*
===============================================================================
  8.2 <comparison predicate>

  Specify a comparison of two row values.
===============================================================================
*/
comparison_predicate //# TODO
  : left=row_value_predicand c=comp_op right=row_value_predicand
  ;

comp_op //# TODO
  : EQUAL
  | NOT_EQUAL
  | LTH
  | LEQ
  | GTH
  | GEQ
  ;

/*
===============================================================================
  8.7 <null predicate>

  Specify a test for a null value.
===============================================================================
*/

null_predicate //# TODO
  : predicand=row_value_predicand IS (n=NOT)? NULL
  ;



/*
===============================================================================
  14.1 <declare cursor>
===============================================================================
*/

orderby_clause //# TODO
  : ORDER BY sort_specifier_list
  ;

sort_specifier_list //# TODO
  : sort_specifier (COMMA sort_specifier)*
  ;

sort_specifier //# TODO
  : key=row_value_predicand order=order_specification? null_order=null_ordering?
  ;

order_specification //# TODO
  : ASC
  | DESC
  ;


null_ordering //# TODO
  : NULL FIRST
  | NULL LAST
  ;

/*
===============================================================================
  14.8 <insert statement>
===============================================================================
*/

insert_statement //# TODO
  : INSERT INTO tb_name=identifier (LEFT_PAREN column_name_list RIGHT_PAREN)? (VALUES LEFT_PAREN insert_value_list RIGHT_PAREN)?
  ;

insert_value_list //# TODO
  : value_expression  ( COMMA value_expression )*
  ;
/*
===============================================================================
  14.8 <delete statement>
===============================================================================
*/

delete_statement //# TODO
  : DELETE table_expression
  | DELETE qualified_asterisk? from_clause
  ;
/*
===============================================================================
  14.8 <update statement>
===============================================================================
*/
update_statement //# TODO
  : UPDATE tb_name=identifier SET column_value_expression where_clause?
  ;
column_value_expression //# TODO
  : value_expression ( COMMA value_expression)*
  ;

/*
===============================================================================
  Tokens for Case Insensitive Keywords
===============================================================================
*/
fragment A
    :	'A' | 'a';

fragment B
    :	'B' | 'b';

fragment C
    :	'C' | 'c';

fragment D
    :	'D' | 'd';

fragment E
    :	'E' | 'e';

fragment F
    :	'F' | 'f';

fragment G
    :	'G' | 'g';

fragment H
    :	'H' | 'h';

fragment I
    :	'I' | 'i';

fragment J
    :	'J' | 'j';

fragment K
    :	'K' | 'k';

fragment L
    :	'L' | 'l';

fragment M
    :	'M' | 'm';

fragment N
    :	'N' | 'n';

fragment O
    :	'O' | 'o';

fragment P
    :	'P' | 'p';

fragment Q
    :	'Q' | 'q';

fragment R
    :	'R' | 'r';

fragment S
    :	'S' | 's';

fragment T
    :	'T' | 't';

fragment U
    :	'U' | 'u';

fragment V
    :	'V' | 'v';

fragment W
    :	'W' | 'w';

fragment X
    :	'X' | 'x';

fragment Y
    :	'Y' | 'y';

fragment Z
    :	'Z' | 'z';

/*
===============================================================================
  Reserved Keywords
===============================================================================
*/

AND : A N D;
ASC : A S C;

CREATE : C R E A T E;

DELETE: D E L E T E;
DESC : D E S C;
DISTINCT : D I S T I N C T;


FALSE : F A L S E;
FROM : F R O M;

INTO : I N T O;
IS : I S;



LEFT : L E F T;

NOT : N O T;
NULL : N U L L;

OR : O R;
ORDER : O R D E R;
RIGHT : R I G H T;
SELECT : S E L E C T;
SET: S E T;

TABLE : T A B L E;

TRUE : T R U E;

UPDATE: U P D A T E;

WHERE : W H E R E;

/*
===============================================================================
  Non Reserved Keywords
===============================================================================
*/
AVG : A V G;

BY : B Y;


CHARACTER : C H A R A C T E R;


COUNT : C O U N T;


DEC : D E C;


DROP : D R O P;


FIRST : F I R S T;


INSERT : I N S E R T;


LAST : L A S T;

MAX : M A X;
MIN : M I N;


SUM : S U M;

UNKNOWN : U N K N O W N;

VALUES : V A L U E S;


/*
===============================================================================
  Data Type Tokens
===============================================================================
*/
BOOLEAN : B O O L E A N;
BOOL : B O O L;


INT : I N T; // alias for INT4
INTEGER : I N T E G E R; // alias - INT4

REAL : R E A L; // alias for FLOAT4
FLOAT : F L O A T; // alias for FLOAT8
DOUBLE : D O U B L E; // alias for FLOAT8

NUMERIC : N U M E R I C;
DECIMAL : D E C I M A L; // alias for number

CHAR : C H A R;
VARCHAR : V A R C H A R;

DATE : D A T E;
TIME : T I M E;
TIMESTAMP : T I M E S T A M P;

TEXT : T E X T;


ASSIGN  : ':=';
EQUAL  : '=';
COLON :  ':';
SEMI_COLON :  ';';
COMMA : ',';
NOT_EQUAL  : '<>' | '!=' | '~='| '^=' ;
LTH : '<' ;
LEQ : '<=';
GTH   : '>';
GEQ   : '>=';
LEFT_PAREN :  '(';
RIGHT_PAREN : ')';
PLUS  : '+';
MINUS : '-';
MULTIPLY: '*';
DIVIDE  : '/';
MODULAR : '%';
DOT : '.';
UNDERLINE : '_';
VERTICAL_BAR : '|';
QUOTE : '\'';

NUMBER : Digit+;

fragment
Digit : '0'..'9';

REAL_NUMBER
    :   ('0'..'9')+ '.' ('0'..'9')*
    |   '.' ('0'..'9')+
    ;

/*
===============================================================================
 Identifiers
===============================================================================
*/

Identifier
  : Regular_Identifier
  ;

fragment
Regular_Identifier
  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|Digit|'_')*
  ;
 /*
=========================
string literal
=========================
*/

Character_String_Literal
    : QUOTE ( ~('\\'|'\'') )* QUOTE
    ;