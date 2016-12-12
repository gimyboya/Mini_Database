package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class NULL_Node implements SqlStatementNode{

    private boolean Null;

    public NULL_Node(String truth) { //here we will make the parser decide in the is_claus()
        Null = Boolean.parseBoolean(truth);
    }

    public boolean isNull() {
        return Null;
    }

    @Override
    public int getType() {
        return SqlStatementNode.NULL_Node;
    }
}
