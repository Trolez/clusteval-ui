package clusteval;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.serverclient.BackendClient;
import de.wiwie.wiutils.utils.Pair;

import java.rmi.ConnectException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.*;

@Controller
public class ResultController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JdbcTemplate jdbcTemplate;

    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @Autowired
    public ResultController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value="/show-results")
    public String showResults(Model model, @RequestParam(value="name", required=true) String name) {
        String sql = "SELECT * FROM programs";

        ArrayList<String> test = new ArrayList<String>();

    	List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
    	for (Map row : rows) {
    		test.add(new String((byte[])row.get("alias")));
    	}

        model.addAttribute("programs", test);

        return "results/show";
    }

    @RequestMapping(value="/results")
    public String resultsTest(Model model) {
        return "results/graphs";
    }

    @RequestMapping(value="/resultTest")
    public @ResponseBody Map<Pair<String, String>, Map<String, Double>> test(@RequestParam(value="name", required=true) String name) {
        Map<Pair<String, String>, Map<String, Double>> data;
        try {
            BackendClient backendClient = getBackendClient();

            data = backendClient.getRunResults(name);
            return data;
        } catch (ConnectException e) {
        } catch (Exception e) {
        }
        return null;
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
