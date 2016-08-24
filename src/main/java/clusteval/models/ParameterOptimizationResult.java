package clusteval;

import java.util.ArrayList;

public class ParameterOptimizationResult {
    private ArrayList<ParameterOptimizationResultProgram> programs = new ArrayList<ParameterOptimizationResultProgram>();

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
