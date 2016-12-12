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
  : CREATE TABLE tb_name=identifier (LEFT_PAREN field_element (COMMA field_element)* RIGHT_PAREN)?
  ;

field_element
  : column_name=identifier data_type
  ;

/*
===============================================================================
  11.21 <Drop Table>
===============================================================================
*/

drop_table_statement
  : DROP TABLE tb_name=identifier
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
  | boolean_literal
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
  ;

character_string_type
  : type=CHARACTER type_length?
  | type=CHAR type_length?
  | type=VARCHAR type_length?
  | type=TEXT
  ;

type_length
  : LEFT_PAREN v=NUMBER RIGHT_PAREN
  ;


numeric_type
  : exact_numeric_type | approximate_numeric_type
  ;

exact_numeric_type
  : type=NUMERIC
  | type=DECIMAL
  | type=DEC
  | type=INT
  | type=INTEGER
  ;

approximate_numeric_type
  : type=FLOAT
  | type=REAL
  | type=DOUBLE
  ;


boolean_type
  : type=BOOLEAN
  | type=BOOL
  ;

/*
===============================================================================
  6.3 <value_expression_primary>
===============================================================================
*/

value
  : v=unsigned_literal //strings
  | v=signed_number
  | v=is_clause
  | v=truth_value//column_reference
  ;

/*
===============================================================================
  6.4 <unsigned value specification>
===============================================================================
*/

unsigned_numeric_literal
  : NUMBER
  | REAL_NUMBER
  ;


signed_number
    : (PLUS | MINUS) unsigned_numeric_literal
    ;
/*
===============================================================================
  6.25 <value expression>
===============================================================================
*/
value_expression
  : column_name=identifier EQUAL v=value
  ;

/*
===============================================================================
  6.26 <numeric value expression>

  Specify a comparison of two row values.
===============================================================================
*/

/*
===============================================================================
  6.28 <string value expression>
===============================================================================
*/


/*
===============================================================================
  6.34 <boolean value expression>
===============================================================================
*/

is_clause
  : IS NOT? t=truth_value
  ;

truth_value
  : TRUE | FALSE | UNKNOWN | NULL
  ;


/*
===============================================================================
  7.2 <row value expression>
===============================================================================
*/

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
  : FROM tb_name=identifier
  ;

/*
===============================================================================
  7.6 <table reference>
===============================================================================
*/

column_name_list //#
  :  column_name=identifier  ( COMMA column_name=identifier  )*
  ;


/*
===============================================================================
  7.8 <where clause>
===============================================================================
*/
where_clause
  : WHERE comparison_predicate (c=(AND|OR) comparison_predicate )?
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
  : column_name=identifier (COMMA column_name=identifier)*
  | qualified_asterisk
  ;


qualified_asterisk
  : column_name=MULTIPLY
  ;

/*
===============================================================================
  8.1 <predicate>
===============================================================================
*/


/*
===============================================================================
  8.2 <comparison predicate>

  Specify a comparison of two row values.
===============================================================================
*/
comparison_predicate
  : column_name=identifier c=comp_op right=value
  | column_name=identifier right=is_clause
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



/*
===============================================================================
  14.1 <declare cursor>
===============================================================================
*/

orderby_clause
  : ORDER BY sort_specifier
  ;


sort_specifier
  : column_name=identifier order=order_specification? null_order=null_ordering?
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
  : value  ( COMMA value )*
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

BY : B Y;


CHARACTER : C H A R A C T E R;


DEC : D E C;


DROP : D R O P;


FIRST : F I R S T;


INSERT : I N S E R T;


LAST : L A S T;


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