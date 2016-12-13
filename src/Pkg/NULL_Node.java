package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class NULL_Node extends SqlStatementNode{

    private boolean Null;

    public NULL_Node(String truth) { //here we will make the parser decide in the is_claus()
        Null = Boolean.parseBoolean(truth);
    }

    public String get_value() {
        if(Null){
            return "NULL";
        }else{
            return "NOT NULL";
        }

    }

    @Override
    public int getType() {
        return SqlStatementNode.NULL_Node;
    }
}
