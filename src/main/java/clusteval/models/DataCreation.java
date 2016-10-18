package clusteval;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class DataCreation {
    @Size(min = 1, message = "Please specify a name for the data")
    private String name;

    @HasOneFile(message = "Please provide a dataset file")
    private MultipartFile datasetFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getDatasetFile() {
        return datasetFile;
    }

    public void setDatasetFile(MultipartFile datasetFile) {
        this.datasetFile = datasetFile;
    }
}
