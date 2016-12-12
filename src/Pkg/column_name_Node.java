package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class column_name_Node implements SqlStatementNode {
    private String column_name;

    public column_name_Node(String column_name) {
        this.column_name = column_name;
    }

    public String getColumn_name_Node() {
        return column_name;
    }

    @Override
    public int getType() {
        return SqlStatementNode.column_name_Node;
    }
}
