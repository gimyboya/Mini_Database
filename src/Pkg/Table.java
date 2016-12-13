package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.LinkedHashMap;
import java.util.LinkedList;


/**
 * Created by gimy on 12/12/2016.
 */
public class Table {

    private LinkedHashMap<String, LinkedList> Attributes; // column_name -> all column_values

    //private LinkedHashMap<Object, Tuple> Tuples; // (column_value| search_value) -> row_values


    public Table() {
        Attributes = new LinkedHashMap<>();
    }

    public Table initialize_Columns(LinkedList<String> Column_names){

        while (!Column_names.isEmpty()){
            this.Attributes.put(Column_names.getFirst(), new LinkedList());
            System.out.println("Column " + Column_names.getFirst() + " created!");
            Column_names.pop();

        }

        System.out.println("columns initialized!");
        return this;
    }

    public void Insert_All(LinkedList values){

        if(Attributes.keySet().size() != values.size()){
            throw new ParserException("ERROR: the number of values does not match the number of columns!");
        }
        for (String s : Attributes.keySet()) { //we get the column names
                Attributes.get(s).add(values.getFirst()); // we assign a value
                values.pop();

        }
    }

    public void Insert_Values_With_sp_Columns(LinkedList<String> Column_names, LinkedList values){

        while (!Column_names.isEmpty()){
            if(Attributes.containsKey(Column_names.getFirst())){
                Attributes.get(Column_names.getFirst()) //we get the column name Values
                        .add(values.getFirst()); // we assign a value
            }else {
                throw new ParserException("This Column: " + Column_names.getFirst()+ " does not exist!");
            }
            values.pop();
            Column_names.pop();
        }

    }

    public void update_All_values_in_A_Column(LinkedList<String> Column_names, LinkedList values){

        while (!Column_names.isEmpty()){
            if(Attributes.containsKey(Column_names.getFirst())){
                 int size = Attributes.get(Column_names.getFirst()) //we get the column_name Values (linkedlist)
                        .size(); //we get it's size

                LinkedList temp = new LinkedList();//we create a temporary linked list to populate

                for (int i = 0; i < size; i++) { // we assign the same value many times
                    temp.add(values.getFirst());
                }

                Attributes.get(Column_names.getFirst()) //we get the column_name Values (linkedlist)
                        .add(temp);// we assign the temp linked list as values

            }else {
                throw new ParserException("This Column: " + Column_names.getFirst()+ " does not exist!");
            }
            values.pop();
            Column_names.pop();
        }
    }

}
