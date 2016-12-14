package Pkg;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.*;


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

        String[] All_Column_nams = Attributes.keySet().toArray(new String[Attributes.keySet().size()]);



        if(Attributes.keySet().size() != Column_names.size()){ //if not all the columns has been mentioned

            for (int i = 0; i < All_Column_nams.length ; i++) {

                if(All_Column_nams[i].matches(Column_names.getFirst())){
                    Attributes.get(Column_names.getFirst()) //we get the column name Values
                            .add(values.getFirst()); // we assign a single value
                    System.out.println("Value (" + values.getFirst() + ") inserted in column: "+ Column_names.getFirst());
                    values.pop();
                    Column_names.pop();
                }else{
                    Attributes.get(All_Column_nams[i]) //we get the column name from the array
                            .add("NULL"); // we assign null
                }

            }

        }else{
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

                Attributes.get(Column_names.getFirst()).clear();// we clear all the values

                for (int i = 0; i < size; i++) {
                    Attributes.get(Column_names.getFirst()).add(i, values.getFirst()); //we get the column_name Values (linkedlist) and we assign the temp linked list as values
                }



            }else {
                throw new ParserException("This Column: " + Column_names.getFirst()+ " does not exist!");
            }
            System.out.print("updated The Column " + Column_names.getFirst()+ " to the vale: " + values.getFirst() );
            values.pop();
            Column_names.pop();
        }
    }

    public void select_All(Table table){

        String[] Column_names = table.Attributes.keySet().toArray(new String[table.Attributes.keySet().size()]);

        int numberofrows = table.Attributes.get(Column_names[0]).size();


        for (String column_name : Column_names) {
            System.out.print("|\t" + column_name + "\t|");

        }

        System.out.println("|\n");
        for (int i = 0; i < numberofrows; i++) {
            for (int i1 = 0; i1 < Column_names.length; i1++) {
                System.out.print(" " + table.Attributes.get(Column_names[i1]).get(i) + "\t");
            }

            System.out.println("\n");
        }


    }

    public void select_Column(Table table, LinkedList<String> Column_names){


        int numberofrows = table.Attributes.get(Column_names.getFirst()).size();


        for (String column_name : Column_names) {
            System.out.print("|\t" + column_name + "\t|");

        }

        System.out.println("|\n");

        for (int i = 0; i < numberofrows; i++) {
            for (int i1 = 0; i1 < Column_names.size(); i1++) {
                System.out.print(" " + table.Attributes.get(Column_names.get(i1)).get(i) + "\t");
            }

            System.out.println("\n");
        }


    }

    public int numberOfattributes(){
        return this.Attributes.size();
    }

    public int number_of_Rows(){

        Object[] values = this.Attributes.keySet().toArray();
        return this.Attributes.get(values[0]).size();
    }

    public LinkedHashMap<String, LinkedList> getAttributes() {
        return Attributes;
    }
}
