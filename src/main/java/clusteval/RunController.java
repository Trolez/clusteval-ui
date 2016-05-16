package clusteval;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;
import de.clusteval.run.RUN_STATUS;
import de.wiwie.wiutils.utils.Pair;

import java.rmi.ConnectException;

import java.util.ArrayList;
import java.util.Map;

import java.io.*;

@Controller
public class RunController {
    @Value("${port}")
    private int port;

    @RequestMapping("/runs")
    public String showRuns(Model model) {
        ArrayList<String> runNames = new ArrayList<String>();
        ArrayList<Run> runs = new ArrayList<Run>();
        try {
            BackendClient backendClient = new BackendClient(new String[0]);
            runNames = new ArrayList<String>(backendClient.getRuns());

            for (String runName : runNames) {
                runs.add(new Run(runName));
            }

            Map<String, Pair<RUN_STATUS, Float>> runStatus = backendClient.getMyRunStatus();

            model.addAttribute("status", runStatus);

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
            BackendClient backendClient = new BackendClient(new String[0]);
            backendClient.performRun(run.getName());

            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully started.");
        } catch (ConnectException e) {
            redirectAttributes.addFlashAttribute("failure", "Could not connect to the Clusteval server. Is it running?");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");
        }

        return "redirect:/runs";
    }

    @RequestMapping(value="/runs/create")
    public String createRun(Model model) {
        ArrayList<String> dataSets = new ArrayList<String>();
        ArrayList<String> programs = new ArrayList<String>();
        try {
            BackendClient backendClient = new BackendClient(new String[0]);

            dataSets = new ArrayList<String>(backendClient.getDataSets());
            model.addAttribute("dataSets", dataSets);

            programs = new ArrayList<String>(backendClient.getPrograms());
            model.addAttribute("programs", programs);
        } catch (ConnectException e) {
            return "runs/notRunning";
        } catch (Exception e) {
        }

        return "runs/create";
    }

    @RequestMapping(value="/runs/create", method=RequestMethod.POST)
    public String createRun(Model model, RedirectAttributes redirectAttributes) {
        //Create test file
        try {
            File file = new File("/home/troels/Desktop/testfile.run");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            writer.write("This is a just a test!");
            writer.close();

            redirectAttributes.addFlashAttribute("success", "The run has been succcesfully created.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("failure", "An error occurred during file writing.");
        }

        return "redirect:/runs";
    }
}
