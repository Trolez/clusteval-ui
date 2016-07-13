package clusteval;

import java.util.ArrayList;

public class ProgramParameter {
    private String name;
    private ArrayList<ProgramParameterOption> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ProgramParameterOption> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<ProgramParameterOption> options) {
        this.options = options;
    }
}
