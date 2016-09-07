package clusteval;

public class DataConfig {
    private String name;

    private String dataSetConfig;

    private String goldStandardConfig;

    private String dataSetName;

    private String dataSetFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSetConfig() {
        return dataSetConfig;
    }

    public void setDataSetConfig(String dataSetConfig) {
        this.dataSetConfig = dataSetConfig;
    }

    public String getGoldStandardConfig() {
        return goldStandardConfig;
    }

    public void setGoldStandardConfig(String goldStandardConfig) {
        this.goldStandardConfig = goldStandardConfig;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public String getDataSetFile() {
        return dataSetFile;
    }

    public void setDataSetFile(String dataSetFile) {
        this.dataSetFile = dataSetFile;
    }
}
