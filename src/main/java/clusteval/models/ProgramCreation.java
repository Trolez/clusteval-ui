package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

public class ProgramCreation {
    @Size(min = 1, message = "Please specify a name for the program")
    private String name;

    @Size(min = 1, message = "Please specify an alias for the program parameter")
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

    public void setCompatibleDataSetFormats(ArrayList<String> compatibleDataSetFormats) {
        this.compatibleDataSetFormats = compatibleDataSetFormats;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
}
