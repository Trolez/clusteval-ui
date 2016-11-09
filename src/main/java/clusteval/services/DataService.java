package clusteval;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.serverclient.BackendClient;
import java.rmi.ConnectException;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DataService {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void createData(DataCreation dataCreation) throws IOException {
        try {
            //Create dataconfig file
            File dataConfigFile = new File(getPath() + "/data/configs/" + dataCreation.getName() + ".dataconfig");

            if (!dataConfigFile.exists()) {
                dataConfigFile.createNewFile();
            }

            FileWriter writer = new FileWriter(dataConfigFile);

            writer.write("datasetConfig = " + dataCreation.getName() + "\n");

            if (dataCreation.getGoldstandardFile().getSize() > 0) {
                writer.write("goldstandardConfig = " + dataCreation.getName());
            }
            writer.close();

            String dataSetFileName = "";

            //Copy data set file to repository
            Path path = Paths.get(getPath() + "/data/datasets/" + dataCreation.getName());
            File filePath = new File(getPath() + "/data/datasets/" + dataCreation.getName());

            if (!filePath.exists()) {
                filePath.mkdir();
            }

            String extension = "";
            try {
                dataSetFileName = dataCreation.getDataSetFile().getOriginalFilename();
            } catch (Exception e) {}

            File dataSetFile = new File(getPath() + "/data/datasets/" + dataCreation.getName() + "/" + dataSetFileName);
            writer = new FileWriter(dataSetFile);
            writer.write("// dataSetFormat = " + dataCreation.getDataSetFormat() + "\n");
            writer.write("// dataSetType = " + dataCreation.getDataSetType() + "\n");
            writer.write("// dataSetFormatVersion = 1\n");
            writer.write("// alias = " + dataCreation.getName() + "\n");
            writer.write("// websiteVisibility = show_always\n");

            BufferedReader br = new BufferedReader(new InputStreamReader(dataCreation.getDataSetFile().getInputStream()));
            String currentLine = "";
            while((currentLine = br.readLine()) != null) {
                if (!currentLine.startsWith("//")) {
                    writer.write(currentLine + "\n");
                }
            }

            writer.close();

            //Create dsconfig file
            File dataSetConfigFile = new File(getPath() + "/data/datasets/configs/" + dataCreation.getName() + ".dsconfig");

            if (!dataSetConfigFile.exists()) {
                dataSetConfigFile.createNewFile();
            }

            writer = new FileWriter(dataSetConfigFile);

            writer.write("datasetName = " + dataCreation.getName() + "\n");
            writer.write("datasetFile = " + dataSetFileName);

            writer.close();
        } catch (IOException e) {
            throw(e);
        }

        if (dataCreation.getGoldstandardFile().getSize() > 0) {
            String goldstandardFileName = "";
            try {
                //Copy goldstandard file to repository
                Path path = Paths.get(getPath() + "/data/goldstandards/" + dataCreation.getName());
                File filePath = new File(getPath() + "/data/goldstandards/" + dataCreation.getName());

                if (!filePath.exists()) {
                    filePath.mkdir();
                }

                try {
                    goldstandardFileName = dataCreation.getGoldstandardFile().getOriginalFilename();
                } catch (Exception e) {}

                Files.copy(dataCreation.getGoldstandardFile().getInputStream(), path.resolve(goldstandardFileName));

                //Create gsconfig file
                File goldstandardConfigFile = new File(getPath() + "/data/goldstandards/configs/" + dataCreation.getName() + ".gsconfig");

                if (!goldstandardConfigFile.exists()) {
                    goldstandardConfigFile.createNewFile();
                }

                FileWriter writer = new FileWriter(goldstandardConfigFile);

                writer.write("goldstandardName = " + dataCreation.getName() + "\n");
                writer.write("goldstandardFile = " + goldstandardFileName);

                writer.close();
            } catch (IOException e) {
                throw(e);
            }
        }
    }

    public void deleteData(String name) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(getPath() + "/data/configs/" + name + ".dataconfig"));
            String currentLine;

            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("datasetConfig")) {
                    BufferedReader br2 = new BufferedReader(new FileReader(getPath() + "/data/datasets/configs/" + currentLine.split("=")[1].trim() + ".dsconfig"));
                    String currentLine2;
                    while ((currentLine2 = br2.readLine()) != null) {
                        System.err.println("Deleting dataset folder - " + currentLine2);
                        if (currentLine2.startsWith("datasetName")) {
                            //Delete dataset folder
                            String directoryName = currentLine2.split("=")[1].trim();
                            File directory = new File(getPath() + "/data/datasets/" + directoryName);
                            FileUtils.deleteDirectory(directory);
                        }
                    }

                    //Delete dataset configuration file
                    File configurationFile = new File(getPath() + "/data/datasets/configs/" + currentLine.split("=")[1].trim() + ".dsconfig");
                    configurationFile.delete();
                }
                if (currentLine.startsWith("goldstandardConfig")) {
                    BufferedReader br2 = new BufferedReader(new FileReader(getPath() + "/data/goldstandards/configs/" + currentLine.split("=")[1].trim() + ".gsconfig"));
                    String currentLine2;
                    while ((currentLine2 = br2.readLine()) != null) {
                        System.err.println("Deleting goldstandard folder - " + currentLine2);
                        if (currentLine2.startsWith("goldstandardName")) {
                            //Delete goldstandard folder
                            String directoryName = currentLine2.split("=")[1].trim();
                            File directory = new File(getPath() + "/data/goldstandards/" + directoryName);
                            FileUtils.deleteDirectory(directory);
                        }
                    }

                    //Delete goldstandard configuration file
                    File configurationFile = new File(getPath() + "/data/goldstandards/configs/" + currentLine.split("=")[1].trim() + ".gsconfig");
                    configurationFile.delete();
                }
            }

            //Delete data configuration file
            File configurationFile = new File(getPath() + "/data/configs/" + name + ".dataconfig");
            configurationFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
