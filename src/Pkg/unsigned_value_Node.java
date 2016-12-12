package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class unsigned_value_Node implements SqlStatementNode{
    private String unsigned_value;

    public unsigned_value_Node(String unsigned_value) {
        this.unsigned_value = unsigned_value;
    }

    public String getUnsigned_value() {
        return unsigned_value;
    }

    @Override
    public int getType() {
        return SqlStatementNode.unsigned_value_Node;
    }
}
