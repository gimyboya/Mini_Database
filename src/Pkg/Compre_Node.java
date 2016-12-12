package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class Compre_Node implements SqlStatementNode{
    private String comp_op;

    public Compre_Node(String comp_op) {
        this.comp_op = comp_op;
    }

    public String getComp_op() {
        return comp_op;
    }

    @Override
    public int getType() {
        return SqlStatementNode.Compare_Node;
    }
}
