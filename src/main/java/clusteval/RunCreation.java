package clusteval;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class RunCreation {
    @Size(min = 1, message = "Please specify a name for the run")
    private String name;

    @NotNull(message = "Please specify a mode for the run")
    private String mode;

    private String optimizationCriterion;

    private String optimizationMethod;

    private int optimizationIterations;

    @NotBlankOrNull(message = "Please select one or more programs for the run")
    private ArrayList<String> programs = new ArrayList<String>();

    private ArrayList<String> dataSets = new ArrayList<String>();

    private ArrayList<String> qualityMeasures = new ArrayList<String>();

    private ArrayList<String> dataStatistics = new ArrayList<String>();

    private ArrayList<String> runStatistics = new ArrayList<String>();

    private ArrayList<String> runDataStatistics = new ArrayList<String>();

    private ArrayList<String> uniqueRunIdentifiers = new ArrayList<String>();

    private ArrayList<String> uniqueDataIdentifiers = new ArrayList<String>();

    private String randomizer;
    private int numberOfRandomizedDataSets;

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

    public ArrayList<String> getRunStatistics() {
        return runStatistics;
    }

    public void setRunStatistics(Collection runStatistics) {
        this.runStatistics = new ArrayList<String>(runStatistics);
    }

    public ArrayList<String> getRunDataStatistics() {
        return runDataStatistics;
    }

    public void setRunDataStatistics(Collection runDataStatistics) {
        this.runDataStatistics = new ArrayList<String>(runDataStatistics);
    }

    public ArrayList<String> getUniqueRunIdentifiers() {
        return uniqueRunIdentifiers;
    }

    public void setUniqueRunIdentifiers(Collection uniqueRunIdentifiers) {
        this.uniqueRunIdentifiers = new ArrayList<String>(uniqueRunIdentifiers);
    }

    public ArrayList<String> getUniqueDataIdentifiers() {
        return uniqueDataIdentifiers;
    }

    public void setUniqueDataIdentifiers(Collection uniqueDataIdentifiers) {
        this.uniqueDataIdentifiers = new ArrayList<String>(uniqueDataIdentifiers);
    }

    public String getRandomizer() {
        return randomizer;
    }

    public void setRandomizer(String randomizer) {
        this.randomizer = randomizer;
    }

    public int getNumberOfRandomizedDataSets() {
        return numberOfRandomizedDataSets;
    }

    public void setNumberOfRandomizedDataSets(int numberOfRandomizedDataSets) {
        this.numberOfRandomizedDataSets = numberOfRandomizedDataSets;
    }

    public String toString(String path) {
        //TODO: replace placeholders with actual values
        String run = "mode = " + mode + "\n";

        if (mode.equals("clustering") || mode.equals("parameter_optimization") || mode.equals("dataAnalysis") || mode.equals("robustnessAnalysis")) {
            run += "dataConfig = ";
            run += StringUtils.join(dataSets, ',');
            run += "\n";
        }

        if (mode.equals("clustering") || mode.equals("parameter_optimization") || mode.equals("robustnessAnalysis")) {
            run += "programConfig = ";
            run += StringUtils.join(programs, ',');
            run += "\n";

            run += "qualityMeasures = ";
            run += StringUtils.join(qualityMeasures, ',');
            run += "\n";
        }

        if (mode.equals("parameter_optimization")) {
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
        }

        if (mode.equals("dataAnalysis")) {
            run += "dataStatistics = " + StringUtils.join(dataStatistics, ',') + "\n";
        }

        if (mode.equals("runAnalysis") || mode.equals("robustnessAnalysis")) {
            run += "uniqueRunIdentifiers = " + StringUtils.join(uniqueRunIdentifiers, ',') + "\n";
        }

        if (mode.equals("runAnalysis")) {
            run += "runStatistics = " + StringUtils.join(runStatistics, ',') + "\n";
        }

        if (mode.equals("runDataAnalysis")) {
            run += "runDataStatistics = " + StringUtils.join(runDataStatistics, ',') + "\n";
            run += "uniqueRunIdentifiers = " + StringUtils.join(uniqueRunIdentifiers, ',') + "\n";
            run += "uniqueDataIdentifiers = " + StringUtils.join(uniqueDataIdentifiers, ',') + "\n";
        }

        if (mode.equals("robustnessAnalysis")) {
            run += "randomizer = " + randomizer + "\n";
            run += "numberOfRandomizedDataSets = " + numberOfRandomizedDataSets + "\n";
        }

        return run;
    }
}
