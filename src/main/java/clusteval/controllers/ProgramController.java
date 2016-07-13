package clusteval;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@Controller
public class ProgramController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/getProgram", method=RequestMethod.GET)
    public @ResponseBody Program getProgram(@RequestParam(value="name", required=true) String name) {
        Program program = new Program();
        program.setName(name);
        try {
            BackendClient backendClient = getBackendClient();

            Map<String, Map<String, String>> parameters = backendClient.getParametersForProgramConfiguration(name);

            ArrayList<ProgramParameter> programParameters = new ArrayList<ProgramParameter>();
            for (Map.Entry<String, Map<String, String>> entry : parameters.entrySet())
            {
                ProgramParameter programParameter = new ProgramParameter();
                programParameter.setName(entry.getKey());

                ArrayList<ProgramParameterOption> programParameterOptions = new ArrayList<ProgramParameterOption>();
                for (Map.Entry<String, String> subEntry : entry.getValue().entrySet()) {
                    ProgramParameterOption programParameterOption = new ProgramParameterOption();
                    programParameterOption.setName(subEntry.getKey());
                    programParameterOption.setValue(subEntry.getValue());

                    programParameterOptions.add(programParameterOption);
                }

                programParameter.setOptions(programParameterOptions);
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
}
