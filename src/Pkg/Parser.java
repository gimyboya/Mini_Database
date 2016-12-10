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
            throw new ParserException("Unexpected '"+ LookAhead +"' found");
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
            System.out.println("Missing ';'");
        }
    }

    private void statement(){
        /**
         * statement
         : data_statement
         | data_change_statement
         | schema_statement
         ;
         */
        data_statement();
        data_change_statement();
        schema_statement();

    }

    private void data_statement(){
        /**
         * data_statement
         : SELECT DISTINCT? select_list table_expression?
         ;
         */
        if(LookAhead.tokenCode == Tokenizer.Token.SELECT){ //check if it's a select statement
            if(LookAhead.tokenCode == Tokenizer.Token.DISTINCT){
                nextToken();
                select_list();
                table_expression(); //optional # TODO Think how to implement this
            }else{
                select_list();
                table_expression(); //optional # TODO Think how to implement this
            }
        } //# TODO notice that we are not forcing the exits
    }

    private void select_list(){
        /**
         * select_list
         : select_sublist (COMMA select_sublist)*
         ;
         */
        select_sublist();
        nextToken();
        if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
            select_sublist();
        }

    }

    private void select_sublist(){
        /**
         * select_sublist
         : derived_column
         | qualified_asterisk
         ;
         */
        derived_column();
        qualified_asterisk();

    }

    private void derived_column(){
        /**
         * derived_column
         : value_expression
         ;
         */
        value_expression();
    }

    private void qualified_asterisk(){
        /**
         * qualified_asterisk
         : MULTIPLY
         ;
         */

        if(LookAhead.tokenCode == Tokenizer.Token.COMMA){
            nextToken();
        }else{
            throw new ParserException("Unexpected symbol '"+LookAhead.sequence+"' found");
        }
    }
    private void data_change_statement(){}
    private void schema_statement(){}


}
