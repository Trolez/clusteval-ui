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
//import de.clusteval.data.distance;

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
public class UtilityController {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/utilities")
    public String utilitiesIndex(Model model) {
        return "utilities/index";
    }

    @RequestMapping(value="/utilities/upload-distance-measure")
    public String uploadDistanceMeasure(Utility utility, Model model) {
        utility.setTypeName("distance measure");
        return "utilities/uploadDistanceMeasure";
    }

    @RequestMapping(value="/utilities/upload-distance-measure", method=RequestMethod.POST)
    public String uploadDistanceMeasure(@Valid Utility utility, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "utilities/uploadDistanceMeasure";
        }

        try {
            //Copy distance measure file to repository
            if (utility.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/supp/distanceMeasures/");

            String extension = "";
            try {
                extension = utility.getFile().getOriginalFilename().split("\\.")[1];
            } catch (Exception e) {}

            String distanceMeasureFileName = utility.getName() + "." + extension;

            Files.copy(utility.getFile().getInputStream(), path.resolve(distanceMeasureFileName));
            redirectAttributes.addFlashAttribute("success", "Distance measure uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure");
        }

        return "redirect:/utilities/upload-distance-measure";
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
