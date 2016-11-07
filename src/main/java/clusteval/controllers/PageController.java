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
import java.rmi.ConnectException;

import java.util.ArrayList;
import java.io.*;

@Controller
public class PageController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping("/")
    public String homePage(Model model) {
        boolean connectedToServer = true;
        String absRepoPath = "";
        int numberOfRuns = 0;
        int numberOfDataConfigs = 0;
        int numberOfProgramConfigs = 0;

        try {
            BackendClient backendClient = getBackendClient();
            absRepoPath = getPath();
            numberOfRuns = backendClient.getRuns().size();
            numberOfDataConfigs = backendClient.getDataConfigurations().size();
            numberOfProgramConfigs = backendClient.getProgramConfigurations().size();
        } catch (ConnectException e) {
            connectedToServer = false;
        } catch (Exception e) {}

        model.addAttribute("connected", connectedToServer);
        model.addAttribute("absRepoPath", absRepoPath);
        model.addAttribute("numberOfRuns", numberOfRuns);
        model.addAttribute("numberOfDataConfigs", numberOfDataConfigs);
        model.addAttribute("numberOfProgramConfigs", numberOfProgramConfigs);
        model.addAttribute("port", port);

        return "pages/home";
    }

    @RequestMapping("/redirect")
    public String redirectPage(Model model) {
        return "pages/redirect";
    }

    @RequestMapping("/connect")
    public String connectToServer(Model model, RedirectAttributes redirectAttributes) {
        ClustEvalConnectionService service = new ClustEvalConnectionService();
        service.connectToServer();
        redirectAttributes.addFlashAttribute("success", "The ClustEval server is starting up");

        return "redirect:/";
    }

    @RequestMapping("/disconnect")
    public String disconnectFromServer(Model model, RedirectAttributes redirectAttributes) {
        try {
            BackendClient backendClient = getBackendClient();
            backendClient.shutdownFramework();
            redirectAttributes.addFlashAttribute("success", "The ClustEval server has been shut down");
        } catch (ConnectException e) {
        } catch (Exception e) {}

        return "redirect:/";
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
