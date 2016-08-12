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
public class DataController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/data")
    public String showData(Model model) {
        ArrayList<String> data;

        try {
            BackendClient backendClient = getBackendClient();
            data = new ArrayList<String>(backendClient.getDataConfigurations());
            Collections.sort(data, String.CASE_INSENSITIVE_ORDER);

            model.addAttribute("datas", data);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e){

        }

        return "data/index";
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
