package clusteval;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;

import java.rmi.ConnectException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
            Collections.sort(runNames, String.CASE_INSENSITIVE_ORDER);

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

    private void populateModel(Model model) throws ConnectException {
        try {
            BackendClient backendClient = getBackendClient();

            ArrayList<String> dataSets = new ArrayList<String>(backendClient.getDataConfigurations());
            ArrayList<String> programs = new ArrayList<String>(backendClient.getProgramConfigurations());
            ArrayList<String> qualityMeasures = new ArrayList<String>(backendClient.getClusteringQualityMeasures());
            ArrayList<String> optimizationMethods = new ArrayList<String>(backendClient.getParameterOptimizationMethods());
            ArrayList<String> dataStatistics = new ArrayList<String>(backendClient.getDataStatistics());
            ArrayList<String> runStatistics = new ArrayList<String>(backendClient.getRunStatistics());
            ArrayList<String> runDataStatistics = new ArrayList<String>(backendClient.getRunDataStatistics());
            ArrayList<String> uniqueRunIdentifiers = new ArrayList<String>(backendClient.getClusteringRunResultIdentifiers());
            ArrayList<String> randomizers = new ArrayList<String>(backendClient.getDataRandomizers());

            Collections.sort(dataSets, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(programs, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(qualityMeasures, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(optimizationMethods, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(dataStatistics, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(runStatistics, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(runDataStatistics, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(uniqueRunIdentifiers, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(randomizers, String.CASE_INSENSITIVE_ORDER);

            model.addAttribute("dataSets", dataSets);
            model.addAttribute("programs", programs);
            model.addAttribute("qualityMeasures", qualityMeasures);
            model.addAttribute("optimizationMethods", optimizationMethods);
            model.addAttribute("dataStatistics", dataStatistics);
            model.addAttribute("runStatistics", runStatistics);
            model.addAttribute("runDataStatistics", runDataStatistics);
            model.addAttribute("uniqueRunIdentifiers", uniqueRunIdentifiers);
            model.addAttribute("randomizers", randomizers);
        } catch (ConnectException e) {
            throw(e);
        } catch (Exception e) {
        }
    }

    @RequestMapping(value="/runs/create")
    public String createRun(RunCreation runCreation, Model model) {
        try {
            populateModel(model);
            ProgramController programController = new ProgramController();
            ArrayList<Program> programs = new ArrayList<Program>();
            programs.add(programController.getProgram("DBSCAN"));
            runCreation.setProgramSettings(programs);
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
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            } catch (Exception e) {
            }
            return "runs/create";
        }

        //Create run file
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

    @RequestMapping(value="/runs/edit")
    public String editRun(@RequestParam(value="name", required=true) String fileName, RunCreation runCreation, Model model) {
        try {
            runCreation.parse(path, fileName);
            populateModel(model);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        return "runs/edit";
    }

    @RequestMapping(value="/runs/edit", method=RequestMethod.POST)
    public String editRun(@Valid RunCreation runCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        //Return to form if there were validation errors
        if (bindingResult.hasErrors()) {
            try {
                populateModel(model);
            } catch (ConnectException e) {
                return "runs/notRunning";
            } catch (Exception e) {
            }
            return "runs/edit";
        }

        //Create run file
        try {
            File file = new File(path + "/runs/" + runCreation.getName() + ".run");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            writer.write(runCreation.toString(path));
            writer.close();

            redirectAttributes.addFlashAttribute("success", "The run has been succcesfully edited.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("failure", "An error occurred during file writing.");
        }

        return "redirect:/runs";
    }

    @RequestMapping(value="/runs/delete")
    public String deleteRun(@RequestParam(value="name", required=true) String fileName, RedirectAttributes redirectAttributes) {
        File file = new File(path + "/runs/" + fileName + ".run");

        if (file.exists()) {
            file.delete();
        }

        redirectAttributes.addFlashAttribute("success", "The run has been succcesfully deleted.");

        return "redirect:/runs";
    }

    @RequestMapping(value="/getRun", method=RequestMethod.GET)
    public @ResponseBody RunCreation getRunCreationFromFileName(@RequestParam(value="name", required=true) String name) {
        RunCreation runCreation = new RunCreation();
        runCreation.parse(path, name);
        return runCreation;
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }
}
