package clusteval;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

import java.io.*;

@Controller
public class RunController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @Value("${absRepoPath}")
    private String path;

    @RequestMapping("/runs")
    public String showRuns(Model model) {
        ArrayList<String> runNames = new ArrayList<String>();
        ArrayList<Run> runs = new ArrayList<Run>();
        ArrayList<Run> runResumes = new ArrayList<Run>();

        try {
            BackendClient backendClient = getBackendClient();
            runNames = new ArrayList<String>(backendClient.getRuns());

            for (String runName : runNames) {
                runs.add(new Run(runName));
            }

            ArrayList<String> runResumeNames = new ArrayList<String>(backendClient.getRunResumes());

            for (String runResumeName : runResumeNames) {
                runResumes.add(new Run(runResumeName));
            }

            model.addAttribute("resumes", runResumes);

        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        model.addAttribute("runs", runs);

        return "runs/index";
    }

    @RequestMapping(value="/runs", method=RequestMethod.POST)
    public String performRun(@ModelAttribute Run run, Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = getBackendClient();

            backendClient.performRun(run.getName());

            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully started.");
        } catch (ConnectException e) {
            redirectAttributes.addFlashAttribute("failure", "Could not connect to the Clusteval server. Is it running?");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        return "redirect:/runs";
    }

    @RequestMapping(value="/resume-run", method=RequestMethod.POST)
    public String resumeRun(@ModelAttribute Run run, Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = getBackendClient();

            backendClient.resumeRun(run.getName());

            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully resumed.");
        } catch (ConnectException e) {
            redirectAttributes.addFlashAttribute("failure", "Could not connect to the Clusteval server. Is it running?");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        return "redirect:/runs";
    }

    @RequestMapping(value="/terminate-run", method=RequestMethod.POST)
    public String terminateRun(@ModelAttribute Run run, Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = getBackendClient();

            backendClient.terminateRun(run.getName());

            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully terminated.");
        } catch (ConnectException e) {
            redirectAttributes.addFlashAttribute("failure", "Could not connect to the Clusteval server. Is it running?");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        return "redirect:/runs";
    }

    @RequestMapping(value="/runs/create")
    public String createRun(RunCreation runCreation, Model model) {
        ArrayList<String> dataSets = getDataConfigurations();
        ArrayList<String> programs = getProgramConfigurations();
        ArrayList<String> qualityMeasures = getQualityMeasures();
        ArrayList<String> optimizationMethods = getOptimizationMethods();
        ArrayList<String> dataStatistics = getDataStatistics();

        model.addAttribute("optimizationMethods", optimizationMethods);

        try {
            BackendClient backendClient = getBackendClient();

            runCreation.setDataSets(dataSets);
            runCreation.setAllPrograms(programs);
            runCreation.setPrograms(new ArrayList<String>());
            runCreation.setQualityMeasures(qualityMeasures);
            runCreation.setDataStatistics(dataStatistics);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        return "runs/create";
    }

    @RequestMapping(value="/runs/create", method=RequestMethod.POST)
    public String createRun(@Valid RunCreation runCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            ArrayList<String> dataSets = getDataConfigurations();
            ArrayList<String> programs = getProgramConfigurations();
            ArrayList<String> qualityMeasures = getQualityMeasures();
            ArrayList<String> optimizationMethods = getOptimizationMethods();

            model.addAttribute("optimizationMethods", optimizationMethods);

            if (runCreation.getPrograms() == null) {
                runCreation.setPrograms(new ArrayList<String>());
            }

            try {
                BackendClient backendClient = getBackendClient();

                runCreation.setDataSets(dataSets);
                runCreation.setAllPrograms(programs);
                runCreation.setQualityMeasures(qualityMeasures);
            } catch (ConnectException e) {
                return "runs/notRunning";
            } catch (Exception e) {
            }
            return "runs/create";
        }

        //Create test file
        try {
            File file = new File(path + "/runs/" + runCreation.getName() + ".run");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            writer.write(runCreation.toString(path));
            writer.close();

            redirectAttributes.addFlashAttribute("success", "The run has been succcesfully created.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("failure", "An error occurred during file writing.");
        }

        return "redirect:/runs";
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }

    private ArrayList<String> getProgramConfigurations() {
        return getFileNames("programs/configs", "config");
    }

    private ArrayList<String> getDataConfigurations() {
        return getFileNames("data/configs", "dataconfig");
    }

    private ArrayList<String> getQualityMeasures() {
        return getFileNames("supp/clustering/qualityMeasures", "jar");
    }

    private ArrayList<String> getOptimizationMethods() {
        return getFileNames("supp/clustering/paramOptimization", "jar");
    }

    private ArrayList<String> getRandomizers() {
        return getFileNames("supp/randomizers/data", "jar");
    }

    private ArrayList<String> getDataStatistics() {
        return getFileNames("supp/statistics/data", "jar");
    }

    private ArrayList<String> getFileNames(String location, String extension) {
        ArrayList<String> fileNames = new ArrayList<String>();

        File folder = new File(path + "/" + location);

        File[] files = folder.listFiles();

        for (File file : files) {
            if (!file.isFile() || !FilenameUtils.getExtension(file.getName()).equals(extension)) {
                continue;
            }

            fileNames.add(FilenameUtils.removeExtension(file.getName()));
        }

        return fileNames;
    }
}
