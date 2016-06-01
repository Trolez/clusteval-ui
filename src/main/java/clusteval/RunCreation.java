package clusteval;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class RunCreation {
    @Size(min=1)
    private String name;

    @NotNull
    private String mode;

    private String optimizationCriterion;

    private String optimizationMethod;

    private int optimizationIterations;

    private ArrayList<String> programs = new ArrayList<String>();

    private ArrayList<String> dataSets = new ArrayList<String>();

    private ArrayList<String> qualityMeasures = new ArrayList<String>();

    private ArrayList<String> dataStatistics = new ArrayList<String>();

    private ArrayList<String> runStatistics = new ArrayList<String>();

    private ArrayList<String> runDataStatistics = new ArrayList<String>();

    private ArrayList<String> uniqueRunIdentifiers = new ArrayList<String>();

    private ArrayList<String> uniqueDataIdentifiers = new ArrayList<String>();

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

    public String toString(String path) {
        //TODO: replace placeholders with actual values
        String run = "mode = " + mode + "\n";

        if (mode.equals("parameter_optimization") || mode.equals("dataAnalysis")) {
            run += "dataConfig = ";
            run += StringUtils.join(dataSets, ',');
            run += "\n";
        }

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
        }

        if (mode.equals("dataAnalysis")) {
            run += "dataStatistics = " + StringUtils.join(dataStatistics, ',') + "\n";
        }

        if (mode.equals("runAnalysis")) {
            run += "uniqueRunIdentifiers = " + StringUtils.join(uniqueRunIdentifiers, ',') + "\n";
            run += "runStatistics = " + StringUtils.join(runStatistics, ',') + "\n";
        }

        return run;
    }
}
