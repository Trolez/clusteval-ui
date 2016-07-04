package clusteval;

import java.util.ArrayList;

public class Randomizer {
    private String name;
    private ArrayList<Parameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<Parameter> parameters) {
        this.parameters = parameters;
    }
}
