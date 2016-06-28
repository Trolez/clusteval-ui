package clusteval;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Program {
    private String name;
    //private Map<String, Map<String, String>> parameters;
    private ArrayList<String> testing;

    public Program(String name, Map<String, Map<String, String>> parameters) {
        this.name = name;
        //this.parameters = parameters;
        this.testing = new ArrayList<String>();

        System.out.println("Testing this shit out!");
        for (Map.Entry<String, Map<String, String>> entry : parameters.entrySet())
        {
            System.out.println(entry.getKey());
            //testing.add(entry.getKey());
        }
    }

    public String getName() {
        return name;
    }

    /*public Map<String, Map<String, String>> getParameters() {
        return parameters;
    }*/

    public ArrayList<String> getTesting() {
        return testing;
    }
}
