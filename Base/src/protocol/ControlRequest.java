package protocol;

import java.util.ArrayList;

public class ControlRequest extends Request{
    private static final long serialVersionUID = 1L;
    private ArrayList args;

    public ControlRequest() {
        super.type = "CONTROL_REQUEST";
        this.args = new ArrayList();
    }

    public ControlRequest(String subtype) {
        super.type = "CONTROL_REQUEST";
        super.subtype = subtype;
        this.args = new ArrayList();
    }

    public ArrayList getArgs() {
        return this.args;
    }

    public void setArgs(ArrayList args) {
        this.args = args;
    }
}
