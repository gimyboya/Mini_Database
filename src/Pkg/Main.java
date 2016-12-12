package Pkg;

import jdk.nashorn.internal.runtime.ParserException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        FileHandler fileHandler = new FileHandler();

        Tokenizer tokenizer = new Tokenizer();

        fileHandler.openFileR("TokensRegex+Code.tokens");// open the file
        fileHandler.readTokens(tokenizer);// add the information to the linked list
        fileHandler.closeFile();//close the file


        try
        {
            tokenizer.tokenize("");

            Parser parser = new Parser();
            parser.parse(tokenizer.getTokens());

            for (int i = 0; i < tokenizer.getTokens().size(); i++) {

               // System.out.println("" + tokenizer.getTokens().get(i).sequence + " " + tokenizer.getTokens().get(i).tokenCode);
            }

        }
        catch (ParserException e)
        {
            System.out.println(e.getMessage());
        }



    }
}
