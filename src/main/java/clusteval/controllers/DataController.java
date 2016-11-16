package clusteval;

import javax.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

@Controller
public class DataController {
    @Autowired
    DataService dataService;

    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/data")
    public String showAllData(Model model) {
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

    @RequestMapping(value="/data/show")
    public String showData(DataConfig dataConfig, @RequestParam(value="name", required=true) String name) {
        dataService.getData(dataConfig, name);

        return "data/show";
    }

    @RequestMapping(value="/data/upload")
    public String uploadData(DataCreation dataCreation, Model model) {
        try {
            populateModel(model);
        } catch (ConnectException e) {
            return "runs/notRunning";
        }
        return "data/upload";
    }

    @RequestMapping(value="/data/upload", method=RequestMethod.POST)
    public String uploadData(@Valid DataCreation dataCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            }
            return "data/upload";
        }

        try {
            dataService.createData(dataCreation);
            redirectAttributes.addFlashAttribute("success", "Succesfully uploaded data configuration");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "There was an error when creating the data configuration");
        }

        return "redirect:/data/upload";
    }

    @RequestMapping(value="/data/edit")
    public String editData(DataCreation dataCreation, Model model, @RequestParam(value="name", required=true) String name) {
        try {
            populateModel(model);
        } catch (ConnectException e) {
            return "runs/notRunning";
        }

        dataCreation.parse(getPath(), name);

        return "data/edit";
    }

    @RequestMapping(value="/data/edit", method=RequestMethod.POST)
    public String editData(@Valid DataCreation dataCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        boolean ignoreErrors = true;

        //Ignore validation errors if only data set file is missing
        for (FieldError error : bindingResult.getFieldErrors()) {
            if (!error.getField().equals("dataSetFile")) {
                ignoreErrors = false;
            }
        }

        //Return to form if there were validation errors
        if (bindingResult.hasErrors() && !ignoreErrors) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            }
            return "data/edit";
        }

        try {
            dataService.editData(dataCreation);
            redirectAttributes.addFlashAttribute("success", "Succesfully edited data configuration");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("success", "An error occurred when editing the data");
        }

        return "redirect:/data";
    }

    @RequestMapping(value="/data/delete")
    public String deleteData(@RequestParam(value="name", required=true) String name) {
        dataService.deleteData(name);


        return "data/index";
    }

    private void populateModel(Model model) throws ConnectException {
        try {
            BackendClient backendClient = getBackendClient();

            ArrayList<String> dataSetTypes = new ArrayList<String>(backendClient.getDataSetTypes());
            ArrayList<String> dataSetFormats = new ArrayList<String>(backendClient.getDataSetFormats());

            Collections.sort(dataSetTypes, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(dataSetFormats, String.CASE_INSENSITIVE_ORDER);

            model.addAttribute("dataSetTypes", dataSetTypes);
            model.addAttribute("dataSetFormats", dataSetFormats);
        } catch (ConnectException e) {
            throw(e);
        } catch (Exception e) {
        }
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
