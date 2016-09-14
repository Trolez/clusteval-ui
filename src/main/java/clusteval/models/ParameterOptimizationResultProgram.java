package clusteval;

import java.util.ArrayList;

public class ParameterOptimizationResultProgram {
    private String name;

    private int id;

    private ArrayList<ParameterOptimizationResultData> data = new ArrayList<ParameterOptimizationResultData>();

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

    public ArrayList<ParameterOptimizationResultData> getData() {
        return data;
    }

    public void setData(ArrayList<ParameterOptimizationResultData> data) {
        this.data = data;
    }

    public void addToData(ParameterOptimizationResultData data) {
        this.data.add(data);
    }
}
