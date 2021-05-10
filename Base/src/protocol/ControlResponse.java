package protocol;

import java.util.ArrayList;

public class ControlResponse extends Response{
    private ArrayList args;

    public ControlResponse(){
        this.type = "CONTROL_RESPONSE";
        this.args = new ArrayList();
    }

    public ControlResponse(String subtype){
        this.type = "CONTROL_RESPONSE";
        this.subtype = subtype;
        this.args = new ArrayList();
    }

    public ArrayList getArgs(){
        return this.args;
    }

    public void setArgs (ArrayList args){
        this.args = args;
    }
}
