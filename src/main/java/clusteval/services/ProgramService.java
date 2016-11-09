package clusteval;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.serverclient.BackendClient;
import de.clusteval.program.r.RProgram;

import java.rmi.ConnectException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

@Service
public class ProgramService {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void createProgram(ProgramCreation programCreation, BindingResult bindingResult) throws IOException {
        boolean isRProgram = false;

        File temporaryFile = null;

        try {
            temporaryFile = convertToFile(programCreation.getExecutableFile());
            JarFile jarFile = new JarFile(temporaryFile);
            Enumeration<JarEntry> entries = jarFile.entries();

            URL[] urls = { new URL("jar:file:" + temporaryFile.getAbsolutePath() + "!/") };
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

                    if (RProgram.class.isAssignableFrom(c)) {
                        isRProgram = true;
                    } else {
                        isRProgram = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {}

        //Delete temporarily copied file
        FileUtils.deleteQuietly(temporaryFile);

        if (!isRProgram) {
            //Invocation format is required for this type of program
            if (programCreation.getInvocationFormat().isEmpty()) {
                bindingResult.rejectValue("invocationFormat", "invocationFormat", "Invocation format is required for this type of program");
                return;
            }
        }

        String programFileName = "";

        try {
            //Copy program file to repository
            ArrayList<String> parameters = new ArrayList<String>();
            ArrayList<String> optimizableParameters = new ArrayList<String>();
            if (programCreation.getParameters() != null) {
                for (ProgramCreationParameter parameter : programCreation.getParameters()) {
                    parameters.add(parameter.getName());
                    if (parameter.getOptimizable()) {
                        optimizableParameters.add(parameter.getName());
                    }
                }
            }

            File programConfigFile = new File(getPath() + "/programs/configs/" + programCreation.getName() + ".config");
            FileWriter writer = new FileWriter(programConfigFile);

            if (isRProgram) {
                Path path = Paths.get(getPath() + "/programs");

                Files.copy(programCreation.getExecutableFile().getInputStream(), path.resolve(programCreation.getExecutableFile().getOriginalFilename()));

                String typeName = "";
                try {
                    typeName = programCreation.getExecutableFile().getOriginalFilename().split("\\.")[0];
                } catch (Exception e) {}

                //Write basic program configuration
                writer.write("type = " + typeName + "\n");
                writer.write("parameters = " + StringUtils.join(parameters, ',') + "\n");
                writer.write("optimizationParameters = " + StringUtils.join(optimizableParameters, ',') + "\n");
                writer.write("alias = " + programCreation.getAlias() + "\n\n");
            } else {
                Path path = Paths.get(getPath() + "/programs/" + programCreation.getName());
                File filePath = new File(getPath() + "/programs/" + programCreation.getName());

                if (!filePath.exists()) {
                    filePath.mkdir();
                }

                Files.copy(programCreation.getExecutableFile().getInputStream(), path.resolve(programCreation.getExecutableFile().getOriginalFilename()));

                //Write basic program configuration
                writer.write("program = " + programCreation.getName() + "/" + programCreation.getExecutableFile().getOriginalFilename() + "\n");
                writer.write("parameters = " + StringUtils.join(parameters, ',') + "\n");
                writer.write("optimizationParameters = " + StringUtils.join(optimizableParameters, ',') + "\n");
                writer.write("compatibleDataSetFormats = " + StringUtils.join(programCreation.getCompatibleDataSetFormats(), ',') + "\n");
                writer.write("outputFormat = " + programCreation.getOutputFormat() + "\n");
                writer.write("alias = " + programCreation.getAlias() + "\n\n");
                writer.write("[invocationFormat]\n");
                writer.write("invocationFormat = " + programCreation.getInvocationFormat());
            }

            //Write out program parameter configurations
            if (programCreation.getParameters() != null) {
                for (ProgramCreationParameter parameter : programCreation.getParameters()) {
                    writer.write("\n\n");
                    writer.write("[" + parameter.getName() + "]\n");
                    writer.write("desc = " + parameter.getDescription() + "\n");
                    writer.write("type = " + parameter.getType() + "\n");
                    writer.write("def = " + parameter.getDefaultValue() + "\n");
                    if (parameter.getType() == 0) {
                        writer.write("options = " + StringUtils.join(parameter.getOptions(), ','));
                    } else {
                        writer.write("minValue = " + parameter.getMinValue() + "\n");
                        writer.write("maxValue = " + parameter.getMaxValue());
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            throw(e);
        }
    }

    public void editProgram(ProgramCreation programCreation, BindingResult bindingResult) throws IOException {
        if (programCreation.getExecutableFile().getSize() == 0) {
            //A file was *not* chosen. Create a MultipartFile from existing file
            Path path = Paths.get(getPath());
            String name = "";
            String originalName = "";
            String contentType = "";
            byte[] content = null;

            try {
                BufferedReader br = new BufferedReader(new FileReader(path + "/programs/configs/" + programCreation.getOriginalName() + ".config"));
                String currentLine;

                while ((currentLine = br.readLine()) != null) {
                    String line = currentLine.substring(currentLine.indexOf("=") + 1).trim();
                    if (currentLine.startsWith("program")) {
                        String[] parts = line.split("/");
                        path = Paths.get(getPath() + "/programs/" + parts[0] + "/" + parts[1]);
                        name = parts[1];
                        originalName = parts[1];
                        contentType = Files.probeContentType(path);
                        try {
                            content = Files.readAllBytes(path);
                        } catch (IOException e) {
                            throw(e);
                        }
                        programCreation.setExecutableFile(new MockMultipartFile(name, originalName, contentType, content));
                    } else if (currentLine.startsWith("type")) {
                        path = Paths.get(getPath() + "/programs/" + line + ".jar");
                        name = line + ".jar";
                        originalName = line + ".jar";
                        contentType = Files.probeContentType(path);
                        try {
                            content = Files.readAllBytes(path);
                        } catch (IOException e) {
                            throw(e);
                        }
                        programCreation.setExecutableFile(new MockMultipartFile(name, originalName, contentType, content));
                    }
                }
            } catch (Exception e) {}
        }

        //To edit the program, we just delete the original and create a new one
        try {
            deleteProgram(programCreation.getOriginalName());
            createProgram(programCreation, bindingResult);
        } catch (IOException e) {
            throw(e);
        }
    }

    public void deleteProgram(String name) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(getPath() + "/programs/configs/" + name + ".config"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("type")) {
                    //Delete program jar file
                    File jarFile = new File(getPath() + "/programs/" + currentLine.split("=")[1].trim() + ".jar");

                    jarFile.delete();
                }
                if (currentLine.startsWith("program")) {
                    //Delete program folder
                    String directoryName = currentLine.split("=")[1].split("/")[0].trim();
                    File directory = new File(getPath() + "/programs/" + directoryName);
                    FileUtils.deleteDirectory(directory);
                }
            }

            //Delete program configuration file
            File configurationFile = new File(getPath() + "/programs/configs/" + name + ".config");
            configurationFile.delete();
        } catch (Exception e) {}
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
