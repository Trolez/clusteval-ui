package clusteval;

import java.util.ArrayList;

public class ParameterOptimizationResultData {
    private String name;

    private ArrayList<ParameterOptimizationResultQuality> qualities = new ArrayList<ParameterOptimizationResultQuality>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ParameterOptimizationResultQuality> getQualities() {
        return qualities;
    }

    public void setQualities(ArrayList<ParameterOptimizationResultQuality> qualitities) {
        this.qualities = qualities;
    }

    public void addToQualities(ParameterOptimizationResultQuality quality) {
        qualities.add(quality);
    }
}
