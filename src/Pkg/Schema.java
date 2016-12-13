package Pkg;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by gimy on 12/13/2016.
 */
public class Schema {
    HashMap<String, Table > TableSheme = new HashMap<>(); // tb_name -> table

    public Schema() {
        TableSheme = new HashMap<>();
    }

    public void creat_table(String tb_name, LinkedList<String> Column_names){
        TableSheme.put(tb_name, new Table().initialize_Columns(Column_names));
        System.out.println("Table '" + tb_name + "' created!");
    }


    public void drop_table(String tb_name){
        TableSheme.remove(tb_name);
        System.out.println("Table '" + tb_name + "' deleted!");
    }

    public void updat_table(String tb_name, LinkedList<String> Column_names, LinkedList values){

        TableSheme.get(tb_name).update_All_values_in_A_Column(Column_names,values);
        System.out.print(" in table" + tb_name );
    }

    public void updat_table_where(){}

    public Table getTable(String tb_name){
        return TableSheme.get(tb_name);
    }
    public void PrintTablSheme(){
        System.out.println("This schema contains: " + TableSheme.size() + " Table(s)");
        for(String tb_name: TableSheme.keySet()){
            System.out.println(tb_name);
        }
    }

    public boolean hasTable(String tb_name){
        if(TableSheme.containsKey(tb_name)){
            return true;
        }else {
            return false;
        }
    }

}
