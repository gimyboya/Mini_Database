package Pkg;

/**
 * Created by gimy on 12/12/2016.
 */
public class CONTEXT_NODE extends SqlStatementNode{


    public static final int
            SELECT=16,CREATE=3,DELETE=4,
            INSERT=27,UPDATE=20,WHERE=21,
            ORDERBY=14,DISTINCT=6,ASC=2,DESC =5, DROP = 25, MULTIPLY=57;

    private int Context;

    public CONTEXT_NODE(int context){
        this.Context = context;
    }


    public int getType(){
        return SqlStatementNode.CONTEXT_NODE;
    }

    public int getContext(){
        return Context;
    }

}

