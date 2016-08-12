package clusteval;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.*;

import de.clusteval.serverclient.BackendClient;
import java.rmi.ConnectException;

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

        try {
            BackendClient backendClient = getBackendClient();
        } catch (ConnectException e) {
            connectedToServer = false;
        } catch (Exception e) {}

        model.addAttribute("connected", connectedToServer);

        return "pages/home";
    }

    @RequestMapping("/redirect")
    public String redirectPage(Model model) {
        return "pages/redirect";
    }

    private BackendClient getBackendClient() throws ConnectException, Exception {
        return new BackendClient(new String[]{"-port", Integer.toString(port), "-clientId", Integer.toString(clientId)});
    }
}
