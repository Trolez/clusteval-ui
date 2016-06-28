package clusteval;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.Map;
import java.util.HashMap;

@Controller
public class ProgramController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/getProgram", method=RequestMethod.GET)
    public @ResponseBody Program getProgram(@RequestParam(value="name", required=true) String name) {
        Program program = null;
        try {
            BackendClient backendClient = getBackendClient();

            program = new Program(name, backendClient.getParametersForProgramConfiguration(name));
        } catch (ConnectException e) {
        } catch (Exception e) {
        }

        return program;
    }

    @RequestMapping(value="/test", method=RequestMethod.GET)
    public @ResponseBody String getProgramTest(@RequestParam(value="name", required=true) String name) {
        String result = "Nothing";
        try {
            BackendClient backendClient = getBackendClient();

            result += " - Looping now: ";
            //result += "" + backendClient.getParametersForProgramConfiguration(name).values().size() + "";

            Map<String, Map<String, String>> map = backendClient.getParametersForProgramConfiguration(name);

            if (map == null) {
                result += "null";
            }

            /*for (Map.Entry<String, Map<String, String>> entry : map.entrySet())
            {
                result += "Iteration: ";
                //result += entry.getKey().toString();
            }*/
        } catch (ConnectException e) {
            result = " There was a connect exception!";
        } catch (Exception e) {
            result = " There was some exception!";
            result += e.toString();
            e.printStackTrace();
        }

        return result;
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }
}
