package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;


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
                        .add(values.getFirst()); // we assign a single value
                System.out.println("Value (" + values.getFirst() + ") inserted in column: "+ Column_names.getFirst());
            }else {
                throw new ParserException("This Column: " + Column_names.getFirst()+ " does not exist!");
            }
            values.pop();
            Column_names.pop();
        }

        System.out.println("Table populated successfully!");
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
            System.out.print("all the values in The Column " + Column_names.getFirst()+ " has been updated to the vale: " + values.getFirst() );
            values.pop();
            Column_names.pop();
        }
    }

    public void select_All(Table table){
        
        Set<String> Column_names = table.Attributes.keySet();
        System.out.println("The set: " + Column_names);
        for (String column_name : Column_names) {
            System.out.print("||" + column_name + "\t||");
            System.out.println("\n");
            LinkedList c_values;
            c_values = (LinkedList) table.Attributes.get(column_name).clone();

            for (Object c_value : c_values) {
                System.out.println(c_value);
            }
        }
        

    }

}
