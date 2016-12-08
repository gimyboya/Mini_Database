package Pkg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;

/**
 * Created by gimy on 12/8/2016.
 */
public class FileHandler {

    private Formatter pen;
    private Scanner scanner;
    private File file;

    public void openFileR (String fileName){ // to open a file for reading only
        try{
            this.file = new File(fileName);

            if (this.file.createNewFile()){
                System.out.println("Warning! File doesn't exist. A blank file has been created!");
            }else{
                System.out.println("File open successfully!.");
            }

            this.scanner = new Scanner(file);
        }
        catch (IOException e){}
    }

    public void readTokens (Tokenizer tokenizer){ //read the tokens information from the file

        while(scanner.hasNext()){

            try{

                tokenizer.add(scanner.next(), scanner.nextInt());
            }
            catch (IndexOutOfBoundsException e){

            }

        }

    }

    public void openFileW (String fileName){ // to open a file for writing only
        try{
            pen = new Formatter(new BufferedWriter(new FileWriter(fileName, true)));
        }
        catch (Exception e){

            System.out.println("Warning! File doesn't exist.");
        }
    }

    public void closeFile (){
        this.scanner.close();
    }

}
