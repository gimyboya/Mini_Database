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
  : data_statement //select
  | data_change_statement //insert delete update
  | schema_statement //create drop
  ;


data_change_statement
  : insert_statement
  | delete_statement
  | update_statement
  ;

schema_statement
  : create_table_statement
  | drop_table_statement
  ;

create_table_statement
  : CREATE TABLE identifier (LEFT_PAREN field_element (COMMA field_element)* RIGHT_PAREN)?
  ;

field_element
  : name=identifier data_type
  ;

/*
===============================================================================
  11.21 <Drop Table>
===============================================================================
*/

drop_table_statement
  : DROP TABLE identifier
  ;

/*
===============================================================================
  5.2 <token and separator>

  Specifying lexical units (tokens and separators) that participate in SQL language
===============================================================================
*/

identifier
  : Identifier
  | nonreserved_keywords
  ;

nonreserved_keywords
  : INSERT
  | BY
  ;

/*
===============================================================================
  5.3 <literal>
===============================================================================
*/

unsigned_literal
  : unsigned_numeric_literal
  | general_literal
  ;

general_literal
  : Character_String_Literal
  | datetime_literal
  | boolean_literal
  ;

datetime_literal
  : timestamp_literal
  | time_literal
  | date_literal
  ;

time_literal
  : TIME time_string=Character_String_Literal
  ;

timestamp_literal
  : TIMESTAMP timestamp_string=Character_String_Literal
  ;

date_literal
  : DATE date_string=Character_String_Literal
  ;

boolean_literal
  : TRUE | FALSE | UNKNOWN
  ;

/*
===============================================================================
  6.1 <data types>
===============================================================================
*/

data_type
  : character_string_type
  | numeric_type
  | boolean_type
  | datetime_type
  ;

character_string_type
  : CHARACTER type_length?
  | CHAR type_length?
  | VARCHAR type_length?
  | TEXT
  ;

type_length
  : LEFT_PAREN NUMBER RIGHT_PAREN
  ;


numeric_type
  : exact_numeric_type | approximate_numeric_type
  ;

exact_numeric_type
  : NUMERIC
  | DECIMAL
  | DEC
  | INT
  | INTEGER
  ;

approximate_numeric_type
  : FLOAT
  | REAL
  | DOUBLE
  ;


boolean_type
  : BOOLEAN
  | BOOL
  ;

datetime_type
  : DATE
  | TIME
  | TIMESTAMP
  ;


/*
===============================================================================
  6.3 <value_expression_primary>
===============================================================================
*/
value_expression_primary
  : LEFT_PAREN value_expression RIGHT_PAREN
  | nonparenthesized_value_expression_primary
  ;

nonparenthesized_value_expression_primary
  : unsigned_value_specification //strings
  | identifier //column_reference
  | aggregate_function //aggregate functions
  ;

/*
===============================================================================
  6.4 <unsigned value specification>
===============================================================================
*/

unsigned_value_specification
  : unsigned_literal
  ;

unsigned_numeric_literal
  : NUMBER
  | REAL_NUMBER
  ;


/*
===============================================================================
  6.9 <set function specification>

  Invoke an SQL-invoked routine.
===============================================================================
*/

aggregate_function
  : COUNT LEFT_PAREN MULTIPLY RIGHT_PAREN
  | general_set_function
  ;

general_set_function
  : set_function_type LEFT_PAREN DISTINCT? value_expression RIGHT_PAREN
  ;

set_function_type
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
value_expression
  : common_value_expression
  | row_value_expression
  | boolean_value_expression
  ;

common_value_expression
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

numeric_value_expression
  : left=term ((PLUS|MINUS) right=term)*
  ;

term
  : left=factor ((MULTIPLY|DIVIDE|MODULAR) right=factor)*
  ;

factor
  : (PLUS | MINUS)? numeric_primary
  ;

numeric_primary
  : value_expression_primary // deleted the casting
  ;

/*
===============================================================================
  6.28 <string value expression>
===============================================================================
*/

string_value_expression
  : value_expression_primary
  ;

/*
===============================================================================
  6.34 <boolean value expression>
===============================================================================
*/

boolean_value_expression
  : or_predicate
  ;

or_predicate
  : and_predicate (OR or_predicate)*
  ;

and_predicate
  : boolean_factor (AND and_predicate)*
  ;

boolean_factor
  : boolean_test
  | NOT boolean_test
  ;

boolean_test
  : boolean_primary is_clause?
  ;

is_clause
  : IS NOT? t=truth_value //is not = ! | <>
  ;

truth_value
  : TRUE | FALSE | UNKNOWN
  ;

boolean_primary
  : predicate
  | boolean_predicand
  ;

boolean_predicand
  : parenthesized_boolean_value_expression 
  | nonparenthesized_value_expression_primary
  ;

parenthesized_boolean_value_expression
  : LEFT_PAREN boolean_value_expression RIGHT_PAREN
  ;

/*
===============================================================================
  7.2 <row value expression>
===============================================================================
*/
row_value_expression
  : nonparenthesized_value_expression_primary
  | NULL
  ;

row_value_predicand
  : nonparenthesized_value_expression_primary
  | row_value_constructor_predicand
  ;

row_value_constructor_predicand
  : common_value_expression
  | boolean_predicand
  ;

/*
===============================================================================
  7.4 <table expression>
===============================================================================
*/

table_expression
  : from_clause
    where_clause?
    orderby_clause?
  ;

/*
===============================================================================
  7.5 <from clause>
===============================================================================
*/

from_clause
  : FROM table_reference_list
  ;

table_reference_list
  :table_reference (COMMA table_reference)*
  ;

/*
===============================================================================
  7.6 <table reference>
===============================================================================
*/

table_reference
    : identifier
  ;

column_name_list //#
  :  identifier  ( COMMA identifier  )*
  ;


/*
===============================================================================
  7.8 <where clause>
===============================================================================
*/
where_clause
  : WHERE search_condition
  ;

search_condition
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

predicate
  : comparison_predicate
  | null_predicate
  ;

/*
===============================================================================
  8.2 <comparison predicate>

  Specify a comparison of two row values.
===============================================================================
*/
comparison_predicate
  : left=row_value_predicand c=comp_op right=row_value_predicand
  ;

comp_op
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

null_predicate
  : predicand=row_value_predicand IS (n=NOT)? NULL
  ;



/*
===============================================================================
  14.1 <declare cursor>
===============================================================================
*/

orderby_clause
  : ORDER BY sort_specifier_list
  ;

sort_specifier_list
  : sort_specifier (COMMA sort_specifier)*
  ;

sort_specifier
  : key=row_value_predicand order=order_specification? null_order=null_ordering?
  ;

order_specification
  : ASC
  | DESC
  ;


null_ordering
  : NULL FIRST
  | NULL LAST
  ;

/*
===============================================================================
  14.8 <insert statement>
===============================================================================
*/

insert_statement
  : INSERT INTO tb_name=identifier (LEFT_PAREN column_name_list RIGHT_PAREN)? (VALUES LEFT_PAREN insert_value_list RIGHT_PAREN)
  ;

insert_value_list
  : value_expression  ( COMMA value_expression )*
  ;
/*
===============================================================================
  14.8 <delete statement>
===============================================================================
*/

delete_statement
  : DELETE table_expression
  | DELETE qualified_asterisk? from_clause
  ;
/*
===============================================================================
  14.8 <update statement>
===============================================================================
*/
update_statement
  : UPDATE tb_name=identifier SET column_value_expression where_clause?
  ;
column_value_expression
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