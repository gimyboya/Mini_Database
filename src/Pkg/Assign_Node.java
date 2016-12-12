package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class Assign_Node implements SqlStatementNode {

    private String Assign_op;

    public Assign_Node() {
        this.Assign_op = "=";
    }

//    public String getAssign_op() {
//        return Assign_op;
//    }

    @Override
    public int getType() {
        return SqlStatementNode.Assign_Node;
    }
}
