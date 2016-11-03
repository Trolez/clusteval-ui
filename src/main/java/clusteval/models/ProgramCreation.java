package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.io.*;

public class ProgramCreation {
    @Size(min = 1, message = "Please specify a name for the program")
    @javax.validation.constraints.Pattern(regexp="([0-9|a-z|A-Z|\\_])+", message = "Please only include letters a-z, numbers 0-9 and underscores (_) in the name")
    private String name;

    @Size(min = 1, message = "Please specify an alias for the program")
    private String alias;

    private String invocationFormat;

    @HasOneFile(message = "Please provide a executable file containing the program")
    private MultipartFile executableFile;

    @Valid
    private ArrayList<ProgramCreationParameter> parameters;

    private ArrayList<String> compatibleDataSetFormats = new ArrayList<String>();

    private String outputFormat = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getInvocationFormat() {
        return invocationFormat;
    }

    public void setInvocationFormat(String invocationFormat) {
        this.invocationFormat = invocationFormat;
    }

    public MultipartFile getExecutableFile() {
        return executableFile;
    }

    public void setExecutableFile(MultipartFile executableFile) {
        this.executableFile = executableFile;
    }

    public ArrayList<ProgramCreationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(ArrayList<ProgramCreationParameter> parameters) {
        this.parameters = parameters;
    }

    public ArrayList<String> getCompatibleDataSetFormats() {
        return compatibleDataSetFormats;
    }

    public void setCompatibleDataSetFormats(Collection compatibleDataSetFormats) {
        this.compatibleDataSetFormats = new ArrayList<String>(compatibleDataSetFormats);
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void parse(String path, String fileName) {
        setName(fileName);

        ArrayList<String> optimizableParameters = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "/programs/configs/" + fileName + ".config"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                if (currentLine.startsWith("compatibleDataSetFormats")) {
                    setCompatibleDataSetFormats(Arrays.asList(line.split("\\s*,\\s*")));
                }
                else if (currentLine.startsWith("outputFormat")) {
                    setOutputFormat(line);
                }
                else if (currentLine.startsWith("alias")) {
                    setAlias(line);
                }
                else if (currentLine.startsWith("invocationFormat")) {
                    setInvocationFormat(line);
                }
                else if (currentLine.startsWith("optimizationParameters")) {
                    optimizableParameters = new ArrayList<String>(Arrays.asList(line.split("\\s*,\\s*")));
                }
            }
        } catch (Exception e) {}

        //Program parameters
        ArrayList<ProgramCreationParameter> parameters = new ArrayList<ProgramCreationParameter>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path + "/programs/configs/" + fileName + ".config"));;
            String currentLine;

            Pattern pattern = Pattern.compile("\\[(.+)\\]"); //Should match [parameter]
            Matcher matcher;
            ProgramCreationParameter parameter = new ProgramCreationParameter();

            parameters.add(parameter);

            while ((currentLine = br.readLine()) != null) {
                matcher = pattern.matcher(currentLine);
                if (matcher.find()) {
                    String parameterName = matcher.group(1); //Group count is 1-based

                    parameter.setName(parameterName);

                    if (optimizableParameters.contains(parameterName)) {
                        parameter.setOptimizable(true);
                    }

                    while ((currentLine = br.readLine()) != null && !(currentLine.equals(""))) {
                        String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                        if (currentLine.startsWith("minValue")) {
                            parameter.setMinValue(line);
                        }
                        if (currentLine.startsWith("maxValue")) {
                            parameter.setMaxValue(line);
                        }
                        if (currentLine.startsWith("def")) {
                            parameter.setDefaultValue(line);
                        }
                        if (currentLine.startsWith("options")) {
                            parameter.setOptions(line);
                        }
                        if (currentLine.startsWith("desc")) {
                            parameter.setDescription(line);
                        }
                        if (currentLine.startsWith("type")) {
                            parameter.setType(Integer.parseInt(line));
                        }
                    }
                }
            }

            setParameters(parameters);
        } catch (Exception e) {
        }
    }
}
