package clusteval;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collection;

public class RunCreation {
    @NotNull(message = "Please enter a name for the run")
    private String name;

    @NotNull
    private String mode;

    @NotNull
    private ArrayList<String> programs;

    private ArrayList<String> dataSets;

    private ArrayList<String> selectedPrograms;

    private ArrayList<String> selectedDataSets;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public ArrayList<String> getPrograms() {
        return programs;
    }

    public void setPrograms(Collection programs) {
        this.programs = new ArrayList<String>(programs);
    }

    public ArrayList<String> getDataSets() {
        return dataSets;
    }

    public void setDataSets(Collection dataSets) {
        this.dataSets = new ArrayList<String>(dataSets);
    }

    public ArrayList<String> getSelectedPrograms() {
        return selectedPrograms;
    }

    public void setSelectedPrograms(Collection selectedPrograms) {
        this.selectedPrograms = new ArrayList<String>(selectedPrograms);
    }

    public ArrayList<String> getSelectedDataSets() {
        return selectedDataSets;
    }

    public void setSelectedDataSets(Collection selectedDataSets) {
        this.selectedDataSets = new ArrayList<String>(selectedDataSets);
    }

    public String toString() {
        //TODO: replace placeholders with actual values
        String run = "";

        run += "mode: parameter_optimization\n";

        return run;
    }
}
