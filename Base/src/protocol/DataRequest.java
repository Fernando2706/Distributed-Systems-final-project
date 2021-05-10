package protocol;

import java.util.ArrayList;

public class DataRequest extends Request{
    private ArrayList args;

    public DataRequest(){
        super.type = "DATA_REQUEST";
        this.args = new ArrayList();
    }

    public DataRequest(String subtype) {
        super.type = "DATA_REQUEST";
        super.subtype = subtype;
        this.args = new ArrayList();
    }

    public ArrayList getArgs() {
        return args;
    }
    
    public void setArgs(ArrayList args) {
        this.args = args;
    }
}
