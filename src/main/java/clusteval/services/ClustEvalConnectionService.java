package clusteval;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

@Service
public class ClustEvalConnectionService {
    @Value("${absClustEvalPath}")
    private String absClustEvalPath;

    @Value("${absRepoPath}")
    private String absRepoPath;

    @Value("${absWorkingPath}")
    private String absWorkingPath;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Async
    public void connectToServer() {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", absClustEvalPath, "-absRepoPath", absRepoPath, "-noDatabase");
        processBuilder.directory(new File(absWorkingPath));

        try {
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);

            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("An error occurred when trying to connect to the ClustEval server.");
        }
    }
}
