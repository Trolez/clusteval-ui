package clusteval;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.io.*;

@Service
public class ClustEvalConnectionService {
    @Async
    public Future<Void> connectToServer() {
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "/home/troels/clusteval/clustevalBackendServer.jar", "-absRepoPath", "/home/troels/clusteval/clustevalDockerRepository", "-noDatabase");
        processBuilder.directory(new File("/home/troels/clusteval"));

        try {
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);

            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }
        } catch (IOException e) {
            
        }
        return new AsyncResult<Void>(null);
    }
}
