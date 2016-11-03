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

//Utility classes
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormatParser;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.RunResultFormatParser;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.data.statistics.DataStatisticCalculator;

import java.rmi.ConnectException;

import java.util.stream.Stream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileAlreadyExistsException;
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
        utility.setTypeName("Distance measure");
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

            if (uploadUtility(utility.getFile(), path, new Class[] { DistanceMeasure.class })) {
                redirectAttributes.addFlashAttribute("success", "Distance measure uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("failure", "The jar file contained invalid classes");
            }
        } catch (FileAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure. File already exists");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload distance measure");
        }

        return "redirect:/utilities/upload-distance-measure";
    }

    @RequestMapping(value="/utilities/upload-data-set-format")
    public String uploadDataSetFormat(Utility utility, Model model) {
        utility.setTypeName("Data set format");
        return "utilities/uploadDataSetFormat";
    }

    @RequestMapping(value="/utilities/upload-data-set-format", method=RequestMethod.POST)
    public String uploadDataSetFormat(@Valid Utility utility, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "utilities/uploadDataSetFormat";
        }

        try {
            //Copy data set format file to repository
            if (utility.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/supp/formats/dataset");

            if (uploadUtility(utility.getFile(), path, new Class[] { DataSetFormat.class, DataSetFormatParser.class })) {
                redirectAttributes.addFlashAttribute("success", "Data set format uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("failure", "The jar file contained invalid classes");
            }
        } catch (FileAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set format. File already exists");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set format");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set format");
        }

        return "redirect:/utilities/upload-data-set-format";
    }

    @RequestMapping(value="/utilities/upload-run-result-format")
    public String uploadRunResultFormat(Utility utility, Model model) {
        utility.setTypeName("Run result format");
        return "utilities/uploadRunResultFormat";
    }

    @RequestMapping(value="/utilities/upload-run-result-format", method=RequestMethod.POST)
    public String uploadRunResultFormat(@Valid Utility utility, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "utilities/uploadRunResultFormat";
        }

        try {
            //Copy run result format file to repository
            if (utility.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/supp/formats/runresult");

            if (uploadUtility(utility.getFile(), path, new Class[] { RunResultFormat.class, RunResultFormatParser.class })) {
                redirectAttributes.addFlashAttribute("success", "Run result format uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("failure", "The jar file contained invalid classes");
            }
        } catch (FileAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload run result format. File already exists");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload run result format");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload run result format");
        }

        return "redirect:/utilities/upload-run-result-format";
    }

    @RequestMapping(value="/utilities/upload-data-set-type")
    public String uploadDataSetType(Utility utility, Model model) {
        utility.setTypeName("Data set type");
        return "utilities/uploadDataSetType";
    }

    @RequestMapping(value="/utilities/upload-data-set-type", method=RequestMethod.POST)
    public String uploadDataSetType(@Valid Utility utility, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "utilities/uploadDataSetType";
        }

        try {
            //Copy data set type file to repository
            if (utility.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/supp/types/dataset");

            if (uploadUtility(utility.getFile(), path, new Class[] { DataSetType.class })) {
                redirectAttributes.addFlashAttribute("success", "Data set type uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("failure", "The jar file contained invalid classes");
            }
        } catch (FileAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set type. File already exists");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set type");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data set type");
        }

        return "redirect:/utilities/upload-data-set-type";
    }

    @RequestMapping(value="/utilities/upload-data-statistic")
    public String uploadDataStatisitc(Utility utility, Model model) {
        utility.setTypeName("Data statistic");
        return "utilities/uploadDataStatistic";
    }

    @RequestMapping(value="/utilities/upload-data-statistic", method=RequestMethod.POST)
    public String uploadDataStatistic(@Valid Utility utility, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "utilities/uploadDataStatistic";
        }

        try {
            //Copy data statistic file to repository
            if (utility.getFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("failure", "Failed to store empty file");
            }
            Path path = Paths.get(getPath() + "/supp/statistics/data");

            if (uploadUtility(utility.getFile(), path, new Class[] { DataStatistic.class, DataStatisticCalculator.class })) {
                redirectAttributes.addFlashAttribute("success", "Data statistic uploaded successfully");
            } else {
                redirectAttributes.addFlashAttribute("failure", "The jar file contained invalid classes");
            }
        } catch (FileAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data statistic. File already exists");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data statistic");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("failure", "Failed to upload data statistic");
        }

        return "redirect:/utilities/upload-data-statistic";
    }

    private boolean uploadUtility(MultipartFile file, Path path, Class[] utilityClasses) throws IOException {
        boolean valid = false;
        Files.copy(file.getInputStream(), path.resolve(file.getOriginalFilename()));

        File temporaryFile = convertToFile(file);

        JarFile jarFile = new JarFile(temporaryFile);
        Enumeration<JarEntry> entries = jarFile.entries();

        URL[] urls = { new URL("jar:file:" + convertToFile(file).getAbsolutePath() + "!/") };
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        for (int i = 0; i < utilityClasses.length; i++){
            Class utilityClass = utilityClasses[i];
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
                        valid = true;
                        break;
                    } else {
                        //The class in the jar is invalid
                        valid = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!valid) {
                //Delete temporarily copied files
                FileUtils.deleteQuietly(temporaryFile);
                FileUtils.deleteQuietly(new File(path.resolve(file.getOriginalFilename()).toString()));

                return false;
            }
        }

        //Delete temporarily copied file
        FileUtils.deleteQuietly(temporaryFile);

        return valid;
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
