package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        FileHandler fileHandler = new FileHandler();

        Tokenizer tokenizer = new Tokenizer();// creating a new tokenizer
        Parser parser = new Parser(); // creating a new parser
        Schema schema = new Schema();// creating a schema

        fileHandler.openFileR("TokensRegex+Code.tokens");// open the file
        fileHandler.readTokens(tokenizer);// add the information to the linked list
        fileHandler.closeFile();//close the file

        System.out.println("Do you want use the file \"InputTest.txt\" as an input?: y-n");
        Scanner input = new Scanner(System.in);
        String answer = input.nextLine();



        do{
            if(answer.matches("y|Y")){
                // # TODO read the inputText file by the filehandler and give it to the tokenizer

                ArrayList<String> queries = new ArrayList<>();

                fileHandler.openFileR("InputText.text");
                queries = fileHandler.readSql();
                fileHandler.closeFile();

                for (int i = 0; i < queries.size(); i++) {
                    tokenizer.tokenize(queries.get(i));

                    try
                    {
                        tokenizer.tokenize("UPDATE Weather SET AverageTemperature = 20;");


                        System.out.println("the sql query has been broken into tokenz : ");
                        for (int j = 0; j < tokenizer.getTokens().size(); i++) {

                            System.out.print(tokenizer.getTokens().get(j).sequence + "=" + tokenizer.getTokens().get(j).tokenCode + ", ");
                        }


                        parser.parse(tokenizer.getTokens());// parsing the tokenz to create a parse linkend list

                        System.out.println(parser.ParsedNodes);



                        if(parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.CREATE){ // CREATE Statement execution

                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                            if(schema.hasTable(tb_name)){
                                throw new ParserException("ERROR: this table already exists!");
                            }

                            parser.ParsedNodes.pop();

                            LinkedList<String> Column_names = new LinkedList<>();
                            LinkedList<String> Data_types = new LinkedList<>();
                            for (SqlStatementNode parsedNode : parser.ParsedNodes) {
                                if(parsedNode.getType() == SqlStatementNode.column_name_Node){
                                    Column_names.add(parsedNode.getColumn_name());
                                    parser.ParsedNodes.pop();
                                }else if(parsedNode.getType() == SqlStatementNode.Data_type_Node){
                                    Data_types.add(parsedNode.get_Data_type());
                                    parser.ParsedNodes.pop();
                                }
                            }



                            schema.creat_table(tb_name,Column_names); // creating the table;;



                        }else if(parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.INSERT){ // INSERT Statement execution
                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                            if(!schema.hasTable(tb_name)){
                                throw new ParserException("ERROR: This table does not exist!");
                            }else {
                                parser.ParsedNodes.pop();
                                LinkedList Column_names = new LinkedList();
                                LinkedList values = new LinkedList();
                                for (SqlStatementNode parsedNode : parser.ParsedNodes) {
                                    if(parsedNode.getType() == SqlStatementNode.column_name_Node){
                                        Column_names.add(parsedNode.getColumn_name());
                                        parser.ParsedNodes.pop();
                                    }else if(parsedNode.getType() == SqlStatementNode.signed_value_Node ||
                                            parsedNode.getType() == SqlStatementNode.unsigned_value_Node ||
                                            parsedNode.getType() == SqlStatementNode.TRUEFALS_Node ||
                                            parsedNode.getType() == SqlStatementNode.NULL_Node){
                                        values.add(parsedNode.get_value());
                                        parser.ParsedNodes.pop();
                                    }
                                }

                                if(Column_names.isEmpty()){
                                    schema.getTable(tb_name).Insert_All(values);
                                }else{
                                    if(Column_names.size() != values.size()){
                                        throw new ParserException("ERROR: Number of values does not match number of columns!");
                                    }
                                    schema.getTable(tb_name).Insert_Values_With_sp_Columns(Column_names,values); //inserting the values
                                    System.out.println("Table has been populated!");
                                }


                            }
                        }else if(parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.DROP){ // DROP Statement execution

                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                            if(!schema.hasTable(tb_name)){
                                throw new ParserException("ERROR: this table does not exist!");
                            }

                            parser.ParsedNodes.pop();
                            schema.drop_table(tb_name);

                        }else if(parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.UPDATE){ //update statment
                            parser.ParsedNodes.pop();

                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                            if(!schema.hasTable(tb_name)){
                                throw new ParserException("ERROR: this table does not exist!");
                            }
                            parser.ParsedNodes.pop();

                            LinkedList Column_names = new LinkedList();
                            LinkedList values = new LinkedList();
                            for (SqlStatementNode parsedNode : parser.ParsedNodes) {
                                if(parsedNode.getType() == SqlStatementNode.column_name_Node){
                                    Column_names.add(parsedNode.getColumn_name());
                                    parser.ParsedNodes.pop();
                                }else if(parsedNode.getType() == SqlStatementNode.signed_value_Node ||
                                        parsedNode.getType() == SqlStatementNode.unsigned_value_Node ||
                                        parsedNode.getType() == SqlStatementNode.TRUEFALS_Node ||
                                        parsedNode.getType() == SqlStatementNode.NULL_Node){
                                    values.add(parsedNode.get_value());
                                    parser.ParsedNodes.pop();
                                }
                            }

                            schema.updat_table(tb_name,Column_names,values); //# TODO implement where here

                        }else if(parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.SELECT){

                        }



                    }
                    catch (ParserException e)
                    {
                        System.out.println(e.getMessage());
                    }

                }


            }else if(answer.matches("n|N")){
                //# TODO read the user input and pass it to the tokenizer
            }else{
                System.out.println("Please enter (y|Y) or (n|N): ");
                answer = input.nextLine();
            }

        }while (!answer.matches("y|Y|n|N"));



    }
}
