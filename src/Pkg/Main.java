package Pkg;

import jdk.nashorn.internal.runtime.ParserException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        FileHandler fileHandler = new FileHandler();

        Tokenizer tokenizer = new Tokenizer();

        fileHandler.openFile("TokensInfos.txt");// open the file
        fileHandler.readFromFile(tokenizer);// add the information to the linked list
        fileHandler.closeFile();//close the file
        






    }
}
