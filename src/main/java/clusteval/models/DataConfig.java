package clusteval;

public class DataConfig {
    private String name;

    private String dataSetConfig;

    private String goldStandardConfig;

    private String dataSetName;

    private String dataSetFile;

    private Integer numberOfSamples;

    private Integer dimensionality;

    private boolean hasGoldstandard;

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

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Integer getDimensionality() {
        return dimensionality;
    }

    public void setDimensionality(Integer dimensionality) {
        this.dimensionality = dimensionality;
    }

    public boolean getHasGoldstandard() {
        return hasGoldstandard;
    }

    public void setHasGoldstandard(boolean hasGoldstandard) {
        this.hasGoldstandard = hasGoldstandard;
    }
}
