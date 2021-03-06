package clusteval;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Program {
    private String name;

    @HasOneOptimizationParameter(message = "Please select at least one parameter to optimize")
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

    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Program)) return false;

        Program otherProgram = (Program)other;

        return name.equals(otherProgram.getName());
    }
}
