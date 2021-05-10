package protocol;

import java.io.Serializable;

public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String type;
    protected String subtype;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}