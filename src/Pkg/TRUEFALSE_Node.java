package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class TRUEFALSE_Node extends SqlStatementNode {

    private Boolean truth_value;

    public TRUEFALSE_Node(String truth_value) {
        this.truth_value = Boolean.parseBoolean(truth_value);
    }

    public String get_value() {
        return truth_value.toString();
    }

    @Override
    public int getType() {
        return SqlStatementNode.TRUEFALS_Node;
    }
}
