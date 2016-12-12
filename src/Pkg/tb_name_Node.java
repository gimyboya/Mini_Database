package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class tb_name_Node implements SqlStatementNode {

    private String tb_name;

    public tb_name_Node(String tb_name) {
        this.tb_name = tb_name;
    }

    public String getTb_name() {
        return tb_name;
    }

    @Override
    public int getType() {
        return SqlStatementNode.tb_name_Node;
    }
}
