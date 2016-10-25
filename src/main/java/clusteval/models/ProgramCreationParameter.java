package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProgramCreationParameter {
    @Size(min = 1, message = "Please specify a name for the program parameter")
    private String name;

    @Size(min = 1, message = "Please specify a short description for the program parameter")
    private String description;

    @NotNull(message = "Please specify a type for the program parameter")
    private Integer type;

    @Size(min = 1, message = "Please specify a default value for the program parameter")
    private String defaultValue;

    @Size(min = 1, message = "Please specify a minimum value for the program parameter")
    private String minValue;

    @Size(min = 1, message = "Please specify a maximum value for the program parameter")
    private String maxValue;

    private boolean optimizable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public boolean getOptimizable() {
        return optimizable;
    }

    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }
}
