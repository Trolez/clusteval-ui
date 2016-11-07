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
import org.apache.commons.io.FileUtils;

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
                    dataConfig.setHasGoldstandard(true);
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

        //Read dataset file to determine number of samples and dimensionality
        Integer dimensionality = null;
        Integer numberOfSamples = 0;
        String datasetFile = new String(getPath() + "/data/datasets/" + dataConfig.getDataSetName() + "/" + dataConfig.getDataSetFile());
        try {
            BufferedReader br = new BufferedReader(new FileReader(datasetFile));

            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (!currentLine.startsWith("//")) {
                    numberOfSamples++;
                    if (dimensionality == null) {
                        dimensionality = currentLine.split("\t").length - 1;
                    }
                }
            }
        } catch (Exception e) {}
        dataConfig.setNumberOfSamples(numberOfSamples);
        dataConfig.setDimensionality(dimensionality);

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
            //Create dataconfig file
            File dataConfigFile = new File(getPath() + "/data/configs/" + dataCreation.getName() + ".dataconfig");

            if (!dataConfigFile.exists()) {
                dataConfigFile.createNewFile();
            }

            FileWriter writer = new FileWriter(dataConfigFile);

            writer.write("datasetConfig = " + dataCreation.getName() + "\n");

            if (dataCreation.getGoldstandardFile().getSize() > 0) {
                writer.write("goldstandardConfig = " + dataCreation.getName());
            }
            writer.close();
        } catch (IOException e) {
        }

        String dataSetFileName = "";

        try {
            //Copy data set file to repository
            if (dataCreation.getDataSetFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/data/datasets/" + dataCreation.getName());
            File filePath = new File(getPath() + "/data/datasets/" + dataCreation.getName());

            if (!filePath.exists()) {
                filePath.mkdir();
            }

            String extension = "";
            try {
                extension = dataCreation.getDataSetFile().getOriginalFilename().split("\\.")[1];
            } catch (Exception e) {}

            dataSetFileName = dataCreation.getName() + "." + extension;

            File dataConfigFile = new File(getPath() + "/data/datasets/" + dataCreation.getName() + "/" + dataSetFileName);
            FileWriter writer = new FileWriter(dataConfigFile);
            writer.write("// dataSetFormat = " + dataCreation.getDataSetFormat() + "\n");
            writer.write("// dataSetType = " + dataCreation.getDataSetType() + "\n");
            writer.write("// dataSetFormatVersion = 1\n");
            writer.write("// alias = " + dataCreation.getName() + "\n");
            writer.write("// websiteVisibility = show_always\n");

            BufferedReader br = new BufferedReader(new InputStreamReader(dataCreation.getDataSetFile().getInputStream()));
            String currentLine = "";
            while((currentLine = br.readLine()) != null) {
                if (!currentLine.startsWith("//")) {
                    writer.write(currentLine + "\n");
                }
            }

            writer.close();

            redirectAttributes.addFlashAttribute("success", "Data set file uploaded successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload file");
        }

        try {
            //Create dsconfig file
            File dataSetConfigFile = new File(getPath() + "/data/datasets/configs/" + dataCreation.getName() + ".dsconfig");

            if (!dataSetConfigFile.exists()) {
                dataSetConfigFile.createNewFile();
            }

            FileWriter writer = new FileWriter(dataSetConfigFile);

            writer.write("datasetName = " + dataCreation.getName() + "\n");
            writer.write("datasetFile = " + dataSetFileName);

            writer.close();
        } catch (IOException e) {
        }

        if (dataCreation.getGoldstandardFile().getSize() > 0) {
            String goldstandardFileName = "";
            try {
                //Copy goldstandard file to repository
                if (dataCreation.getGoldstandardFile().isEmpty()) {
                    redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
                }
                Path path = Paths.get(getPath() + "/data/goldstandards/" + dataCreation.getName());
                File filePath = new File(getPath() + "/data/goldstandards/" + dataCreation.getName());

                if (!filePath.exists()) {
                    filePath.mkdir();
                }

                String extension = "";
                try {
                    extension = dataCreation.getDataSetFile().getOriginalFilename().split("\\.")[1];
                } catch (Exception e) {}

                goldstandardFileName = dataCreation.getName() + "." + extension;

                Files.copy(dataCreation.getDataSetFile().getInputStream(), path.resolve(dataSetFileName));
                redirectAttributes.addFlashAttribute("success", "Goldstandard file uploaded successfully");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("failure", "Failed to upload file");
            }

            try {
                //Create gsconfig file
                File goldstandardConfigFile = new File(getPath() + "/data/goldstandards/configs/" + dataCreation.getName() + ".gsconfig");

                if (!goldstandardConfigFile.exists()) {
                    goldstandardConfigFile.createNewFile();
                }

                FileWriter writer = new FileWriter(goldstandardConfigFile);

                writer.write("goldstandardName = " + dataCreation.getName() + "\n");
                writer.write("goldstandardFile = " + goldstandardFileName);

                writer.close();
            } catch (IOException e) {
            }
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
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            }
            return "data/edit";
        }

        return "redirect:/data/edit";
    }

    @RequestMapping(value="/data/delete")
    public String deleteData(@RequestParam(value="name", required=true) String name) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(getPath() + "/data/configs/" + name + ".dataconfig"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("datasetConfig")) {
                    BufferedReader br2 = new BufferedReader(new FileReader(getPath() + "/data/datasets/configs/" + currentLine.split("=")[1].trim() + ".dsconfig"));
                    String currentLine2;
                    while ((currentLine2 = br.readLine()) != null) {
                        if (currentLine2.startsWith("datasetName")) {
                            //Delete dataset folder
                            String directoryName = currentLine2.split("=")[1].trim();
                            File directory = new File(getPath() + "/data/datasets/" + directoryName);
                            FileUtils.deleteDirectory(directory);
                        }
                    }

                    //Delete dataset configuration file
                    File configurationFile = new File(getPath() + "/data/datasets/configs/" + currentLine.split("=")[1].trim() + ".dsconfig");
                    configurationFile.delete();
                }
                if (currentLine.startsWith("goldstandardConfig")) {
                    BufferedReader br2 = new BufferedReader(new FileReader(getPath() + "/data/goldstandards/configs/" + currentLine.split("=")[1].trim() + ".gsconfig"));
                    String currentLine2;
                    while ((currentLine2 = br.readLine()) != null) {
                        if (currentLine2.startsWith("goldstandardName")) {
                            //Delete goldstandard folder
                            String directoryName = currentLine2.split("=")[1].trim();
                            File directory = new File(getPath() + "/data/goldstandards/" + directoryName);
                            FileUtils.deleteDirectory(directory);
                        }
                    }

                    //Delete goldstandard configuration file
                    File configurationFile = new File(getPath() + "/data/goldstandards/configs/" + currentLine.split("=")[1].trim() + ".gsconfig");
                    configurationFile.delete();
                }
            }

            //Delete data configuration file
            File configurationFile = new File(getPath() + "/data/configs/" + name + ".dataconfig");
            configurationFile.delete();
        } catch (Exception e) {}

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
