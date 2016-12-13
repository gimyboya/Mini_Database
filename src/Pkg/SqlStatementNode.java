package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class SqlStatementNode {
    public static final int
            CONTEXT_NODE=1,tb_name_Node=2, column_name_Node =3,Assign_Node=4,
            Compare_Node=5, TRUEFALS_Node =6, ANDOR_Node =7,
            Data_type_Node = 8, unsigned_value_Node = 9, signed_value_Node = 9, NULL_Node = 10;

    public int getType(){return 0;}
    public int getContext(){return 0;}
    public String getTb_name(){return null;}
    public String getColumn_name(){return null;}
    public String get_value(){return null;}
    public String get_Data_type(){return null;}
}