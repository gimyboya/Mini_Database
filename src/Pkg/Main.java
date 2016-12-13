package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.ArrayList;
import java.util.Iterator;
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


        if (answer.matches("y|Y")) {


            ArrayList<String> queries;

            fileHandler.openFileR("InputText.text");
            queries = fileHandler.readSql();
            fileHandler.closeFile();

            int size = queries.size();

            for (int i = 0; i < size; i++) {


                try { // this is to ctach the costomized exceptions that w made in our parser and tokenizer and even methods

                    tokenizer.tokenize(queries.get(i));


                    parser.parse(tokenizer.getTokens());// parsing the tokenz to create a parse linkend list


                    if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.CREATE) { // CREATE Statement execution

                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table already exists!");
                        }

                        parser.ParsedNodes.pop();

                        LinkedList<String> Column_names = new LinkedList<>();
                        LinkedList<String> Data_types = new LinkedList<>();


                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.peek().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.peek().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.peek().getType() == SqlStatementNode.Data_type_Node) {
                                Data_types.add(parser.ParsedNodes.peek().get_Data_type());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.creat_table(tb_name, Column_names); // creating the table;;


                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.INSERT) { // INSERT Statement execution

                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: This table does not exist!");
                        } else {
                            parser.ParsedNodes.pop();
                            LinkedList Column_names = new LinkedList();
                            LinkedList values = new LinkedList();

                            int pnsize = parser.ParsedNodes.size();


                            for (int i1 = 0; i1 < pnsize; i1++) {
                                if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                    Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                    parser.ParsedNodes.pop();
                                } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                    values.add(parser.ParsedNodes.getFirst().get_value());
                                    parser.ParsedNodes.pop();
                                }
                            }


                            if (Column_names.isEmpty()) {
                                schema.getTable(tb_name).Insert_All(values);
                                System.out.println("Table has been populated!");
                            } else {
                                if (Column_names.size() != values.size()) {
                                    throw new ParserException("ERROR: Number of values does not match number of columns!");
                                }
                                schema.getTable(tb_name).Insert_Values_With_sp_Columns(Column_names, values); //inserting the values
                                System.out.println("Table has been populated!");
                            }


                        }
                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.DROP) { // DROP Statement execution

                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }

                        schema.PrintTablSheme();
                        parser.ParsedNodes.pop();
                        schema.drop_table(tb_name);
                        System.out.println(tb_name + " has been droped!");
                        schema.PrintTablSheme();

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.UPDATE) { //UPDATE statment
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();

                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }
                        parser.ParsedNodes.pop();

                        LinkedList Column_names = new LinkedList();
                        LinkedList values = new LinkedList();

                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                values.add(parser.ParsedNodes.getFirst().get_value());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.updat_table(tb_name, Column_names, values); //# TODO implement where here

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.SELECT) {
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        System.out.println(parser.ParsedNodes.getFirst().getContext());
                        if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.MULTIPLY) {
                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                            schema.getTable(tb_name).select_All(schema.getTable(tb_name));
                        } else {


                        }

                    } else {

                    }


                } catch (ParserException e) {
                    System.out.println(e.getMessage());
                }

            }

            System.out.println("\n\n\n\n\n\n");
            System.out.println("Notice: Write your query in a single line and do not forget the famous ';' ");

            String statement = input.nextLine();
            String exit;

            do {
                try { // this is to ctach the costomized exceptions that w made in our parser and tokenizer and even methods

                    tokenizer.tokenize(statement);


                    parser.parse(tokenizer.getTokens());// parsing the tokenz to create a parse linkend list


                    if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.CREATE) { // CREATE Statement execution
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table already exists!");
                        }

                        parser.ParsedNodes.pop();

                        LinkedList<String> Column_names = new LinkedList<>();
                        LinkedList<String> Data_types = new LinkedList<>();


                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.peek().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.peek().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.peek().getType() == SqlStatementNode.Data_type_Node) {
                                Data_types.add(parser.ParsedNodes.peek().get_Data_type());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.creat_table(tb_name, Column_names); // creating the table;;


                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.INSERT) { // INSERT Statement execution
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: This table does not exist!");
                        } else {
                            parser.ParsedNodes.pop();
                            LinkedList Column_names = new LinkedList();
                            LinkedList values = new LinkedList();

                            int pnsize = parser.ParsedNodes.size();


                            for (int i1 = 0; i1 < pnsize; i1++) {
                                if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                    Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                    parser.ParsedNodes.pop();
                                } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                    values.add(parser.ParsedNodes.getFirst().get_value());
                                    parser.ParsedNodes.pop();
                                }
                            }


                            if (Column_names.isEmpty()) {
                                schema.getTable(tb_name).Insert_All(values);
                                System.out.println("Table has been populated!");
                            } else {
                                if (Column_names.size() != values.size()) {
                                    throw new ParserException("ERROR: Number of values does not match number of columns!");
                                }
                                schema.getTable(tb_name).Insert_Values_With_sp_Columns(Column_names, values); //inserting the values
                                System.out.println("Table has been populated!");
                            }


                        }
                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.DROP) { // DROP Statement execution
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }

                        schema.PrintTablSheme();
                        parser.ParsedNodes.pop();
                        schema.drop_table(tb_name);
                        System.out.println(tb_name + " has been droped!");
                        schema.PrintTablSheme();

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.UPDATE) { //UPDATE statment
                        System.out.println("\n\n\n\n\n\n");
                        parser.ParsedNodes.pop();

                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }
                        parser.ParsedNodes.pop();

                        LinkedList Column_names = new LinkedList();
                        LinkedList values = new LinkedList();

                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                values.add(parser.ParsedNodes.getFirst().get_value());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.updat_table(tb_name, Column_names, values); //# TODO implement where here

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.SELECT) {
                        parser.ParsedNodes.pop();
                        System.out.println(parser.ParsedNodes.getFirst().getContext());
                        if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.MULTIPLY) {
                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                            schema.getTable(tb_name).select_All(schema.getTable(tb_name));
                        } else {


                        }

                    } else {

                    }


                } catch (ParserException e) {
                    System.out.println(e.getMessage());
                }

                System.out.println("\n\nDo you want to exit?: y");
                exit = input.nextLine();
            } while (!exit.matches("y|Y"));


        } else if (answer.matches("n|N")) {
            //# TODO read the user input and pass it to the tokenizer
            System.out.println("Notice: Write your query in a single line and do not forget the famous ';' ");

            String statement = input.nextLine();
            String exit;

            do {
                try { // this is to ctach the costomized exceptions that w made in our parser and tokenizer and even methods

                    tokenizer.tokenize(statement);


                    parser.parse(tokenizer.getTokens());// parsing the tokenz to create a parse linkend list


                    if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.CREATE) { // CREATE Statement execution

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table already exists!");
                        }

                        parser.ParsedNodes.pop();

                        LinkedList<String> Column_names = new LinkedList<>();
                        LinkedList<String> Data_types = new LinkedList<>();


                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.peek().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.peek().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.peek().getType() == SqlStatementNode.Data_type_Node) {
                                Data_types.add(parser.ParsedNodes.peek().get_Data_type());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.creat_table(tb_name, Column_names); // creating the table;;


                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.INSERT) { // INSERT Statement execution

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: This table does not exist!");
                        } else {
                            parser.ParsedNodes.pop();
                            LinkedList Column_names = new LinkedList();
                            LinkedList values = new LinkedList();

                            int pnsize = parser.ParsedNodes.size();


                            for (int i1 = 0; i1 < pnsize; i1++) {
                                if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                    Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                    parser.ParsedNodes.pop();
                                } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                        parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                    values.add(parser.ParsedNodes.getFirst().get_value());
                                    parser.ParsedNodes.pop();
                                }
                            }


                            if (Column_names.isEmpty()) {
                                schema.getTable(tb_name).Insert_All(values);
                                System.out.println("Table has been populated!");
                            } else {
                                if (Column_names.size() != values.size()) {
                                    throw new ParserException("ERROR: Number of values does not match number of columns!");
                                }
                                schema.getTable(tb_name).Insert_Values_With_sp_Columns(Column_names, values); //inserting the values
                                System.out.println("Table has been populated!");
                            }


                        }
                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.DROP) { // DROP Statement execution
                        System.out.println("\n\n\n\n\n\n");

                        parser.ParsedNodes.pop();
                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();

                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }

                        schema.PrintTablSheme();
                        parser.ParsedNodes.pop();
                        schema.drop_table(tb_name);
                        System.out.println(tb_name + " has been droped!");
                        schema.PrintTablSheme();

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.UPDATE) { //UPDATE statment
                        System.out.println("\n\n\n\n\n\n");
                        parser.ParsedNodes.pop();

                        String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                        if (!schema.hasTable(tb_name)) {
                            throw new ParserException("ERROR: this table does not exist!");
                        }
                        parser.ParsedNodes.pop();

                        LinkedList Column_names = new LinkedList();
                        LinkedList values = new LinkedList();

                        int pnsize = parser.ParsedNodes.size();

                        for (int i1 = 0; i1 < pnsize; i1++) {
                            if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.column_name_Node) {
                                Column_names.add(parser.ParsedNodes.getFirst().getColumn_name());
                                parser.ParsedNodes.pop();
                            } else if (parser.ParsedNodes.getFirst().getType() == SqlStatementNode.signed_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.unsigned_value_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.TRUEFALS_Node ||
                                    parser.ParsedNodes.getFirst().getType() == SqlStatementNode.NULL_Node) {
                                values.add(parser.ParsedNodes.getFirst().get_value());
                                parser.ParsedNodes.pop();
                            }
                        }


                        schema.updat_table(tb_name, Column_names, values); //# TODO implement where here

                    } else if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.SELECT) {
                        parser.ParsedNodes.pop();
                        System.out.println(parser.ParsedNodes.getFirst().getContext());
                        if (parser.ParsedNodes.getFirst().getContext() == Tokenizer.Token.MULTIPLY) {
                            parser.ParsedNodes.pop();
                            String tb_name = parser.ParsedNodes.getFirst().getTb_name();
                            schema.getTable(tb_name).select_All(schema.getTable(tb_name));
                        } else {


                        }

                    } else {

                    }


                } catch (ParserException e) {
                    System.out.println(e.getMessage());
                }

                System.out.println("\n\nDo you want to exit?: y");
                exit = input.nextLine();
            } while (!exit.matches("y|Y"));


        } else {
            System.out.println("Ummmm wired answer ....Please enter (y|Y) or (n|N): ");
            answer = input.nextLine();
        }


    }
}
