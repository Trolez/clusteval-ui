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

import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.dataset.format.DataSetFormat;

import java.rmi.ConnectException;

import java.util.stream.Stream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.*;
import java.net.URLClassLoader;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

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
            Path path = Paths.get(getPath() + "/suppTest/distanceMeasures/");

            uploadUtility(utility.getFile(), path, utility.getName(), DistanceMeasure.class);

            redirectAttributes.addFlashAttribute("success", "Distance measure uploaded successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure");
        }

        return "redirect:/utilities/upload-distance-measure";
    }

    private void uploadUtility(MultipartFile file, Path path, String name, Class utilityClass) throws IOException {
        Files.copy(file.getInputStream(), path.resolve(file.getOriginalFilename()));

        File temporaryFile = convertToFile(file);

        JarFile jarFile = new JarFile(temporaryFile);
        Enumeration<JarEntry> entries = jarFile.entries();

        URL[] urls = { new URL("jar:file:" + convertToFile(file).getAbsolutePath() + "!/") };
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

                if (utilityClass.isAssignableFrom(c)) {
                    //The class in the jar is valid
                } else {
                    //The class in the jar is invalid
                    FileUtils.deleteQuietly(new File(path.resolve(file.getOriginalFilename()).toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Delete temporarily copied file
        FileUtils.deleteQuietly(temporaryFile);
    }

    private File convertToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        convertedFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
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
