package clusteval;

import javax.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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

@Controller
public class DataController {
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
        dataConfig.setName(name);

        try {
            BufferedReader br = new BufferedReader(new FileReader(getPath() + "/data/configs/" + name + ".dataconfig"));;
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                if (currentLine.startsWith("datasetConfig")) {
                    dataConfig.setDataSetConfig(line);
                } else if (currentLine.startsWith("goldstandardConfig")) {
                    dataConfig.setGoldStandardConfig(line);
                }
            }

            br = new BufferedReader(new FileReader(getPath() + "/data/datasets/configs/" + dataConfig.getDataSetConfig() + ".dsconfig"));;

            while ((currentLine = br.readLine()) != null) {
                String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                if (currentLine.startsWith("datasetName")) {
                    dataConfig.setDataSetName(line);
                } else if (currentLine.startsWith("datasetFile")) {
                    dataConfig.setDataSetFile(line);
                }
            }
        } catch (Exception e) {
        }

        return "data/show";
    }

    @RequestMapping(value="/data/upload")
    public String uploadData(DataCreation dataCreation) {
        return "data/upload";
    }

    @RequestMapping(value="/data/upload", method=RequestMethod.POST)
    public String uploadData(@Valid DataCreation dataCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            return "data/upload";
        }

        try {
            if (dataCreation.getDatasetFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath());

            Files.copy(dataCreation.getDatasetFile().getInputStream(), path.resolve(dataCreation.getDatasetFile().getOriginalFilename()));
            redirectAttributes.addFlashAttribute("success", "File uploaded successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload file");
        }

        return "redirect:/data/upload";
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
