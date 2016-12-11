package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.LinkedList;

/**
 * Created by gimy on 12/8/2016.
 */
public class Parser {

    LinkedList<Tokenizer.Token> TokensToParse;
    Tokenizer.Token LookAhead;

    public void parse(LinkedList<Tokenizer.Token> tokens)
    {
        this.TokensToParse = (LinkedList<Tokenizer.Token>) tokens.clone();
        LookAhead = this.TokensToParse.getFirst();
        System.out.println("LookAhead: " + LookAhead.sequence);

        sql();

        if (LookAhead.tokenCode != Tokenizer.Token.EOF)
            throw new ParserException("Unexpected '"+ LookAhead +"' found, Expecting EOF");
    }

    private void nextToken()
    {
        Tokenizer.Token lastPoped;
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

        if(LookAhead.tokenCode == Tokenizer.Token.SEMI_COLON){ //notice here that the semi column is optional
           nextToken();
        }else{
            System.out.println("Warning!: Missing ';'");
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
            data_statement();
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

    private int data_statement(){
        /**
         * data_statement
         : SELECT DISTINCT? select_list table_expression?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.SELECT){ //check if it's a select statement
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                nextToken();

                if(select_list() != 0){ //# TODO
                    select_list();
                }else{
                    return 0;
                }
                if(table_expression() != 0){ //# TODO
                    table_expression();
                }

            }else{

                if(select_list() != 0){
                    select_list();
                }else{
                    return 0;
                }
                if(table_expression() != 0){
                    table_expression();
                }
            }
            return 1;
        }else{
            return 0;
        }
    }


    private int select_list(){
        /**
         * select_list
         : select_sublist (COMMA select_sublist)*
         ;
         */
        if(select_sublist() != 0){ //comma astrix value expression
            do{
                select_sublist();
                nextToken();// # fixme we may actully not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
            return 1;
        }else{
            return 0;
        }


    }

    private int select_sublist(){
        /**
         * select_sublist
         : derived_column
         | qualified_asterisk
         ;
         */
        if (derived_column() != 0) { //value expression
            derived_column();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
            qualified_asterisk();
            return 2;
        }else{
            return 0;
        }

    }

    private int derived_column(){
        /**
         * derived_column
         : value_expression
         ;
         */
        if(value_expression() != 0){
            value_expression();
            return 1;
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

    private int value_expression(){
        /**
         *  value_expression
         : common_value_expression
         | row_value_expression
         | boolean_value_expression
         :
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NULL ||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.MULTIPLY ||
                LookAhead.tokenCode == Tokenizer.Token.DIVIDE ||
                LookAhead.tokenCode == Tokenizer.Token.MODULAR ||
                LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){ //null plus minus multyply devide modular
            common_value_expression();
            return 1;
        }else if(row_value_expression() != 0){
            row_value_expression();
            return 2;
        }else if(boolean_value_expression() != 0){
            boolean_value_expression();
            return 3;
        }else{
            return 0;
        }
    }

    private int common_value_expression(){ //# fixme decision conflict
        /**
         * common_value_expression
         : numeric_value_expression
         | string_value_expression
         | NULL
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.MULTIPLY ||
                LookAhead.tokenCode == Tokenizer.Token.DIVIDE ||
                LookAhead.tokenCode == Tokenizer.Token.MODULAR ||
                LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            numeric_value_expression();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier){
            string_value_expression();
            return 2;
        }else{
            if(LookAhead.tokenCode == Tokenizer.Token.NULL){
                nextToken();
                return 3;
            }else{
                System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting 'NULL' or a value expression");
                return 0;
            }
        }
    }

    private int numeric_value_expression(){
        /**
         * numeric_value_expression
         : left=term ((PLUS|MINUS) right=term)*
         ;
         */
        // # TODO there should be a way to save left and right term
        if(     LookAhead.tokenCode == Tokenizer.Token.MULTIPLY ||
                LookAhead.tokenCode == Tokenizer.Token.DIVIDE ||
                LookAhead.tokenCode == Tokenizer.Token.MODULAR ||
                LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){ //plus minus multyply devide modular
            do{
                term();
                nextToken();// # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.PLUS || LookAhead.tokenCode == Tokenizer.Token.MINUS);
            return 1;
        }else {
            return 0;
        }
    }

    private int term(){
        /**
         * term
         : left=factor ((MULTIPLY|DIVIDE|MODULAR) right=factor)*
         ;
         */
        // # TODO there should be a way to save left and right factor
        if(     LookAhead.tokenCode == Tokenizer.Token.PLUS ||
                LookAhead.tokenCode == Tokenizer.Token.MINUS ||
                LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            do{
                factor();
                nextToken(); // # fixme we may actully not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.MULTIPLY || LookAhead.tokenCode == Tokenizer.Token.DIVIDE || LookAhead.tokenCode == Tokenizer.Token.MODULAR);
            return 1;
        }else {
            return 0;
        }

    }

    private int factor(){
        /**
         factor
         : (PLUS | MINUS)? numeric_primary
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.PLUS || LookAhead.tokenCode == Tokenizer.Token.MINUS){
            nextToken();
            if(     LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                    LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                    LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                    LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                    LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                    LookAhead.tokenCode == Tokenizer.Token.TIME ||
                    LookAhead.tokenCode == Tokenizer.Token.DATE ||
                    LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                    LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                    LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                    LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                    LookAhead.tokenCode == Tokenizer.Token.AVG ||
                    LookAhead.tokenCode == Tokenizer.Token.MAX ||
                    LookAhead.tokenCode == Tokenizer.Token.MIN ||
                    LookAhead.tokenCode == Tokenizer.Token.SUM ||
                    LookAhead.tokenCode == Tokenizer.Token.COUNT){
                numeric_primary();
            }else{
                System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a number");
                return 0;
            }
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            numeric_primary();
            return 1;
        }else{
            return 0;
        }
    }

    private int numeric_primary(){
        /**
         * numeric_primary
         : value_expression_primary // deleted the casting
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            value_expression_primary();
            return 1;
        }else {
            return 0;
        }

    }

    private int string_value_expression(){
        /**
         * string_value_expression
         : value_expression_primary
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN ||
                LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            value_expression_primary();
            return 1;
        }else {
            return 0;
        }

    }

    private int value_expression_primary(){
        /**
         value_expression_primary
         : LEFT_PAREN value_expression RIGHT_PAREN
         | nonparenthesized_value_expression_primary
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            nextToken();
            if(value_expression() != 0){ //# TODO
                value_expression();
            }
            if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                nextToken();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
            }
            return 1;
        }else if (  LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                    LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                    LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                    LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                    LookAhead.tokenCode == Tokenizer.Token.TIME ||
                    LookAhead.tokenCode == Tokenizer.Token.DATE ||
                    LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                    LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                    LookAhead.tokenCode == Tokenizer.Token.UNKNOWN ||
                    LookAhead.tokenCode == Tokenizer.Token.Identifier ||
                    LookAhead.tokenCode == Tokenizer.Token.AVG ||
                    LookAhead.tokenCode == Tokenizer.Token.MAX ||
                    LookAhead.tokenCode == Tokenizer.Token.MIN ||
                    LookAhead.tokenCode == Tokenizer.Token.SUM ||
                    LookAhead.tokenCode == Tokenizer.Token.COUNT){
            nonparenthesized_value_expression_primary();
            return 2;
        }else{
            return 0;
        }



    }

    private int nonparenthesized_value_expression_primary(){
        /**
         *nonparenthesized_value_expression_primary
         : unsigned_value_specification //strings & numbers
         | identifier //column_reference
         | set_function_specification //aggregate functions
         ;

         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            unsigned_value_specification(); //if there is a string or a number
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier(); //if it's a column name
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM ||
                LookAhead.tokenCode == Tokenizer.Token.COUNT){
            aggregate_function(); //if it's a function name
            return 3;
        }else {
            return 0;
        }

    }



    private int unsigned_value_specification(){
        /**
         * unsigned_value_specification
         : unsigned_literal
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.NUMBER ||
                LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER||
                LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            unsigned_literal();
            return 1;
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
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            general_literal();//string
            return 2;
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
         | datetime_literal
         | boolean_literal
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal){
            nextToken();
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP||
                LookAhead.tokenCode == Tokenizer.Token.TIME||
                LookAhead.tokenCode == Tokenizer.Token.DATE){

            datetime_literal(); //date keywords
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
            boolean_literal(); //boolean keywords
            return 3;
        }else {
            return 0;
        }
    }

    private int datetime_literal(){
        /**
         * datetime_literal
         : timestamp_literal
         | time_literal
         | date_literal
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP){
            timestamp_literal();// timestamp keyword
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.TIME){
            time_literal();// time keyword
            return 2;
        }else if(date_literal() != 0){
            date_literal();// dae keyword
            return 3;
        }else{
            return 0;
        }
    }

    private int timestamp_literal(){
        /**
         * timestamp_literal
         : TIMESTAMP timestamp_string=Character_String_Literal
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal){ //# TODO we should save it's value
                nextToken();
            }else{
                System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a valid TIMESTAMP value");
            }
            return 1;
        }else{
            return 0;
        }

    }

    private int time_literal(){
        /**
         * time_literal
         : TIME time_string=Character_String_Literal
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.TIME){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal){ //# TODO we should save it's value
                nextToken();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a valid TIME value");
            }
            return 1;
        }else {
            return 0;
        }
    }

    private int date_literal(){
        /**
         * date_literal
         : DATE date_string=Character_String_Literal
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.DATE){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Character_String_Literal){ //# TODO we should save it's value
                nextToken();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a valid DATE value");
            }
            return 1;
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



    private int aggregate_function(){
        /**
         * aggregate_function
         : COUNT LEFT_PAREN MULTIPLY RIGHT_PAREN
         | general_set_function
         */

        if(LookAhead.tokenCode == Tokenizer.Token.COUNT){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.MULTIPLY){
                    nextToken();
                    if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                        nextToken();
                    }else {
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')' ");
                    }
                }else{
                    if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                        nextToken();
                        if(value_expression() != 0){ //# TODO
                            value_expression();
                        }else{
                            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a value expression");
                            return 0;
                        }
                        if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                            nextToken();
                        }else {
                            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')' ");
                        }
                    }else{
                        if(value_expression() != 0){ //# TODO
                            value_expression();
                        }else {
                            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a value expression");
                            return 0;
                        }
                        if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                            nextToken();
                        }else {
                            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')' ");
                        }
                    }
                }

            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting '(' ");
            }
            return 1;
        }else if(LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM){

            general_set_function();//we omit COUNT from function types as we have fully implemented it here as a special case where is accepts '*'
            return 2;
        }else {
            return 0;
        }
    }

    private int general_set_function(){
        /**
         * general_set_function
         : set_function_type LEFT_PAREN DISTINCT? value_expression RIGHT_PAREN
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.AVG ||
                LookAhead.tokenCode == Tokenizer.Token.MAX ||
                LookAhead.tokenCode == Tokenizer.Token.MIN ||
                LookAhead.tokenCode == Tokenizer.Token.SUM){
            set_function_type();
        }else{
            return 0;
        }
        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                nextToken();
                if(value_expression() != 0){ //# TODO
                    value_expression();
                }else {
                    return 0;
                }
                if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                    nextToken();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
                }
            }else {
                if(value_expression() != 0){ //# TODO
                    value_expression();
                }else {
                    return 0;
                }
                if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                    nextToken();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
                }
            }
        }
        return 1;
    }

    private int set_function_type(){
        /**
         * set_function_type
         : AVG
         | MAX
         | MIN
         | SUM
         | COUNT //will not be implemented as it was already benn done up
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.AVG ||
            LookAhead.tokenCode == Tokenizer.Token.MAX ||
            LookAhead.tokenCode == Tokenizer.Token.MIN ||
            LookAhead.tokenCode == Tokenizer.Token.SUM )
        {
          nextToken();
            return 1;
        }else{
            System.out.println("Warning: symbol '"+LookAhead.sequence+"' may be Unexpected, Expecting a non parenthesized value expression primary value");
            return 0;
        }

    }

    private int row_value_expression(){
        /**
         *row_value_expression
         : nonparenthesized_value_expression_primary
         | NULL
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.NULL){
            nextToken();
            return 1;
        }else if(nonparenthesized_value_expression_primary() != 0){
            nonparenthesized_value_expression_primary();
            return 2;
        }else {
            return 0;
        }

    }

    private int row_value_predicand(){
        /**
         * row_value_predicand
         : nonparenthesized_value_expression_primary
         | row_value_constructor_predicand
         ;
         */
        if(nonparenthesized_value_expression_primary() != 0){
            nonparenthesized_value_expression_primary();
            return 1;
        }else if(row_value_constructor_predicand() != 0){
            row_value_constructor_predicand();
            return 2;
        }else{
            return 0;
        }

    }

    private int row_value_constructor_predicand(){
        /**
         * row_value_constructor_predicand
         : common_value_expression
         | boolean_predicand
         ;
         */
        if(common_value_expression() != 0){
            common_value_expression();
            return 1;
        }else if(boolean_predicand() != 0){
            boolean_predicand();
            return 2;
        }else {
            return 0;
        }

    }


    private int boolean_value_expression(){
        /**
         * boolean_value_expression
         : or_predicate
         ;
         */
        if(or_predicate() != 0){
            or_predicate();
            return 1;
        }else{
            return 0;
        }
    }

    private int or_predicate(){
        /**
         * or_predicate
         : and_predicate (OR or_predicate)*
         ;
         */
        if(and_predicate() != 0){
            and_predicate();
            nextToken();
            while (LookAhead.tokenCode == Tokenizer.Token.OR){
                or_predicate();
            }
            return 1;
        }else{
            return 0;
        }



    }

    private int and_predicate(){
        /**
         * and_predicate
         : boolean_factor (AND and_predicate)*
         ;
         */
        if(boolean_factor() != 0){
            boolean_factor();
            nextToken();
            while (LookAhead.tokenCode == Tokenizer.Token.AND){
                and_predicate();
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int boolean_factor(){
        /**
         * boolean_factor
         : boolean_test
         | NOT boolean_test
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.NOT){
            nextToken();
            if(boolean_test() != 0){
                boolean_test();
            }else{
                return 0;
            }
            return 1;
        }else if(boolean_test() != 0){
            boolean_test();
            return 2;
        }else{
           return 0;
        }
    }

    private int boolean_test(){
        /**
         * boolean_test
         : boolean_primary is_clause?
         ;
         */
        if(boolean_primary() != 0){
            boolean_primary();
            if(LookAhead.tokenCode == Tokenizer.Token.IS){
                is_clause();
            }
            return 1;
        }else{
            return 0;
        }

    }

    private int is_clause(){ // is
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
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
                    truth_value();
                }else {
                    return 0;
                }
            }else{
                if(     LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                        LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                        LookAhead.tokenCode == Tokenizer.Token.UNKNOWN){
                    truth_value();
                }else {
                    return 0;
                }
            }
            return 1;
        }else{
            //# TODO parser exception error maybe needed here
            return 0;
        }
    }

    private int truth_value(){
        /**
         * truth_value
         : TRUE | FALSE | UNKNOWN
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.TRUE ||
                LookAhead.tokenCode == Tokenizer.Token.FALSE ||
                LookAhead.tokenCode == Tokenizer.Token.UNKNOWN )
        {
            nextToken();
            return 1;
        }else{
            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a truth value: TRUE | FALSE | UNKNOWN"); // # // FIXME: 12/11/2016 no exception thrown
            return 0;
        }

    }

    private int boolean_primary(){
        /**
         * boolean_primary
         : predicate
         | boolean_predicand
         ;
         */
        if(predicate() != 0){
            predicate();
            return  1;
        }else if(boolean_predicand() != 0){
            boolean_predicand();
            return 2;
        }else{
            return 0;
        }

    }

    private int boolean_predicand(){
        /**
         * boolean_predicand
         : parenthesized_boolean_value_expression
         | nonparenthesized_value_expression_primary
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            parenthesized_boolean_value_expression();
            return 1;
        }else if(nonparenthesized_value_expression_primary() != 0){
            nonparenthesized_value_expression_primary();
            return 2;
        }else {
            return 0;
        }

    }

    private int parenthesized_boolean_value_expression(){
        /**
         * parenthesized_boolean_value_expression
         : LEFT_PAREN boolean_value_expression RIGHT_PAREN
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            nextToken();
            if(boolean_value_expression() != 0){ //# TODO
                boolean_value_expression();
            }else{
                return 0;
            }
            if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                nextToken();
            }else {
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
            }
            return 1;
        }else {
            return 0;
        }
    }

    private int predicate(){
        /**
         * predicate
         : comparison_predicate
         | null_predicate
         ;
         */
        if(comparison_predicate() != 0){
            comparison_predicate();
            return 1;
        }else if(null_predicate() != 0){
            null_predicate();
            return 2;
        }else{
            return 0;
        }
    }

    private int comparison_predicate(){ //# TODO check return values
        /**
         * comparison_predicate
         : left=row_value_predicand c=comp_op right=row_value_predicand
         ;
         */
        if(row_value_predicand() != 0){
            row_value_predicand(); //# TODO needs to be stored
            if(     LookAhead.tokenCode == Tokenizer.Token.EQUAL ||
                    LookAhead.tokenCode == Tokenizer.Token.NOT_EQUAL ||
                    LookAhead.tokenCode == Tokenizer.Token.LTH ||
                    LookAhead.tokenCode == Tokenizer.Token.LEQ ||
                    LookAhead.tokenCode == Tokenizer.Token.GTH ||
                    LookAhead.tokenCode == Tokenizer.Token.GEQ){
                comp_op();
                if(row_value_predicand() != 0){
                    row_value_predicand();
                }else {
                    System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a row value predicand");
                    return 0;
                }
            }else{
                return 0;
            }
            return 1;
        }else{
            return 0;
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
        if(insert_statement() != 0){
            insert_statement();
            return 1;
        }else if(delete_statement() != 0){
            delete_statement();
            return 2;
        }else if(update_statement() != 0){
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
        if(create_table_statement() != 0){
            create_table_statement();
            return 1;
        }else if(drop_table_statement() != 0){
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
            if(LookAhead.tokenCode == Tokenizer.Token.TABLE){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                    identifier();
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
         : name=identifier data_type
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
            identifier(); //# TODO identifier needs to be stored somewhere maybe the function should return a Token
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
                    LookAhead.tokenCode == Tokenizer.Token.BOOL ||
                    LookAhead.tokenCode == Tokenizer.Token.DATE ||
                    LookAhead.tokenCode == Tokenizer.Token.TIME ||
                    LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP
                    ){
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
         | datetime_type
         ;
         */

        if(     LookAhead.tokenCode == Tokenizer.Token.CHARACTER ||
                LookAhead.tokenCode == Tokenizer.Token.CHAR ||
                LookAhead.tokenCode == Tokenizer.Token.VARCHAR ||
                LookAhead.tokenCode == Tokenizer.Token.TEXT){
            character_string_type();
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
            return 2;
        }else if(LookAhead.tokenCode == Tokenizer.Token.BOOLEAN ||
                LookAhead.tokenCode == Tokenizer.Token.BOOL){
            boolean_type();
            return 3;
        }else if(LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP){
            datetime_type();
            return 4;
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

    private int datetime_type(){
        /**
         * datetime_type
         : DATE
         | TIME
         | TIMESTAMP
         ;
         */
        if(     LookAhead.tokenCode == Tokenizer.Token.DATE ||
                LookAhead.tokenCode == Tokenizer.Token.TIME ||
                LookAhead.tokenCode == Tokenizer.Token.TIMESTAMP){
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

    private int null_predicate(){ //# TODO we may need to store the values
        /**
         * null_predicate
         : predicand=row_value_predicand IS (n=NOT)? NULL
         ;
         */
        if(row_value_predicand() != 0){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.IS){
                nextToken();
                if(LookAhead.tokenCode == Tokenizer.Token.NOT){
                    nextToken();
                    if(LookAhead.tokenCode == Tokenizer.Token.NULL){
                       nextToken();
                    }else{
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting NULL");
                    }
                }else{
                    if(LookAhead.tokenCode == Tokenizer.Token.NULL){
                        nextToken();
                    } else{
                        throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting NULL");
                    }
                }
            }
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

        if(from_clause() != 0){
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
         : FROM table_reference_list
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.FROM){
            nextToken();
            if(table_reference_list() != 0){
                table_reference_list();
            }else{
                return 0;
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int table_reference_list(){
        /**
         * table_reference_list
         :table_reference (COMMA table_reference)*
         ;
         */
        if(table_reference() != 0){
            do{
                table_reference();
                nextToken();// # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
            return 1;
        }else{
            return 0;
        }
    }

    private int table_reference(){
        /**
         * table_reference
         : identifier
         ;
         */

        if(identifier() != 0){
            identifier();
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
        if(identifier() != 0){
            do{
                identifier();
                nextToken();// # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
            return 1;
        }else{
            return 0;
        }

    }

    private int where_clause(){
        /**
         * where_clause
         : WHERE search_condition
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.WHERE){
            nextToken();
            if(search_condition() != 0){
                search_condition();
            }else {
                return 0;
            }
            return 1;
        }else{
            return 0;
        }
    }

    private int search_condition(){
        /**
         *search_condition
         : value_expression
         ;
         */

        if(value_expression() != 0){
            return 1;
        }else{
            return 0;
        }
    }

    private int orderby_clause(){
        /**
         * orderby_clause //#
         : ORDER BY sort_specifier_list
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.ORDER){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.BY){
                nextToken();
                if(sort_specifier_list() != 0){
                    sort_specifier_list();
                }else{
                    System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a valid sort specifier list");
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

    private int sort_specifier_list(){
        /**
         * sort_specifier_list
         : sort_specifier (COMMA sort_specifier)*
         ;
         */

        if(sort_specifier() != 0){
            do{
                sort_specifier();
                nextToken();// # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
            return 1;
        }else{
            return 0;
        }
    }

    private int sort_specifier(){ //# TODO we may save the values
        /**
         * sort_specifier
         : key=row_value_predicand order=order_specification? null_order=null_ordering?
         ;
         */
        if(row_value_predicand() != 0){
            row_value_predicand();
            if(order_specification() != 0){
                order_specification();
            }
            if(null_ordering() != 0){
                null_ordering();
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
         * insert_statement
         : INSERT INTO tb_name=identifier (LEFT_PAREN column_name_list RIGHT_PAREN)? (VALUES LEFT_PAREN insert_value_list RIGHT_PAREN)
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.INSERT){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.INTO){
                nextToken();
                if(identifier() != 0){
                    identifier();
                    if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
                        nextToken();
                        if(column_name_list() != 0){
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
                            if(insert_value_list() != 0){
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
         * insert_value_list
         : value_expression  ( COMMA value_expression )*
         ;
         */
        if(value_expression() != 0){
            do{
                value_expression();
                nextToken();// # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
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
            if(table_expression() != 0){
                table_expression();
                return 1;
            }else {
                if(qualified_asterisk() != 0){
                    qualified_asterisk();
                }
                if(from_clause() != 0){
                    from_clause();
                    return 2;
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"', Invalid DELETE statement");
                }
            }
        }else{
            return 0;
        }
    }

    private int update_statement(){
        /**
         * update_statement
         : UPDATE tb_name=identifier SET column_value_expression where_clause?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.UPDATE){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.Identifier){
                identifier();
                if(LookAhead.tokenCode == Tokenizer.Token.SET){
                    nextToken();
                    if(column_value_expression() != 0){
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
        if(value_expression() != 0){
            do{
                value_expression();
                nextToken(); // # fixme we may actually not need this
            }while (LookAhead.tokenCode == Tokenizer.Token.COMMA);
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
            if(LookAhead.tokenCode == Tokenizer.Token.TABLE){
                nextToken();
                if(identifier() != 0){
                    identifier();
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
