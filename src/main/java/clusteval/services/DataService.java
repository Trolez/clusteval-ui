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

@Service
public class DataService {
    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
