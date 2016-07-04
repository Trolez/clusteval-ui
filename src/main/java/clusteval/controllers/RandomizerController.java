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
public class RandomizerController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/getRandomizer", method=RequestMethod.GET)
    public @ResponseBody Randomizer getRandomizerTest(@RequestParam(value="name", required=true) String name) {
        Randomizer randomizer = new Randomizer();
        randomizer.setName(name);
        String result = "";
        try {
            //HashMap<String, String> parameters = new HashMap<String, String>();
            ArrayList<Parameter> parameters = new ArrayList<Parameter>();
            BackendClient backendClient = getBackendClient();

            Options options = backendClient.getOptionsForDataRandomizer(name);
            Collection<Option> fuck = options.getOptions();

            for (Option option : fuck) {
                Parameter param = new Parameter();
                param.setName(option.getArgName());
                param.setDescription(option.getDescription());
                parameters.add(param);
            }
            randomizer.setParameters(parameters);
        } catch (ConnectException e) {
            result = " There was a connect exception!";
        } catch (Exception e) {
            result = " There was some exception!";
            result += e.toString();
            e.printStackTrace();
        }

        return randomizer;
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }
}
