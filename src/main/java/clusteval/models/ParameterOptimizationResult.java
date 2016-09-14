package clusteval;

import java.util.ArrayList;

public class ParameterOptimizationResult {
    private String name;

    private ArrayList<ParameterOptimizationResultProgram> programs = new ArrayList<ParameterOptimizationResultProgram>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ParameterOptimizationResultProgram> getPrograms() {
        return programs;
    }

    public void setPrograms(ArrayList<ParameterOptimizationResultProgram> programs) {
        this.programs = programs;
    }

    public void addToPrograms(ParameterOptimizationResultProgram program) {
        programs.add(program);
    }
}
