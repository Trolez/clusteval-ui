package clusteval;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.clusteval.serverclient.BackendClient;

import java.util.ArrayList;

@Controller
public class RunController {

    @RequestMapping("/runs")
    public String showRuns(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        ArrayList<String> runNames = new ArrayList<String>();
        ArrayList<Run> runs = new ArrayList<Run>();
        try {
            BackendClient backendClient = new BackendClient(new String[0]);
            runNames = new ArrayList<String>(backendClient.getRuns());

            for (String runName : runNames) {
                runs.add(new Run(runName));
            }
        } catch (Exception e) {
        }

        model.addAttribute("name", name);

        model.addAttribute("runs", runs);

        return "runs/index";
    }

    @RequestMapping(value="/runs", method=RequestMethod.POST)
    public String greetingSubmit(@ModelAttribute Run run, Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = new BackendClient(new String[0]);

            redirectAttributes.addFlashAttribute("success", "The run '" + run.getName() + "' was successfully started.");
            redirectAttributes.addFlashAttribute("failure", "An unknown error occurred.");

            //backendClient.performRun(run.getName());
        } catch (Exception e) {
        }

        return "redirect:/runs";
    }
}
