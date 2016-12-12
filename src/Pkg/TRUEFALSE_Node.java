package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class TRUEFALSE_Node implements SqlStatementNode {

    private boolean truth_value;

    public TRUEFALSE_Node(String truth_value) {
        this.truth_value = Boolean.parseBoolean(truth_value);
    }

    public boolean getTruth_value() {
        return truth_value;
    }

    @Override
    public int getType() {
        return SqlStatementNode.TRUEFALS_Node;
    }
}
