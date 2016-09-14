package clusteval;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

import org.apache.commons.lang3.StringUtils;

@Controller
public class ProgramController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/programs")
    public String showPrograms(Model model) {
        ArrayList<String> programs;

        try {
            BackendClient backendClient = getBackendClient();
            programs = new ArrayList<String>(backendClient.getProgramConfigurations());
            Collections.sort(programs, String.CASE_INSENSITIVE_ORDER);

            model.addAttribute("programs", programs);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e){

        }

        return "programs/index";
    }

    @RequestMapping(value="/programs/show")
    public String showProgram(Model model, @RequestParam(value="name", required=true) String name) {
        ArrayList<String> programConfigContent = new ArrayList<String>();
        try {
            BufferedReader br;
            String currentLine;
            br = new BufferedReader(new FileReader(getPath() + "/programs/configs/" + name + ".config"));

            while ((currentLine = br.readLine()) != null) {
                programConfigContent.add(currentLine);
            }
        } catch (Exception e) {
        }

        model.addAttribute("programConfigContent", programConfigContent);

        return "programs/show";
    }

    @RequestMapping(value="/getProgram", method=RequestMethod.GET)
    public @ResponseBody Program getProgram(@RequestParam(value="name", required=true) String name) {
        Program program = new Program();
        program.setName(name);

        //Get array of parameter eligible for parameter optimization
        ArrayList<String> optimizationParameters = new ArrayList<String>();
        try {
            BufferedReader br;
            String currentLine;
            br = new BufferedReader(new FileReader(getPath() + "/programs/configs/" + program.getName() + ".config"));

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("optimizationParameters")) {
                    optimizationParameters = new ArrayList<String>(Arrays.asList(StringUtils.split(currentLine.substring(currentLine.indexOf("=") + 1).trim(), ',')));
                    break;
                }
            }
        } catch (Exception e) {
        }

        try {
            BackendClient backendClient = getBackendClient();

            Map<String, Map<String, String>> parameters = backendClient.getParametersForProgramConfiguration(name);

            ArrayList<ProgramParameter> programParameters = new ArrayList<ProgramParameter>();
            for (Map.Entry<String, Map<String, String>> entry : parameters.entrySet())
            {
                ProgramParameter programParameter = new ProgramParameter();
                programParameter.setName(entry.getKey());

                if (optimizationParameters.contains(programParameter.getName())) {
                    programParameter.setOptimizable(true);
                }

                ArrayList<ProgramParameterOption> programParameterOptions = new ArrayList<ProgramParameterOption>();
                for (Map.Entry<String, String> subEntry : entry.getValue().entrySet()) {
                    ProgramParameterOption programParameterOption = new ProgramParameterOption();
                    programParameterOption.setName(subEntry.getKey());
                    programParameterOption.setValue(subEntry.getValue());

                    programParameterOptions.add(programParameterOption);

                    if (subEntry.getKey().equals("minValue")) {
                        programParameter.setMinValue(subEntry.getValue());
                    } else if (subEntry.getKey().equals("maxValue")) {
                        programParameter.setMaxValue(subEntry.getValue());
                    } else if (subEntry.getKey().equals("defaultValue")) {
                        programParameter.setValue(subEntry.getValue());
                    } else if (subEntry.getKey().equals("options")) {
                        programParameter.setOptions(subEntry.getValue());
                    }
                }

                programParameter.setDefaultOptions(programParameterOptions);
                programParameters.add(programParameter);
            }

            program.setParameters(programParameters);
        } catch (ConnectException e) {
        } catch (Exception e) {
        }

        return program;
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }

    private String getPath() {
        String path = "";
        try {
            BackendClient backendClient = getBackendClient();

            path = backendClient.getAbsoluteRepositoryPath();
        } catch (ConnectException e) {
        } catch (Exception e) {
        }

        return path;
    }
}
