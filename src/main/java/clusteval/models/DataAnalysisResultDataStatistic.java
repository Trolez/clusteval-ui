package clusteval;

public class DataAnalysisResultDataStatistic implements Comparable<DataAnalysisResultDataStatistic> {
    private String name;
    private String alias;
    private String value;
    private String filePath;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int compareTo(DataAnalysisResultDataStatistic other) {
        return this.getAlias().compareTo(other.getAlias());
    }
}
