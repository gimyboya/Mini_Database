package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class signed_value_Node extends SqlStatementNode {

    private Double value;

    public signed_value_Node(Double value) {
        this.value = value;
    }

    public String get_value() {
        return value.toString();
    }

    @Override
    public int getType() {
        return SqlStatementNode.signed_value_Node;
    }
}
