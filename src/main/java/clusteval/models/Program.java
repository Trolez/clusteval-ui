package clusteval;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Program {
    private String name;
    //private Map<String, Map<String, String>> parameters;
    private ArrayList<ProgramParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ProgramParameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<ProgramParameter> parameters) {
        this.parameters = parameters;
    }
}
