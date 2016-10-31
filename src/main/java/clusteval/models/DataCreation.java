package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

public class DataCreation {
    @Size(min = 1, message = "Please specify a name for the data")
    @Pattern(regexp="([0-9|a-z|A-Z|\\_])+", message = "Please only include letters a-z, numbers 0-9 and underscores (_) in the name")
    private String name;

    @NotNull(message = "Please specify a dataset type")
    private String dataSetType;

    @NotNull(message = "Please specify a dataset format")
    private String dataSetFormat;

    @HasOneFile(message = "Please provide a dataset file")
    private MultipartFile dataSetFile;

    private MultipartFile goldstandardFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSetType() {
        return dataSetType;
    }

    public void setDataSetType(String dataSetType) {
        this.dataSetType = dataSetType;
    }

    public String getDataSetFormat() {
        return dataSetFormat;
    }

    public void setDataSetFormat(String dataSetFormat) {
        this.dataSetFormat = dataSetFormat;
    }

    public MultipartFile getDataSetFile() {
        return dataSetFile;
    }

    public void setDataSetFile(MultipartFile dataSetFile) {
        this.dataSetFile = dataSetFile;
    }

    public MultipartFile getGoldstandardFile() {
        return goldstandardFile;
    }

    public void setGoldstandardFile(MultipartFile goldstandardFile) {
        this.goldstandardFile = goldstandardFile;
    }
}
