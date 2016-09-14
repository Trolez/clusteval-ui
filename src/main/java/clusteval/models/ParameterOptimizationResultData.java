package clusteval;

import java.util.ArrayList;

public class ParameterOptimizationResultData {
    private String name;

    private int id;

    private ArrayList<ParameterOptimizationResultQuality> qualities = new ArrayList<ParameterOptimizationResultQuality>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
