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

        sql();

        if (LookAhead.tokenCode != Tokenizer.Token.EOF)
            throw new ParserException("Unexpected '"+ LookAhead +"' found, Expecting EOF");
    }

    private void nextToken()
    {
        TokensToParse.pop();

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

        nextToken();//we take the next token and check if it's a semi column
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
        if(data_statement() != 0){
            data_statement();
            return 1;
        }else if(data_change_statement() != 0){
            data_change_statement();
            return 2;
        }else if(schema_statement() != 0){
            schema_statement();
            return 3;
        }else{
            return 0;
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
                select_list(); //# TODO check the returned value
                //if not end of statement
                if(LookAhead.tokenCode != Tokenizer.Token.EOF || LookAhead.tokenCode != Tokenizer.Token.SEMI_COLON){
                    table_expression();
                }
            }else{
                select_list(); //# TODO check the returned value
                //if not end of statement
                if(LookAhead.tokenCode != Tokenizer.Token.EOF || LookAhead.tokenCode != Tokenizer.Token.SEMI_COLON){
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
        if(select_sublist() != 0){
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
        if (derived_column() != 0) {
            derived_column();
            return 1;
        }else if(qualified_asterisk() != 0){
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
        if(common_value_expression() != 0){
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

    private int common_value_expression(){
        /**
         * common_value_expression
         : numeric_value_expression
         | string_value_expression
         | NULL
         ;
         */
        if(numeric_value_expression() != 0){
            numeric_value_expression();
            return 1;
        }else if(string_value_expression() != 0){
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
        if(term() != 0){
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
        if(factor() != 0){
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
            if(numeric_primary() != 0){
                numeric_primary();
            }else{
                System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a number");
                return 0;
            }
            return 1;
        }else if(numeric_primary() != 0){
            numeric_primary();
            return 2;
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
        if(value_expression_primary() != 0){
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
        if(value_expression_primary() != 0){
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
            if(value_expression() != 0){
                value_expression();
            }
            if(LookAhead.tokenCode == Tokenizer.Token.RIGHT_PAREN){
                nextToken();
            }else{
                throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting ')'");
            }
            return 1;
        }else if (nonparenthesized_value_expression_primary() != 0){
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
         | signed_numerical_literal // numbers
         | identifier //column_reference
         | set_function_specification //aggregate functions
         ;

         */
        if(unsigned_value_specification() != 0){
            unsigned_value_specification(); //if there is a string or a number
            return 1;
        }else if(signed_numerical_literal() != 0){
            signed_numerical_literal(); //if +|-
            return 2;
        }else if(identifier() != 0){
            identifier(); //if it's a column name
            return 3;
        }else if(aggregate_function() != 0){
            aggregate_function(); //if it's a function name
            return 4;
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
        if(unsigned_literal() != 0){
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

        if(unsigned_numeric_literal() != 0){
            unsigned_numeric_literal(); //number
            return 1;
        }else if(general_literal() != 0){
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
        if(LookAhead.tokenCode == Tokenizer.Token.NUMBER || LookAhead.tokenCode == Tokenizer.Token.REAL_NUMBER){
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
        }else if(datetime_literal() != 0){
            datetime_literal(); //date keywords
            return 2;
        }else if(boolean_literal() != 0){
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
        if(timestamp_literal() != 0){
            timestamp_literal();// timestamp keyword
            return 1;
        }else if(time_literal() != 0){
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


    private int signed_numerical_literal(){
        /**
         * signed_numerical_literal
         : (PLUS | MINUS) unsigned_numeric_literal
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.PLUS || LookAhead.tokenCode == Tokenizer.Token.MINUS){
            nextToken();
            if(unsigned_numeric_literal() != 0){
                unsigned_numeric_literal();
            }
            return 1;
        }else {
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
                        if(value_expression() != 0){
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
                        if(value_expression() != 0){
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
        }else if(general_set_function() != 0){

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
        if(set_function_type() != 0){
            set_function_type();
        }else{
            return 0;
        }
        if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){
            nextToken();
            if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                nextToken();
                if(value_expression() != 0){
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
                if(value_expression() != 0){
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
                if(truth_value() != 0){
                    truth_value();
                }else {
                    return 0;
                }
            }else{
                if(truth_value() != 0){
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
        if(parenthesized_boolean_value_expression() != 0){
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
            if(boolean_value_expression() != 0){
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

    private int comparison_predicate(){
        /**
         * comparison_predicate
         : left=row_value_predicand c=comp_op right=row_value_predicand
         ;
         */
        if(row_value_predicand() != 0){
            row_value_predicand(); //# TODO needs to be stored
            if(comp_op != 0){
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
                if(identifier() != 0){
                    identifier();
                    nextToken();
                }else{
                    throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting an identifier after the keyword TABLE");
                }
                if(LookAhead.tokenCode == Tokenizer.Token.LEFT_PAREN){

                    do{
                        if(field_element() != 0){
                            field_element();
                        }else{
                            System.out.println("Unexpected symbol '"+LookAhead.sequence+"' found, Expecting a field element");
                            return 0;
                        }
                        nextToken();
                    } while (LookAhead.tokenCode == Tokenizer.Token.COMMA);

                    if(LookAhead.tokenCode != Tokenizer.Token.RIGHT_PAREN){
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
        if(identifier() != 0){
            identifier(); //# TODO identifier needs to be stored somewhere maybe the function should return a Token
            return 1;
        }else if(data_type() != 0){
            data_type();
            return 2;
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
