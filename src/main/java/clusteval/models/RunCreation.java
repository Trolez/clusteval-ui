package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

public class RunCreation {
    private String originalName;

    @Size(min = 1, message = "Please specify a name for the run")
    private String name;

    @NotNull(message = "Please specify a mode for the run")
    private String mode;

    @NotNull(message = "Please select a optimization criterion")
    private String optimizationCriterion;

    @NotNull(message = "Please select a optimization method")
    private String optimizationMethod;

    @NotNull(message = "Please specify number of optimization iterations")
    @Min(value = 1, message = "Please specify a positive integer")
    private Integer optimizationIterations;

    @NotBlankOrNull(message = "Please select one or more programs for the run")
    private ArrayList<String> programs = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more data sets for the run")
    private ArrayList<String> dataSets = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more quality measures for the run")
    private ArrayList<String> qualityMeasures = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more data statistics for the run")
    private ArrayList<String> dataStatistics = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more run statistics for the run")
    private ArrayList<String> runStatistics = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more run data statistics for the run")
    private ArrayList<String> runDataStatistics = new ArrayList<String>();

    //@NotBlankOrNull(message = "Please select one or more unique run identifiers for the run")
    private ArrayList<String> uniqueRunIdentifiers = new ArrayList<String>();

    @NotBlankOrNull(message = "Please select one or more unique data identifiers for the run")
    private ArrayList<String> uniqueDataIdentifiers = new ArrayList<String>();

    private String randomizer;
    private Integer numberOfRandomizedDataSets;

    private ArrayList<Randomizer> randomizers = new ArrayList<Randomizer>();

    @Valid
    private ArrayList<Program> programSettings = new ArrayList<Program>();

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

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

    public Integer getOptimizationIterations() {
        return optimizationIterations;
    }

    public void setOptimizationIterations(Integer optimizationIterations) {
        this.optimizationIterations = optimizationIterations;
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

    public Integer getNumberOfRandomizedDataSets() {
        return numberOfRandomizedDataSets;
    }

    public void setNumberOfRandomizedDataSets(Integer numberOfRandomizedDataSets) {
        this.numberOfRandomizedDataSets = numberOfRandomizedDataSets;
    }

    public ArrayList<Randomizer> getRandomizers() {
        return randomizers;
    }

    public void setRandomizers(ArrayList<Randomizer> randomizers) {
        this.randomizers = randomizers;
    }

    public ArrayList<Program> getProgramSettings() {
        return programSettings;
    }

    public void setProgramSettings(ArrayList<Program> programSettings) {
        this.programSettings = programSettings;
    }

    public void parse(String path, String fileName) {
        setName(fileName);
        setOriginalName(fileName);

        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "/runs/" + fileName + ".run"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                if (currentLine.startsWith("#editOf")) {
                    setOriginalName(line);
                }
                if (currentLine.startsWith("mode")) {
                    setMode(line);
                }
                else if (currentLine.startsWith("programConfig")) {
                    setPrograms(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("optimizationCriterion")) {
                    setOptimizationCriterion(line);
                }
                else if (currentLine.startsWith("optimizationMethod")) {
                    setOptimizationMethod(line);
                }
                else if (currentLine.startsWith("optimizationIterations")) {
                    setOptimizationIterations(Integer.parseInt(line));
                }
                else if (currentLine.startsWith("dataConfig")) {
                    setDataSets(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("qualityMeasures")) {
                    setQualityMeasures(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("dataStatistics")) {
                    setDataStatistics(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("runStatistics")) {
                    setRunStatistics(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("runDataStatistics")) {
                    setRunDataStatistics(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("uniqueRunIdentifiers")) {
                    setUniqueRunIdentifiers(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("uniqueDataIdentifiers")) {
                    setUniqueDataIdentifiers(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("randomizer")) {
                    setRandomizer(line);
                }
                else if (currentLine.startsWith("numberOfRandomizedDataSets")) {
                    setNumberOfRandomizedDataSets(Integer.parseInt(line));
                }
            }
        } catch (Exception e) {}

        //Randomizers
        ArrayList<Randomizer> randomizers = new ArrayList<Randomizer>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "/runs/" + fileName + ".run"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("[" + getRandomizer())) {
                    Randomizer randomizer = new Randomizer();
                    randomizer.setName(getRandomizer());
                    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
                    while (!(currentLine = br.readLine()).equals("")) {
                        Parameter parameter = new Parameter();
                        parameter.setName(currentLine.split("=")[0].trim());
                        parameter.setValue(currentLine.split("=")[1].trim());
                        parameters.add(parameter);
                    }
                    randomizer.setParameters(parameters);
                    randomizers.add(randomizer);
                }
            }
            setRandomizers(randomizers);
        } catch (Exception e) {}

        //Program parameters
        ArrayList<Program> programs = new ArrayList<Program>();
        ArrayList<ProgramParameter> programParameters = new ArrayList<ProgramParameter>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "/runs/" + fileName + ".run"));;
            String currentLine;

            Pattern pattern = Pattern.compile("\\[(.+):(.+)\\]"); //Should match [program:parameter]
            Matcher matcher;
            Program program = new Program();
            ProgramParameter programParameter;

            HashMap<String, ArrayList<String>> parametersToOptimize = new HashMap<String, ArrayList<String>>();
            Pattern programPattern = Pattern.compile("\\[(.+)\\]"); //Should match [program]
            Matcher programMatcher;

            while ((currentLine = br.readLine()) != null) {
                matcher = pattern.matcher(currentLine);
                programMatcher = programPattern.matcher(currentLine);

                if (programMatcher.find()) {
                    if (currentLine.indexOf(':') < 0) {
                        //Add parameters that should be optimized
                        while ((currentLine = br.readLine()) != null && !(currentLine.equals(""))) {
                            if (currentLine.startsWith("optimizationParameters")) {
                                String programParametersToOptimize = currentLine.split("=")[1].trim();
                                parametersToOptimize.put(programMatcher.group(1), new ArrayList<String>(Arrays.asList(programParametersToOptimize.split(","))));
                                break;
                            }
                        }
                    }
                }

                if (matcher.find()) {
                    String programName = matcher.group(1); //Group count is 1-based
                    String parameterName = matcher.group(2);

                    Program programTest = new Program();
                    programTest.setName(programName);
                    if (!programs.contains(programTest)) {
                        ProgramService programService = new ProgramService();
                        program = programService.getProgram(programName);

                        programs.add(program);
                    }

                    int i = 0;
                    while (!program.getParameters().get(i).getName().equals(parameterName)) {
                        i++;
                    }

                    programParameter = program.getParameters().get(i);

                    while ((currentLine = br.readLine()) != null && !(currentLine.equals(""))) {
                        String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                        if (currentLine.startsWith("minValue")) {
                            programParameter.setMinValue(line);
                        }
                        if (currentLine.startsWith("maxValue")) {
                            programParameter.setMaxValue(line);
                        }
                        if (currentLine.startsWith("def")) {
                            programParameter.setValue(line);
                        }
                        if (currentLine.startsWith("options")) {
                            programParameter.setOptions(line);
                        }
                        if (parametersToOptimize.get(programName) != null && parametersToOptimize.get(programName).contains(parameterName)) {
                            programParameter.setOptimize(true);
                        } else {
                            programParameter.setOptimize(false);
                        }
                    }
                }
            }

            //Add programs for older runs without parameter settings
            for (String programName : getPrograms()) {
                Program programTest = new Program();
                programTest.setName(programName.trim());
                if (!programs.contains(programTest)) {
                    ProgramService programService = new ProgramService();
                    program = programService.getProgram(programName);

                    programs.add(program);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setProgramSettings(programs);
    }

    public String toString(String path) {
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

            run += "optimizationIterations = " + optimizationIterations + "\n";

            run += "optimizationMethod = " + optimizationMethod + "\n\n";

            ArrayList<String> parametersToOptimize;
            for (Program program : programSettings) {
                if (program.getParameters() != null) {
                    parametersToOptimize = new ArrayList<String>();

                    run += "[" + program.getName() + "]\n";

                    for (ProgramParameter parameter : program.getParameters()) {
                        if (parameter.getOptimize() && parameter.getOptimizable()) {
                            parametersToOptimize.add(parameter.getName());
                        }
                    }
                    run += "optimizationParameters = " + StringUtils.join(parametersToOptimize, ',') + "\n\n";
                }
            }
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

            int randomizerCounter = 1;
            for (Randomizer rand : randomizers) {
                run += "\n[" + randomizer + "_" + randomizerCounter + "]\n";
                for (Parameter parameter : rand.getParameters()) {
                    if (!parameter.getValue().isEmpty()) {
                        run += parameter.getName() + " = " + parameter.getValue() + "\n";
                    }
                }
                randomizerCounter++;
            }
        }

        if (mode.equals("clustering") || mode.equals("parameter_optimization") || mode.equals("robustnessAnalysis")) {
            for (Program programSetting : programSettings) {
                if (programSetting.getParameters() != null) {
                    for (ProgramParameter programParameter : programSetting.getParameters()) {
                        run += "\n[" + programSetting.getName() + ":" + programParameter.getName() + "]\n";
                        if (programParameter.getMinValue() != null) {
                            run += "minValue=" + programParameter.getMinValue() + "\n";
                        }
                        if (programParameter.getMaxValue() != null) {
                            run += "maxValue=" + programParameter.getMaxValue() + "\n";
                        }
                        if (programParameter.getValue() != null) {
                            run += "def=" + programParameter.getValue() + "\n";
                        }
                        if (programParameter.getOptions() != null) {
                            run += "options=" + programParameter.getOptions() + "\n";
                        }
                    }
                }
            }
        }

        return run;
    }
}
