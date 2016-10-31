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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.serverclient.BackendClient;
import de.clusteval.run.RUN_STATUS;
import de.wiwie.wiutils.utils.Pair;

import java.rmi.ConnectException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;

@Controller
public class RunController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping("/runs")
    public String showRuns(Model model) {
        ArrayList<Run> runs = new ArrayList<Run>();
        ArrayList<String> runNames = new ArrayList<String>();
        ArrayList<String> runResumes = new ArrayList<String>();
        ArrayList<String> runningRuns;
        ArrayList<String> finishedRuns;
        ArrayList<String> terminatedRuns;

        try {
            BackendClient backendClient = getBackendClient();

            runNames = new ArrayList<String>(backendClient.getRuns());
            for (String runName : runNames) {
                runs.add(new Run(runName));
            }
            //Collections.sort(runs, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(runs, new Comparator<Run>() {
                public int compare(Run left, Run right) {
                    //Use the 'edit of' line to determine the proper order of runs
                    String leftName = left.getName();
                    String rightName = right.getName();

                    try {
                        BufferedReader leftReader = new BufferedReader(new FileReader(getPath() + "/runs/" + left.getName() + ".run"));
                        BufferedReader rightReader = new BufferedReader(new FileReader(getPath() + "/runs/" + right.getName() + ".run"));
                        String currentLine;

                        while ((currentLine = leftReader.readLine()) != null) {
                            if (currentLine.startsWith("#editOf")) {
                                leftName = currentLine.split("=")[1].trim();
                                left.setEdited(true);
                                break;
                            }
                        }

                        while ((currentLine = rightReader.readLine()) != null) {
                            if (currentLine.startsWith("#editOf")) {
                                rightName = currentLine.split("=")[1].trim();
                                right.setEdited(true);
                                break;
                            }
                        }
                    } catch (Exception e){}

                    if (leftName.equals(rightName) && !left.getEdited()) {
                        return -1;
                    } else if (leftName.equals(rightName) && !right.getEdited()) {
                        return 1;
                    }

                    return leftName.compareToIgnoreCase(rightName);
                }
            });

            model.addAttribute("runs", runs);

            runResumes = new ArrayList<String>(backendClient.getRunResumes());
            model.addAttribute("resumes", runResumes);

            runningRuns = new ArrayList<String>(backendClient.getRunningRuns());
            model.addAttribute("runningRuns", runningRuns);

            finishedRuns = new ArrayList<String>(backendClient.getFinishedRuns());
            model.addAttribute("finishedRuns", finishedRuns);

            terminatedRuns = new ArrayList<String>(backendClient.getTerminatedRuns());
            model.addAttribute("terminatedRuns", terminatedRuns);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        return "runs/index";
    }

    @RequestMapping(value="/runs", method=RequestMethod.POST)
    public String performRun(@ModelAttribute Run run, Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = getBackendClient();

            backendClient.performRun(run.getName());

            logger.info("An instance of the run '" + run.getName() + "' was started.");
            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully started.");
        } catch (ConnectException e) {
            redirectAttributes.addFlashAttribute("failure", "Could not connect to the Clusteval server. Is it running?");
        } catch (Exception e) {
            logger.error("An unknown error occurred when attempting to start an instance of the run '" + run.getName() + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
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
            logger.error("An unknown error occurred when attempting to resume the run '" + run.getName() + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
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
            logger.error("An unknown error occurred when attempting to terminate the run '" + run.getName() + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
    }

    @RequestMapping(value="/delete-run")
    public String deleteRun(@RequestParam(value="name", required=true) String runName, Model model, RedirectAttributes redirectAttributes) {
        String path = getPath();

        try {
            File directory = new File(path + "/results/" + runName);
            FileUtils.deleteDirectory(directory);
            redirectAttributes.addFlashAttribute("success", "The run '" + runName + "' has been succcesfully deleted.");
        } catch (Exception e) {
            logger.error("An unknown error occurred when attempting to delete the run '" + runName + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "There was an error when deleting the run.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
    }

    @RequestMapping(value="/getRunStatus", method=RequestMethod.GET)
    public @ResponseBody Map<String, Pair<RUN_STATUS, Float>> getRunStatus() {
        Map<String, Pair<RUN_STATUS, Float>> status = null;
        try {
            BackendClient backendClient = getBackendClient();

            status = backendClient.getMyRunStatus();
        } catch (ConnectException e) {
        } catch (Exception e) {
        }

        return status;
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
            /*ProgramController programController = new ProgramController();
            ArrayList<Program> programs = new ArrayList<Program>();
            programs.add(programController.getProgram("DBSCAN"));
            runCreation.setProgramSettings(programs);*/
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        return "runs/create";
    }

    @RequestMapping(value="/runs/create", method=RequestMethod.POST)
    public String createRun(@Valid RunCreation runCreation, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        /*Create a new array of program settings
        * This fixes an error that occurs if a program
        * was selected and then deselected in the form */
        ArrayList<Program> programSettings = new ArrayList<Program>();
        for (Program program : runCreation.getProgramSettings()) {
            if (program != null && program.getParameters() != null && program.getParameters().size() > 0) {
                programSettings.add(program);
            }
        }
        runCreation.setProgramSettings(programSettings);

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
            File file = new File(getPath() + "/runs/" + runCreation.getName() + ".run");

            if (!file.exists()) {
                file.createNewFile();
            } else {
                //Prevent existing runs from being overwritten, show user error
                bindingResult.rejectValue("name", "name", "A run with this name already exists");
                try {
                    populateModel(model);
                } catch (ConnectException e) {
                    return "runs/notRunning";
                } catch (Exception e) {
                }
                return "runs/create";
            }

            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            writer.write(runCreation.toString(getPath()));
            writer.close();

            redirectAttributes.addFlashAttribute("success", "The run has been succcesfully created.");
        } catch (IOException e) {
            logger.error("An unknown error occurred when attempting to create a new run '" + runCreation.getName() + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "An error occurred during file writing.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
    }

    @RequestMapping(value="/runs/edit")
    public String editRun(@RequestParam(value="name", required=true) String fileName, RunCreation runCreation, Model model) {
        try {
            runCreation.parse(getPath(), fileName);

            //Remove appended date if it is there
            int index = runCreation.getName().lastIndexOf('_');
            if (index > -1) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
                try {
                    if (dateFormat.parse(runCreation.getName(), new ParsePosition(index+1)) != null) {
                        runCreation.setName(runCreation.getName().substring(0, index));
                    }
                } catch (Exception e){
                    System.err.println(e.toString());
                }
            }

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

        String originalName = runCreation.getOriginalName();

        //Append date to run file to show when it was edited
        String dateAppend = new SimpleDateFormat("_yyyy-MM-dd-kk-mm-ss").format(new Date());
        runCreation.setName(runCreation.getName() + dateAppend);

        //Create run file
        try {
            File file = new File(getPath() + "/runs/" + runCreation.getName() + ".run");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);

            writer.write("#editOf = " + originalName + "\n");

            writer.write(runCreation.toString(getPath()));
            writer.close();

            redirectAttributes.addFlashAttribute("success", "The run has been succcesfully edited.");
        } catch (IOException e) {
            logger.error("An unknown error occurred when attempting to edit the run '" + runCreation.getName() + "'. Exception: " + e);
            redirectAttributes.addFlashAttribute("failure", "An error occurred during file writing.");
        }

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
    }

    @RequestMapping(value="/runs/delete")
    public String deleteRunConfig(@RequestParam(value="name", required=true) String fileName, Model model, RedirectAttributes redirectAttributes) {
        File file = new File(getPath() + "/runs/" + fileName + ".run");

        if (file.exists()) {
            file.delete();
        }

        redirectAttributes.addFlashAttribute("success", "The run has been succcesfully deleted.");

        redirectAttributes.addFlashAttribute("redirectUrl", "/runs");

        return "redirect:/redirect";
    }

    @RequestMapping(value="/getRun", method=RequestMethod.GET)
    public @ResponseBody RunCreation getRunCreationFromFileName(@RequestParam(value="name", required=true) String name) {
        RunCreation runCreation = new RunCreation();
        runCreation.parse(getPath(), name);
        return runCreation;
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
