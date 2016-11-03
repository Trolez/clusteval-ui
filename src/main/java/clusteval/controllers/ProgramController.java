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
import de.clusteval.program.r.RProgram;

import java.rmi.ConnectException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

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

    @RequestMapping(value="/programs/upload")
    public String uploadProgram(ProgramCreation programCreation, Model model) {
        try {
            populateModel(model);
        } catch (ConnectException e) {
            return "runs/notRunning";
        }
        return "programs/upload";
    }

    @RequestMapping(value="/programs/upload", method=RequestMethod.POST)
    public String uploadProgram(@Valid ProgramCreation programCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            }
            return "programs/upload";
        }

        boolean isRProgram = false;

        File temporaryFile = null;

        try {
            temporaryFile = convertToFile(programCreation.getExecutableFile());
            JarFile jarFile = new JarFile(temporaryFile);
            Enumeration<JarEntry> entries = jarFile.entries();

            URL[] urls = { new URL("jar:file:" + temporaryFile.getAbsolutePath() + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.isDirectory() || !entry.getName().endsWith(".class")){
                    continue;
                }

                // -6 because of .class
                String className = entry.getName().substring(0, entry.getName().length() - 6);
                className = className.replace('/', '.');
                try {
                    Class c = cl.loadClass(className);

                    if (RProgram.class.isAssignableFrom(c)) {
                        isRProgram = true;
                    } else {
                        isRProgram = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {}

        //Delete temporarily copied file
        FileUtils.deleteQuietly(temporaryFile);

        if (!isRProgram) {
            //Invocation format is required for this type of program
            if (programCreation.getInvocationFormat().isEmpty()) {
                try {
                    populateModel(model);
                } catch (ConnectException e) {
                    return "runs/notRunning";
                }
                bindingResult.rejectValue("invocationFormat", "invocationFormat", "Invocation format is required for this type of program");
                return "programs/upload";
            }
        }

        String programFileName = "";

        try {
            //Copy program file to repository
            if (programCreation.getExecutableFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }

            ArrayList<String> parameters = new ArrayList<String>();
            ArrayList<String> optimizableParameters = new ArrayList<String>();
            if (programCreation.getParameters() != null) {
                for (ProgramCreationParameter parameter : programCreation.getParameters()) {
                    parameters.add(parameter.getName());
                    if (parameter.getOptimizable()) {
                        optimizableParameters.add(parameter.getName());
                    }
                }
            }

            FileWriter writer = null;

            if (isRProgram) {
                Path path = Paths.get(getPath() + "/programs");

                Files.copy(programCreation.getExecutableFile().getInputStream(), path.resolve(programCreation.getExecutableFile().getOriginalFilename()));

                File programConfigFile = new File(getPath() + "/programs/configs/" + programCreation.getName() + ".config");
                writer = new FileWriter(programConfigFile);

                String typeName = "";
                try {
                    typeName = programCreation.getExecutableFile().getOriginalFilename().split("\\.")[0];
                } catch (Exception e) {}

                //Write basic program configuration
                writer.write("type = " + typeName + "\n");
                writer.write("parameters = " + StringUtils.join(parameters, ',') + "\n");
                writer.write("optimizationParameters = " + StringUtils.join(optimizableParameters, ',') + "\n");
                writer.write("alias = " + programCreation.getAlias() + "\n\n");
            } else {
                Path path = Paths.get(getPath() + "/programs/" + programCreation.getName());
                File filePath = new File(getPath() + "/programs/" + programCreation.getName());

                if (!filePath.exists()) {
                    filePath.mkdir();
                }

                Files.copy(programCreation.getExecutableFile().getInputStream(), path.resolve(programCreation.getExecutableFile().getOriginalFilename()));

                File programConfigFile = new File(getPath() + "/programs/configs/" + programCreation.getName() + ".config");
                writer = new FileWriter(programConfigFile);

                //Write basic program configuration
                writer.write("program = " + programCreation.getName() + "/" + programCreation.getExecutableFile().getOriginalFilename() + "\n");
                writer.write("parameters = " + StringUtils.join(parameters, ',') + "\n");
                writer.write("optimizationParameters = " + StringUtils.join(optimizableParameters, ',') + "\n");
                writer.write("compatibleDataSetFormats = " + StringUtils.join(programCreation.getCompatibleDataSetFormats(), ',') + "\n");
                writer.write("outputFormat = " + programCreation.getOutputFormat() + "\n");
                writer.write("alias = " + programCreation.getAlias() + "\n\n");
                writer.write("[invocationFormat]\n");
                writer.write("invocationFormat = " + programCreation.getInvocationFormat());
            }

            //Write out program parameter configurations
            if (programCreation.getParameters() != null) {
                for (ProgramCreationParameter parameter : programCreation.getParameters()) {
                    writer.write("\n\n");
                    writer.write("[" + parameter.getName() + "]\n");
                    writer.write("desc = " + parameter.getDescription() + "\n");
                    writer.write("type = " + parameter.getType() + "\n");
                    writer.write("def = " + parameter.getDefaultValue() + "\n");
                    if (parameter.getType() == 0) {
                        writer.write("options = " + parameter.getOptions());
                    } else {
                        writer.write("minValue = " + parameter.getMinValue() + "\n");
                        writer.write("maxValue = " + parameter.getMaxValue());
                    }
                }
            }

            writer.close();

            redirectAttributes.addFlashAttribute("success", "Program uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("failure", "Failed to upload program");
        }

        return "redirect:/programs/upload";
    }

    @RequestMapping(value="/programs/edit")
    public String editProgram(ProgramCreation programCreation, Model model, @RequestParam(value="name", required=true) String name) {
        try {
            populateModel(model);
        } catch (ConnectException e) {
            return "runs/notRunning";
        }

        programCreation.parse(getPath(), name);

        return "programs/edit";
    }

    @RequestMapping(value="/programs/edit", method=RequestMethod.POST)
    public String editProgram(@Valid ProgramCreation programCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            }
            return "programs/upload";
        }

        return "redirect:/programs/edit";
    }

    @RequestMapping(value="/programs/delete")
    public String deleteProgram(@RequestParam(value="name", required=true) String name) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(getPath() + "/programs/configs/" + name + ".config"));;
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("type")) {
                    //Delete program jar file
                    File jarFile = new File(getPath() + "/programs/" + currentLine.split("=")[1].trim() + ".jar");

                    jarFile.delete();
                }
                if (currentLine.startsWith("program")) {
                    //Delete program folder
                    String directoryName = currentLine.split("=")[1].split("/")[0].trim();
                    File directory = new File(getPath() + "/programs/" + directoryName);
                    FileUtils.deleteDirectory(directory);
                }
            }

            //Delete program configuration file
            File configurationFile = new File(getPath() + "/programs/configs/" + name + ".config");
            configurationFile.delete();
        } catch (Exception e) {}

        return "programs/index";
    }

    private File convertToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        convertedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private void populateModel(Model model) throws ConnectException {
        try {
            BackendClient backendClient = getBackendClient();

            ArrayList<String> compatibleDataSetFormats = new ArrayList<String>(backendClient.getDataSetFormats());
            ArrayList<String> outputFormats = new ArrayList<String>(backendClient.getRunResultFormats());

            Collections.sort(compatibleDataSetFormats, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(outputFormats, String.CASE_INSENSITIVE_ORDER);

            model.addAttribute("compatibleDataSetFormats", compatibleDataSetFormats);
            model.addAttribute("outputFormats", outputFormats);
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
