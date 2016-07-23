package clusteval;

import java.util.ArrayList;

public class ProgramParameter {
    private String name;
    private ArrayList<ProgramParameterOption> options;
    private String value;
    private boolean optimizable = false;
    private boolean optimize = true;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean getOptimizable() {
        return optimizable;
    }

    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    public boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }
}
