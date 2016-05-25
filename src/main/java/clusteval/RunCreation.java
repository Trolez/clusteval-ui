package clusteval;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class RunCreation {
    @NotNull(message = "Please enter a name for the run")
    private String name;

    @NotNull
    private String mode;

    private String optimizationCriterion;

    private String optimizationMethod;

    private int optimizationIterations;

    private ArrayList<String> programs;
    public ArrayList<String> allPrograms;

    private ArrayList<String> dataSets;

    private ArrayList<String> qualityMeasures;

    private ArrayList<String> dataStatistics;

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

    public String getOptimizationCriterion() {
        return optimizationCriterion;
    }

    public void setOptimizationCriterion(String optimizationCriterion) {
        this.optimizationCriterion = optimizationCriterion;
    }

    public String getOptimizationMethod() {
        return optimizationMethod;
    }

    public void setOptimizationMethod(String optimizationMethod) {
        this.optimizationMethod = optimizationMethod;
    }

    public ArrayList<String> getPrograms() {
        return programs;
    }

    public void setPrograms(Collection programs) {
        this.programs = new ArrayList<String>(programs);
    }

    public ArrayList<String> getAllPrograms() {
        return allPrograms;
    }

    public void setAllPrograms(Collection programs) {
        this.allPrograms = new ArrayList<String>(programs);
    }

    public ArrayList<String> getDataSets() {
        return dataSets;
    }

    public void setDataSets(Collection dataSets) {
        this.dataSets = new ArrayList<String>(dataSets);
    }

    public ArrayList<String> getQualityMeasures() {
        return qualityMeasures;
    }

    public void setQualityMeasures(Collection qualityMeasures) {
        this.qualityMeasures = new ArrayList<String>(qualityMeasures);
    }

    public ArrayList<String> getDataStatistics() {
        return dataStatistics;
    }

    public void setDataStatistics(Collection dataStatistics) {
        this.dataStatistics = new ArrayList<String>(dataStatistics);
    }

    public String toString(String path) {
        //TODO: replace placeholders with actual values
        String run = "mode = " + mode + "\n";

        run += "dataConfig = ";
        run += StringUtils.join(dataSets, ',');
        run += "\n";

        if (mode.equals("parameter_optimization")) {
            run += "programConfig = ";
            run += StringUtils.join(programs, ',');
            run += "\n";

            run += "qualityMeasures = ";
            run += StringUtils.join(qualityMeasures, ',');
            run += "\n";

            run += "optimizationCriterion = " + optimizationCriterion + "\n";

            run += "optimizationIterations = " + 1000 + "\n";

            run += "optimizationMethod = " + optimizationMethod + "\n\n";

            try {
                BufferedReader br;
                String currentLine;
                for (String program : programs) {
                    run += "[" + program + "]\n";

                    br = new BufferedReader(new FileReader(path + "/programs/configs/" + program + ".config"));

                    while ((currentLine = br.readLine()) != null) {
                        if (currentLine.startsWith("optimizationParameters")) {
                            run += "optimizationParameters = " + currentLine.substring(currentLine.indexOf("=") + 1).trim() + "\n\n";
                        }
                    }
                }
            } catch (Exception e) {}

            run += "\n[DBSCAN]\n";
            run += "optimizationParameters = eps,MinPts";
        }

        if (mode.equals("dataAnalysis")) {
            run += "dataStatistics = " + StringUtils.join(dataStatistics, ',') + "\n";
        }

        return run;
    }
}
