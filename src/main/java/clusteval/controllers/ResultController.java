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

    @RequestMapping(value="/results")
    public String showResults(Model model) {
        String sql = "SELECT * FROM run_results " +
                      "INNER JOIN run_types ON (run_results.run_type_id = run_types.id)";

        ArrayList<String> results = new ArrayList<String>();

    	List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
    	for (Map row : rows) {
    		results.add(new String((byte[])row.get("unique_run_identifier")));
    	}

        model.addAttribute("results", results);

        return "results/index";
    }

    @RequestMapping(value="/results/show")
    public String showResults(Model model, @RequestParam(value="name", required=true) String name) {
        String sql = "SELECT * FROM run_results " +
                      "INNER JOIN run_types ON (run_results.run_type_id = run_types.id) " +
                      "WHERE unique_run_identifier = '" + name + "'";

        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        String runType = new String((byte[])row.get("name"));

        model.addAttribute("runType", runType);

        if (runType.equals("Parameter Optimization")) {
            return showResultsParameterOptimization(model, name);
        }

        return "results/show";
    }

    public String showResultsParameterOptimization(Model model, String name) {
        String sql = "SELECT MAX(poi.quality) AS best_quality, clustering_quality_measures.alias AS quality_alias, program_configs.name AS program, data_configs.name AS data " +
        "FROM parameter_optimization_iterations AS poi " +
        "INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id) " +
        "INNER JOIN program_configs ON (program_configs.id = poi.program_config_id) " +
        "INNER JOIN data_configs ON (data_configs.id = poi.data_config_id) " +
        "WHERE unique_run_identifier = '" + name + "' " +
        "GROUP BY (quality_alias, program, data) " +
        "ORDER BY program ASC, data ASC, quality_alias ASC";

        /*
        SELECT poi.param_set_as_string, poi.*, clustering_quality_measures.alias AS quality_alias, programs.alias AS program_name, datasets.alias AS dataset_name
        FROM parameter_optimization_iterations AS poi
        INNER JOIN parameter_optimization_max_qual_rows AS pomqr ON (poi.run_results_parameter_optimizations_parameter_set_iteration_id = pomqr.run_results_parameter_optimizations_parameter_set_iteration_id)
        INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id)
        INNER JOIN programs ON (programs.id = pomqr.program_id)
        INNER JOIN datasets ON (datasets.id = pomqr.dataset_id)
        WHERE unique_run_identifier = '08_24_2016-15_37_14_small_run_4'
        ORDER BY program_name ASC, dataset_name ASC, quality_alias ASC

        SELECT MAX(poi.quality), clustering_quality_measures.alias AS quality_alias, program_configs.name AS program_name, data_configs.name AS dataset_name
        FROM parameter_optimization_iterations AS poi
        INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id)
        INNER JOIN program_configs ON (program_configs.id = poi.program_config_id)
        INNER JOIN data_configs ON (data_configs.id = poi.data_config_id)
        WHERE unique_run_identifier = '08_24_2016-15_37_14_small_run_4'
        GROUP BY (quality_alias, program_name, dataset_name)
        ORDER BY program_name ASC, dataset_name ASC, quality_alias ASC
        */

        ParameterOptimizationResult result = new ParameterOptimizationResult();

        String currentProgram = "";
        String program = "";
        String currentData = "";
        String data = "";
        String quality;
        double qualityValue;

        ParameterOptimizationResultProgram resultProgram = new ParameterOptimizationResultProgram();
        ParameterOptimizationResultData resultData = new ParameterOptimizationResultData();
        ParameterOptimizationResultQuality resultQuality;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            program = new String((byte[])row.get("program"));
            data = new String((byte[])row.get("data"));
            quality = new String((byte[])row.get("quality_alias"));
            qualityValue = (double)row.get("best_quality");

            if (!program.equals(currentProgram)) {
                currentProgram = program;
                resultProgram = new ParameterOptimizationResultProgram();
                resultProgram.setName(currentProgram);
                result.addToPrograms(resultProgram);

                currentData = data;
                resultData = new ParameterOptimizationResultData();
                resultData.setName(currentData);
                resultProgram.addToData(resultData);

                resultQuality = new ParameterOptimizationResultQuality();
                resultQuality.setName(quality);
                resultQuality.setValue(qualityValue);
                resultData.addToQualities(resultQuality);
            } else {
                if (!data.equals(currentData)) {
                    currentData = data;
                    resultData = new ParameterOptimizationResultData();
                    resultData.setName(currentData);
                    resultProgram.addToData(resultData);
                }

                resultQuality = new ParameterOptimizationResultQuality();
                resultQuality.setName(quality);
                resultQuality.setValue(qualityValue);
                resultData.addToQualities(resultQuality);
            }
        }

        model.addAttribute("result", result);

        return "results/showParameterOptimization";
    }

    @RequestMapping(value="/results/graphs")
    public String resultsTest(Model model) {
        return "results/graphs";
    }

    @RequestMapping(value="/resultTest")
    public @ResponseBody ParameterOptimizationResult showResultsParameterOptimizationTest(Model model, String name) {
        String sql = "SELECT MAX(poi.quality) AS best_quality, clustering_quality_measures.alias AS quality_alias, program_configs.name AS program, data_configs.name AS data " +
        "FROM parameter_optimization_iterations AS poi " +
        "INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id) " +
        "INNER JOIN program_configs ON (program_configs.id = poi.program_config_id) " +
        "INNER JOIN data_configs ON (data_configs.id = poi.data_config_id) " +
        "WHERE unique_run_identifier = '" + name + "' " +
        "GROUP BY (quality_alias, program, data) " +
        "ORDER BY program ASC, data ASC, quality_alias ASC";

        /*
        SELECT poi.param_set_as_string, poi.*, clustering_quality_measures.alias AS quality_alias, programs.alias AS program_name, datasets.alias AS dataset_name
        FROM parameter_optimization_iterations AS poi
        INNER JOIN parameter_optimization_max_qual_rows AS pomqr ON (poi.run_results_parameter_optimizations_parameter_set_iteration_id = pomqr.run_results_parameter_optimizations_parameter_set_iteration_id)
        INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id)
        INNER JOIN programs ON (programs.id = pomqr.program_id)
        INNER JOIN datasets ON (datasets.id = pomqr.dataset_id)
        WHERE unique_run_identifier = '08_24_2016-15_37_14_small_run_4'
        ORDER BY program_name ASC, dataset_name ASC, quality_alias ASC

        SELECT MAX(poi.quality), clustering_quality_measures.alias AS quality_alias, program_configs.name AS program_name, data_configs.name AS dataset_name
        FROM parameter_optimization_iterations AS poi
        INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id)
        INNER JOIN program_configs ON (program_configs.id = poi.program_config_id)
        INNER JOIN data_configs ON (data_configs.id = poi.data_config_id)
        WHERE unique_run_identifier = '08_24_2016-15_37_14_small_run_4'
        GROUP BY (quality_alias, program_name, dataset_name)
        ORDER BY program_name ASC, dataset_name ASC, quality_alias ASC
        */

        ParameterOptimizationResult result = new ParameterOptimizationResult();

        String currentProgram = "";
        String program = "";
        String currentData = "";
        String data = "";
        String quality;
        double qualityValue;

        ParameterOptimizationResultProgram resultProgram = new ParameterOptimizationResultProgram();
        ParameterOptimizationResultData resultData = new ParameterOptimizationResultData();
        ParameterOptimizationResultQuality resultQuality;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            program = new String((byte[])row.get("program"));
            data = new String((byte[])row.get("data"));
            quality = new String((byte[])row.get("quality_alias"));
            qualityValue = (double)row.get("best_quality");

            if (!program.equals(currentProgram)) {
                currentProgram = program;
                resultProgram = new ParameterOptimizationResultProgram();
                resultProgram.setName(currentProgram);
                result.addToPrograms(resultProgram);

                currentData = data;
                resultData = new ParameterOptimizationResultData();
                resultData.setName(currentData);
                resultProgram.addToData(resultData);

                resultQuality = new ParameterOptimizationResultQuality();
                resultQuality.setName(quality);
                resultQuality.setValue(qualityValue);
                resultData.addToQualities(resultQuality);
            } else {
                if (!data.equals(currentData)) {
                    currentData = data;
                    resultData = new ParameterOptimizationResultData();
                    resultData.setName(currentData);
                    resultProgram.addToData(resultData);
                }

                resultQuality = new ParameterOptimizationResultQuality();
                resultQuality.setName(quality);
                resultQuality.setValue(qualityValue);
                resultData.addToQualities(resultQuality);
            }
        }

        model.addAttribute("result", result);

        return result;
        //return "results/showParameterOptimization";
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