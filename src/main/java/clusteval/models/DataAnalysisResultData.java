package clusteval;

import java.util.ArrayList;

public class DataAnalysisResultData {
    private String name;

    private ArrayList<DataAnalysisResultDataStatistic> dataStatistics = new ArrayList<DataAnalysisResultDataStatistic>();

    private DataAnalysisResultDataStatistic classSizeDistribution;

    private DataAnalysisResultDataStatistic nodeDegreeDistribution;

    private DataAnalysisResultDataStatistic intraInterSimilarityDistribution;

    private DataAnalysisResultDataStatistic similarityDistribution;

    private Integer numberOfSamples;

    private Integer dimensionality;

    private boolean hasGoldstandard;

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

    public DataAnalysisResultDataStatistic getClassSizeDistribution() {
        return classSizeDistribution;
    }

    public void setClassSizeDistribution(DataAnalysisResultDataStatistic classSizeDistribution) {
        this.classSizeDistribution = classSizeDistribution;
    }

    public DataAnalysisResultDataStatistic getNodeDegreeDistribution() {
        return nodeDegreeDistribution;
    }

    public void setNodeDegreeDistribution(DataAnalysisResultDataStatistic nodeDegreeDistribution) {
        this.nodeDegreeDistribution = nodeDegreeDistribution;
    }

    public DataAnalysisResultDataStatistic getIntraInterSimilarityDistribution() {
        return intraInterSimilarityDistribution;
    }

    public void setIntraInterSimilarityDistribution(DataAnalysisResultDataStatistic intraInterSimilarityDistribution) {
        this.intraInterSimilarityDistribution = intraInterSimilarityDistribution;
    }

    public DataAnalysisResultDataStatistic getSimilarityDistribution() {
        return similarityDistribution;
    }

    public void setSimilarityDistribution(DataAnalysisResultDataStatistic similarityDistribution) {
        this.similarityDistribution = similarityDistribution;
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
