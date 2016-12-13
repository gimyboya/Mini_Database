package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by gimy on 12/8/2016.
 */
public class Parser {

    LinkedList<Tokenizer.Token> TokensToParse;
    Tokenizer.Token LookAhead;

    LinkedList<SqlStatementNode> ParsedNodes = new LinkedList<>();
    Tokenizer.Token lastPoped;

    public void parse(LinkedList<Tokenizer.Token> tokens)
    {
        this.TokensToParse = (LinkedList<Tokenizer.Token>) tokens.clone();
        LookAhead = this.TokensToParse.getFirst();
        System.out.println("LookAhead: " + LookAhead.sequence);

        sql();

        if (LookAhead.tokenCode != Tokenizer.Token.EOF)
            throw new ParserException("Unexpected '"+ LookAhead.sequence +"' found, Expecting EOF");
    }

    private void nextToken()
    {

        lastPoped = TokensToParse.pop();

        System.out.println("Last Popped: " + lastPoped.sequence);

        if (TokensToParse.isEmpty()) //when the input is empty
            LookAhead = new Tokenizer.Token(Tokenizer.Token.EOF, "");//at the end we return an end of line
        else
            LookAhead = TokensToParse.getFirst();
    }


    private void sql(){
        /**
         * sql
         : statement (SEMI_COLON)? EOF
         ;
         */

        statement();

        if(LookAhead.tokenCode == Tokenizer.Token.SEMI_COLON){
           nextToken();
        }else{
            throw new ParserException("ERROR: Missing ';'");
        }
    }

    private int statement(){
        /**
         * statement
         : data_statement
         | data_change_statement
         | schema_statement
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.SELECT){
            select_statement();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.INSERT ||
                 LookAhead.tokenCode == Tokenizer.Token.UPDATE ||
                 LookAhead.tokenCode == Tokenizer.Token.DELETE){
            data_change_statement();
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.CREATE ||
                 LookAhead.tokenCode == Tokenizer.Token.DROP){
            schema_statement();
            return 3;
        }else{
            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting CREATE DROP INSERT DELETE UPDATE SELECT");
        }

    }

    private int select_statement(){
        /**
         * data_statement
         : SELECT DISTINCT? select_list table_expression?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.SELECT){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                nextToken();

                if(     LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                        LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
                    select_list();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an Identifier or an '*'");
                }
                if(LookAhead.tokenCode == Tokenizer.Token.FROM){
                    table_expression();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting FROM keyword");
                }

            }else{

                if(   LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                        LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
                    select_list();
                    if(LookAhead.tokenCode == Tokenizer.Token.FROM){
                        table_expression();
                    }else{
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting FROM keyword");
                    }
                }else{
                    return 0;
                }
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int select_list(){
        /**
         select_list
         : identifier (COMMA identifier)*
         | qualified_asterisk
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){

            while (LookAhead.tokenCode == Tokenizer.Token.Identifier){
                identifier();
                if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                    nextToken();
                }
            }

            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
            nextToken();
            return 2;
        }else{
            return 0;
        }


    }

    private int qualified_asterisk(){
        /**
         * qualified_asterisk
         : MULTIPLY
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
            nextToken();
            return 1;
        }else{
            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting '*' ");
            return 0;
        }
    }

    private int value_expression(){ //# TODO we need to store the values
        /**
         * value_expression
         : var=identifier EQUAL value
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier();
            ParsedNodes.add(new column_name_Node(lastPoped.sequence));
            if(LookAhead.tokenCode == Tokenizer.Token.EQUAL){
                nextToken();
                if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                        LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                        LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                        LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                        LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN||
                        LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                        LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                        LookAhead.tokenCode == Tokenizer.Token.IS ||
                        LookAhead.tokenCode == Tokenizer.Token.NULL){
                    value();
                }else{
                    throw new ParserException("Unexpected '"+ LookAhead +"' found, Expecting a value");
                }
            }else{
                throw new ParserException("Unexpected '"+ LookAhead +"' found, Expecting an assign clause");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int value(){
        /**
         * value
         : unsigned_literal //strings
         | signed_number
         | is_clause
         | NULL//column_reference
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            unsigned_literal();
            ParsedNodes.add(new unsigned_value_Node(lastPoped.sequence));
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS){
            signed_number();
            ParsedNodes.add(new signed_value_Node(Double.parseDouble(lastPoped.sequence)));
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.IS){
            is_clause();
            return 3;
        }else if(LookAhead.tokenCode == Tokenizer.Token.NULL){
            nextToken();
            ParsedNodes.add(new NULL_Node("true"));
            return 4;
        }else{
            return 0;
        }
    }

    private int unsigned_literal(){
        /**
         * unsigned_literal
         : unsigned_numeric_literal
         | general_literal
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER){
            unsigned_numeric_literal(); //number
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            general_literal();//string
            return 2;
        }else{
            return 0;
        }

    }

    private int signed_number(){
        /**
         * signed_number
         : (PLUS | MINUS) unsigned_numeric_literal
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.PLUS ||
        LookAhead.tokenCode == Tokenizer.Token.MINUS){
           unsigned_numeric_literal();
            return 1;
        }else{
            return 0;
        }
    }

    private int unsigned_numeric_literal(){
        /**
         * unsigned_numeric_literal
         : NUMBER
         | REAL_NUMBER
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER){

            nextToken();
            return 1;
        }else{
            System.out.println("Warning: symbol '"+LookAhead.sequence+"' may be Unexpected, Expecting a numerical value");
            return 0;
        }
    }

    private int general_literal(){
        /**
         * general_literal
         : Character_String_Literal
         | boolean_literal
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal){
            nextToken();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            boolean_literal(); //boolean keywords
            return 2;
        }else {
            return 0;
        }
    }

    private int boolean_literal(){
        /**
         * boolean_literal
         : TRUE | FALSE | UNKNOWN
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.TRUE ||
            LookAhead.tokenCode == Tokenizer.Token.FALSE ||
            LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            nextToken();
            return 1;
        }else{
            System.out.println("Expecting general literal");
            return 0;
        }
    }

    private int is_clause(){
        /**
         * is_clause
         : IS NOT? t=truth_value
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.IS){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.NOT){
                nextToken();
                if(     LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                        LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                        LookAhead.tokenCode == Tokenizer.Token.NULL){
                    truth_value();
                    if(lastPoped.tokenCode == Tokenizer.Token.TRUE){
                        ParsedNodes.add(new TRUEFALSE_Node("false"));
                    }else if(lastPoped.tokenCode == Tokenizer.Token.FALSE){
                        ParsedNodes.add(new TRUEFALSE_Node("true"));
                    }else if(lastPoped.tokenCode == Tokenizer.Token.NULL){
                        ParsedNodes.add(new NULL_Node("false"));
                    }
                }else {
                    return 0;
                }
            }else{
                if(     LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                        LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                        LookAhead.tokenCode == Tokenizer.Token.NULL){
                    truth_value();
                    if(lastPoped.tokenCode == Tokenizer.Token.TRUE){
                        ParsedNodes.add(new TRUEFALSE_Node("true"));
                    }else if(lastPoped.tokenCode == Tokenizer.Token.FALSE){
                        ParsedNodes.add(new TRUEFALSE_Node("false"));
                    }else if(lastPoped.tokenCode == Tokenizer.Token.NULL){
                        ParsedNodes.add(new NULL_Node("true"));
                    }
                }else {
                    return 0;
                }
            }
            return 1;
        }else{
            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a truth value: TRUE | FALSE | UNKNOWN | NULL");
        }
    }

    private int truth_value(){
        /**
         * truth_value
         : TRUE | FALSE | UNKNOWN | NULL
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.NULL)
        {
            nextToken();
            return 1;
        }else{
            return 0;
        }

    }

    private int comparison_predicate(){
        /**
         comparison_predicate
         : left=identifier c=comp_op right=value
         | left=identifier right=is_clause
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier(); //# TODO needs to be stored
            if(     LookAhead.tokenCode == Tokenizer.Token.EQUAL ||
                    LookAhead.tokenCode == Tokenizer.Token.NOT_EQUAL ||
                    LookAhead.tokenCode == Tokenizer.Token.LTH ||
                    LookAhead.tokenCode == Tokenizer.Token.LEQ ||
                    LookAhead.tokenCode == Tokenizer.Token.GTH ||
                    LookAhead.tokenCode == Tokenizer.Token.GEQ){
                comp_op();
                if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                        LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                        LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                        LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                        LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN||
                        LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                        LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                        LookAhead.tokenCode == Tokenizer.Token.IS ||
                        LookAhead.tokenCode == Tokenizer.Token.NULL){
                    value();
                }else {
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a value");
                }
            }else if(LookAhead.tokenCode == Tokenizer.Token.IS){
                is_clause();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a comparison or a null value");
            }
            return 1;
        }else{
            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an Identifier");
        }
    }

    private int data_change_statement(){
        /**
         * data_change_statement
         : insert_statement
         | delete_statement
         | update_statement
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.INSERT){
            insert_statement();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.DELETE){
            delete_statement();
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.UPDATE){
            update_statement();
            return 3;
        }else {
           return 0;
        }

    }

    private int schema_statement(){
        /**
         * schema_statement
         : create_table_statement
         | drop_table_statement
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.CREATE){
            create_table_statement();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.DROP){
            drop_table_statement();
            return 2;
        }else {
            return 0;
        }


    }

    private int create_table_statement(){
        /**
         * create_table_statement
         : CREATE TABLE identifier (LEFT_PAREN field_element (COMMA field_element)* RIGHT_PAREN)?
         ;

         */

        if(LookAhead.tokenCode == Tokenizer.Token.CREATE){
            nextToken();
            ParsedNodes.clear();
            ParsedNodes.add(new CONTEXT_NODE(CONTEXT_NODE.CREATE));

            if(LookAhead.tokenCode == Tokenizer.Token.TABLE){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                    identifier();
                    ParsedNodes.add(new tb_name_Node(lastPoped.sequence));
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an identifier after the keyword TABLE");
                }
                if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                    nextToken();

                    if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                        while (LookAhead.tokenCode == Tokenizer.Token.Identifier){
                            field_element();
                            if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                                nextToken();
                            }
                        }
                    }

                    if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                        nextToken();
                    }else {
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
                    }

                }
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting 'TABLE'");
            }
            return 1;
        }else{
            return 0;
        }

    }

    private int identifier(){ //# TODO think how to return the Identifier
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            nextToken();
            return 1;
        }else{
            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an Identifier");
            return 0;
        }
    }

    private int field_element(){
        /**
         * field_element
         : column_name=identifier data_type
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier();
            ParsedNodes.add(new column_name_Node(lastPoped.sequence));

            if(     LookAhead.tokenCode == Tokenizer.Token.CHARACTER ||
                    LookAhead.tokenCode == Tokenizer.Token.CHAR ||
                    LookAhead.tokenCode == Tokenizer.Token.VARCHAR ||
                    LookAhead.tokenCode == Tokenizer.Token.TEXT ||
                    LookAhead.tokenCode == Tokenizer.Token.NUMERIC ||
                    LookAhead.tokenCode == Tokenizer.Token.DECIMAL ||
                    LookAhead.tokenCode == Tokenizer.Token.DEC ||
                    LookAhead.tokenCode == Tokenizer.Token.INT ||
                    LookAhead.tokenCode == Tokenizer.Token.INTEGER ||
                    LookAhead.tokenCode == Tokenizer.Token.FLOAT ||
                    LookAhead.tokenCode == Tokenizer.Token.REAL ||
                    LookAhead.tokenCode == Tokenizer.Token.DOUBLE ||
                    LookAhead.tokenCode == Tokenizer.Token.BOOLEAN ||
                    LookAhead.tokenCode == Tokenizer.Token.BOOL){
                data_type();
            }else {
                System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a data type");
              return 0;
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int data_type(){
        /**
         * data_type
         : character_string_type
         | numeric_type
         | boolean_type
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.CHARACTER ||
                LookAhead.tokenCode == Tokenizer.Token.CHAR ||
                LookAhead.tokenCode == Tokenizer.Token.VARCHAR ||
                LookAhead.tokenCode == Tokenizer.Token.TEXT){
            character_string_type();
            ParsedNodes.add(new Data_type_Node(lastPoped.sequence));
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.NUMERIC ||
                LookAhead.tokenCode == Tokenizer.Token.DECIMAL ||
                LookAhead.tokenCode == Tokenizer.Token.DEC ||
                LookAhead.tokenCode == Tokenizer.Token.INT ||
                LookAhead.tokenCode == Tokenizer.Token.INTEGER ||
                LookAhead.tokenCode == Tokenizer.Token.FLOAT ||
                LookAhead.tokenCode == Tokenizer.Token.REAL ||
                LookAhead.tokenCode == Tokenizer.Token.DOUBLE){
            numeric_type();
            ParsedNodes.add(new Data_type_Node(lastPoped.sequence));
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.BOOLEAN ||
                LookAhead.tokenCode == Tokenizer.Token.BOOL){
            boolean_type();
            ParsedNodes.add(new Data_type_Node(lastPoped.sequence));
            return 3;
        }else{
            return 0;
        }
    }

    private int character_string_type(){
        /**
         * character_string_type
         : CHARACTER type_length?
         | CHAR type_length?
         | VARCHAR type_length?
         | TEXT
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.CHARACTER ||
                LookAhead.tokenCode == Tokenizer.Token.CHAR ||
                LookAhead.tokenCode == Tokenizer.Token.VARCHAR ||
                LookAhead.tokenCode == Tokenizer.Token.TEXT){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                type_length();
            }
            return 1;
        }else{
            return 0;
        }

    }

    private int type_length(){
        /**
         * type_length
         : LEFT_PAREN NUMBER RIGHT_PAREN
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.NUMBER){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                    nextToken();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
                }
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a number for the type length");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int numeric_type(){
        /**
         * numeric_type
         : exact_numeric_type | approximate_numeric_type
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.NUMERIC ||
                LookAhead.tokenCode == Tokenizer.Token.DECIMAL ||
                LookAhead.tokenCode == Tokenizer.Token.DEC ||
                LookAhead.tokenCode == Tokenizer.Token.INT ||
                LookAhead.tokenCode == Tokenizer.Token.INTEGER){
            exact_numeric_type();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.FLOAT ||
                LookAhead.tokenCode == Tokenizer.Token.REAL ||
                LookAhead.tokenCode == Tokenizer.Token.DOUBLE){
            approximate_numeric_type();
            return 2;
        }else{
            return 0;
        }
    }

    private int exact_numeric_type(){
        /**
         * exact_numeric_type
         : NUMERIC
         | DECIMAL
         | DEC
         | INT
         | INTEGER
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NUMERIC ||
                LookAhead.tokenCode == Tokenizer.Token.DECIMAL ||
                LookAhead.tokenCode == Tokenizer.Token.DEC ||
                LookAhead.tokenCode == Tokenizer.Token.INT ||
                LookAhead.tokenCode == Tokenizer.Token.INTEGER){
            nextToken();
            return 1;
        }else{
            return 0;
        }
    }

    private int approximate_numeric_type(){
        /**
         * approximate_numeric_type
         : FLOAT
         | REAL
         | DOUBLE
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.FLOAT ||
                LookAhead.tokenCode == Tokenizer.Token.REAL ||
                LookAhead.tokenCode == Tokenizer.Token.DOUBLE){
            nextToken();
            return 1;
        }else{
            return 0;
        }
    }

    private int boolean_type(){
        /**
         * boolean_type
         : BOOLEAN
         | BOOL
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.BOOLEAN ||
                LookAhead.tokenCode == Tokenizer.Token.BOOL){
            nextToken();
            return 1;
        }else{
            return 0;
        }
    }

    private int comp_op(){
        /**
         * comp_op
         : EQUAL
         | NOT_EQUAL
         | LTH
         | LEQ
         | GTH
         | GEQ
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.EQUAL ||
                LookAhead.tokenCode == Tokenizer.Token.NOT_EQUAL ||
                LookAhead.tokenCode == Tokenizer.Token.LTH ||
                LookAhead.tokenCode == Tokenizer.Token.LEQ ||
                LookAhead.tokenCode == Tokenizer.Token.GTH ||
                LookAhead.tokenCode == Tokenizer.Token.GEQ){
            nextToken();
            return 1;
        }else{
            return 0;
        }

    }

    private int table_expression(){
        /**
         * table_expression
         : from_clause
         where_clause?
         orderby_clause?
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.FROM){
            from_clause();
            if(LookAhead.tokenCode == Tokenizer.Token.WHERE){
                where_clause();
            }
            if(LookAhead.tokenCode == Tokenizer.Token.ORDER){
                orderby_clause();
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int from_clause(){
        /**
         * from_clause
         : FROM identifier
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.FROM){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                identifier();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found after FROM, Expecting an Identifier");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int column_name_list(){
        /**
         * column_name_list
         :  identifier  ( COMMA identifier  )*
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            while (LookAhead.tokenCode == Tokenizer.Token.Identifier){
                identifier();
                ParsedNodes.add(new column_name_Node(lastPoped.sequence));
                if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                    nextToken();
                }
            }
            return 1;
        }else{
            return 0;
        }

    }

    private int where_clause(){
        /**
         * where_clause
         : WHERE comparison_predicate ((AND|OR) comparison_predicate )?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.WHERE){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                comparison_predicate();
                if(     LookAhead.tokenCode == Tokenizer.Token.AND ||
                        LookAhead.tokenCode == Tokenizer.Token.OR){
                    nextToken();
                    if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                        comparison_predicate();
                    }else {
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an Identifier");
                    }
                }
            }else {
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an Identifier");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int orderby_clause(){
        /**
         * orderby_clause
         : ORDER BY sort_specifier
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.ORDER){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.BY){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                    sort_specifier();
                }else{
                    System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an identifier in the ORDER BY clause");
                    return 0;
                }
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting the keyword BY");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int sort_specifier(){ //# TODO we need save the values
        /**
         sort_specifier
         : key=identifier order=order_specification? null_order=null_ordering?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier();
            if(     LookAhead.tokenCode == Tokenizer.Token.ASC ||
                    LookAhead.tokenCode == Tokenizer.Token.DEC){
                order_specification();
            }
            if(LookAhead.tokenCode == Tokenizer.Token.NULL){
                null_ordering();
            }

            if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                throw new ParserException("ERROR: We accept only one sort specifier in the ORDER BY clause");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int order_specification(){
        /**
         * order_specification
         : ASC
         | DESC
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.ASC ||
                LookAhead.tokenCode == Tokenizer.Token.DEC){
            nextToken();
            return 1;
        }else{
            return 0;
        }
    }

    private int null_ordering(){
        /**
         * null_ordering
         : NULL FIRST
         | NULL LAST
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.NULL){
            nextToken();
            if(     LookAhead.tokenCode == Tokenizer.Token.FIRST ||
                    LookAhead.tokenCode == Tokenizer.Token.LAST){
                nextToken();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting FIRST | LAST keyword");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int insert_statement(){// # TODO we may save the info here
        /**
         insert_statement
         : INSERT INTO tb_name=identifier (LEFT_PAREN column_name_list RIGHT_PAREN)? (VALUES LEFT_PAREN insert_value_list RIGHT_PAREN)
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.INSERT){
            nextToken();
            ParsedNodes.clear();
            ParsedNodes.add(new CONTEXT_NODE(CONTEXT_NODE.SELECT));
            if(LookAhead.tokenCode == Tokenizer.Token.INTO){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                    identifier();
                    ParsedNodes.add(new tb_name_Node(lastPoped.sequence));
                    if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                        nextToken();
                        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                            column_name_list();
                            if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                                nextToken();
                            }else {
                                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement, missing ')'");
                            }
                        }else {
                            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement at column names");
                        }
                    }

                    if(LookAhead.tokenCode == Tokenizer.Token.VALUES){
                        nextToken();
                        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                            nextToken();
                            if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                                    LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                                    LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                                    LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                                    LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                                    LookAhead.tokenCode == Tokenizer.Token.UNKNOWN||
                                    LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                                    LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                                    LookAhead.tokenCode == Tokenizer.Token.IS ||
                                    LookAhead.tokenCode == Tokenizer.Token.NULL){
                                insert_value_list();
                                if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                                    nextToken();
                                }else {
                                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement, missing ')'");
                                }
                            }else{
                                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid or missing value(s) in INSERT statement");
                            }
                        }else{
                            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement, missing '('");
                        }

                    }else {
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement, expecting VALUES keyword");
                    }

                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid identifier in INSERT statement");
                }
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid INSERT statement, expecting INTO keyword");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int insert_value_list(){
        /**
         insert_value_list
         : value  ( COMMA value )*
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN||
                LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                LookAhead.tokenCode == Tokenizer.Token.IS ||
                LookAhead.tokenCode == Tokenizer.Token.NULL){
            while ( LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                    LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER ||
                    LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                    LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                    LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                    LookAhead.tokenCode == Tokenizer.Token.UNKNOWN||
                    LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                    LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                    LookAhead.tokenCode == Tokenizer.Token.IS ||
                    LookAhead.tokenCode == Tokenizer.Token.NULL){
                value();
                if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                    nextToken();
                }
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int delete_statement(){
        /**
         * delete_statement
         : DELETE table_expression
         | DELETE qualified_asterisk? from_clause
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.DELETE){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.FROM){
                table_expression();
                return 1;
            }else {
                if(LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
                    qualified_asterisk();
                }
                if(LookAhead.tokenCode == Tokenizer.Token.FROM){
                    from_clause();
                    return 2;
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid DELETE statement expecting FROM keyword");
                }
            }
        }else{
            return 0;
        }
    }

    private int update_statement(){ // # TODO we need to store the data
        /**
         * update_statement
         : UPDATE tb_name=identifier SET column_value_expression where_clause?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.UPDATE){
            nextToken();
            ParsedNodes.clear();
            ParsedNodes.add(new CONTEXT_NODE(CONTEXT_NODE.UPDATE));

            if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                identifier();
                ParsedNodes.add(new tb_name_Node(lastPoped.sequence));

                if(LookAhead.tokenCode == Tokenizer.Token.SET){
                    nextToken();
                    if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                        column_value_expression();

                        if(LookAhead.tokenCode == Tokenizer.Token.WHERE){
                            where_clause();
                        }
                    }else{
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid column value expression in UPDATE statement");
                    }
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid UPDATE statement expecting SET keyword");
                }
            }else {
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid identifier in UPDATE statement");
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int column_value_expression(){
        /**
         * column_value_expression
         : value_expression ( COMMA value_expression)*
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            while (LookAhead.tokenCode == Tokenizer.Token.Identifier){
                value_expression();
                if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
                    nextToken();
                }
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int drop_table_statement(){
        /**
         * drop_table_statement
         : DROP TABLE identifier
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.DROP){
            nextToken();
            ParsedNodes.clear();
            ParsedNodes.add(new CONTEXT_NODE(CONTEXT_NODE.DROP));
            if(LookAhead.tokenCode == Tokenizer.Token.TABLE){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                    identifier();
                    ParsedNodes.add(new tb_name_Node(lastPoped.sequence));
                }
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an 'TABLE'");
            }
            return 1;
        }else {
            return 0;
        }

    }


}
