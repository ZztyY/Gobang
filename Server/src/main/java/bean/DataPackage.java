package bean;

import java.io.Serializable;

public class DataPackage implements Serializable {
    private String instruction;
    private String message;

    public DataPackage(String instruction, String message) {
        this.instruction = instruction;
        this.message = message;
    }

    public String getInstruction() {
        return this.instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
