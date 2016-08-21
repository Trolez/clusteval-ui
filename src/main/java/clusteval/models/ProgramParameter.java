package clusteval;

import java.util.ArrayList;

public class ProgramParameter {
    private String name;
    private ArrayList<ProgramParameterOption> defaultOptions;
    private String value;
    private String minValue;
    private String maxValue;
    private String options;
    private boolean optimizable = false;
    private boolean optimize = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ProgramParameterOption> getDefaultOptions() {
        return defaultOptions;
    }

    public void setDefaultOptions(ArrayList<ProgramParameterOption> defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
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
