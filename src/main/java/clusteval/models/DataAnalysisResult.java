package clusteval;

import java.util.ArrayList;

public class DataAnalysisResult {
    private String name;

    private ArrayList<DataAnalysisResultData> dataConfigs = new ArrayList<DataAnalysisResultData>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DataAnalysisResultData> getDataConfigs() {
        return dataConfigs;
    }

    public void setDataConfigs(ArrayList<DataAnalysisResultData> dataConfigs) {
        this.dataConfigs = dataConfigs;
    }

    public void addToDataConfigs(DataAnalysisResultData dataConfig) {
        dataConfigs.add(dataConfig);
    }
}
