package clusteval;

import java.util.ArrayList;

public class DataAnalysisResultData {
    private String name;

    private ArrayList<DataAnalysisResultDataStatistic> dataStatistics = new ArrayList<DataAnalysisResultDataStatistic>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DataAnalysisResultDataStatistic> getDataStatistics() {
        return dataStatistics;
    }

    public void setDataStatistics(ArrayList<DataAnalysisResultDataStatistic> dataStatistics) {
        this.dataStatistics = dataStatistics;
    }

    public void addToDataStatistics(DataAnalysisResultDataStatistic dataStatistic) {
        dataStatistics.add(dataStatistic);
    }
}
