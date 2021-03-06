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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;

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

    /* Overview page with all run results */
    @RequestMapping(value="/results")
    public String showResults(Model model) {
        String sql = "SELECT * FROM run_results " +
                      "INNER JOIN run_types ON (run_results.run_type_id = run_types.id)";

        ArrayList<Result> results = new ArrayList<Result>();

    	List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
    	for (Map row : rows) {
            Result result = new Result();
            result.setName(new String((byte[])row.get("unique_run_identifier")));
            result.setRunType(new String((byte[])row.get("name")));
    		results.add(result);
    	}

        model.addAttribute("results", results);

        return "results/index";
    }

    /* Display page for run results
     * Calls a more specialized
     * function based on run rype
     */
    @RequestMapping(value="/results/show")
    public String showResults(Model model, @RequestParam(value="name", required=true) String name) {
        String sql = "SELECT * FROM run_results " +
                      "INNER JOIN run_types ON (run_results.run_type_id = run_types.id) " +
                      "WHERE unique_run_identifier = '" + name + "'";

        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        String runType = new String((byte[])row.get("name"));

        model.addAttribute("runName", name);
        model.addAttribute("runType", runType);

        if (runType.equals("Parameter Optimization")) {
            return showResultsParameterOptimization(model, name);
        } else if (runType.equals("Clustering")) {
            return showResultsClustering(model, name);
        } else if (runType.equals("Data Analysis")) {
            return showResultsDataAnalysis(model, name);
        }

        return "results/show";
    }

    /* Display page for parameter optimization run results */
    public String showResultsParameterOptimization(Model model, String name) {
        String sql = "SELECT MAX(poi.quality) AS best_quality, poi.program_config_id, poi.data_config_id, clustering_quality_measures.alias AS quality_alias, program_configs.name AS program, data_configs.name AS data " +
        "FROM parameter_optimization_iterations AS poi " +
        "INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id) " +
        "INNER JOIN program_configs ON (program_configs.id = poi.program_config_id) " +
        "INNER JOIN data_configs ON (data_configs.id = poi.data_config_id) " +
        "WHERE unique_run_identifier = '" + name + "' " +
        "GROUP BY quality_alias, program, data, poi.program_config_id, poi.data_config_id " +
        "ORDER BY program ASC, data ASC, quality_alias ASC";

        ParameterOptimizationResult result = new ParameterOptimizationResult();
        result.setName(name);

        String currentProgram = "";
        String program = "";
        int programId;
        String currentData = "";
        String data = "";
        int dataId;
        String quality;
        double qualityValue;

        ParameterOptimizationResultProgram resultProgram = new ParameterOptimizationResultProgram();
        ParameterOptimizationResultData resultData = new ParameterOptimizationResultData();
        ParameterOptimizationResultQuality resultQuality;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            program = new String((byte[])row.get("program"));
            programId = (int)row.get("program_config_id");
            data = new String((byte[])row.get("data"));
            dataId = (int)row.get("data_config_id");
            quality = new String((byte[])row.get("quality_alias"));
            qualityValue = (double)row.get("best_quality");

            if (!program.equals(currentProgram)) {
                currentProgram = program;
                resultProgram = new ParameterOptimizationResultProgram();
                resultProgram.setName(currentProgram);
                resultProgram.setId(programId);
                result.addToPrograms(resultProgram);

                currentData = data;
                resultData = new ParameterOptimizationResultData();
                resultData.setName(currentData);
                resultData.setId(dataId);
                resultProgram.addToData(resultData);
            } else {
                if (!data.equals(currentData)) {
                    currentData = data;
                    resultData = new ParameterOptimizationResultData();
                    resultData.setName(currentData);
                    resultData.setId(dataId);
                    resultProgram.addToData(resultData);
                }
            }

            resultQuality = new ParameterOptimizationResultQuality();
            resultQuality.setName(quality);
            resultQuality.setValue(qualityValue);
            resultData.addToQualities(resultQuality);

            String paramSql = "SELECT poi.quality, poi.param_set_as_string AS param_set " +
                              "FROM parameter_optimization_iterations AS poi " +
                              "INNER JOIN clustering_quality_measures ON (poi.clustering_quality_measure_id = clustering_quality_measures.id) " +
                              "WHERE unique_run_identifier = '" + name + "' AND clustering_quality_measures.alias = '" + quality + "' " +
                              "AND program_config_id = '" + programId + "' AND data_config_id = '" + dataId + "' " +
                              "ORDER BY quality DESC LIMIT 1";

            List<Map<String, Object>> paramSets = jdbcTemplate.queryForList(paramSql);

            for (Map paramSet : paramSets) {
                String paramSetAsString = new String((byte[])paramSet.get("param_set"));
                resultQuality.setParameterSet(paramSetAsString);
            }
        }

        model.addAttribute("result", result);

        return "results/showParameterOptimization";
    }

    /* Display page for clustering run results */
    public String showResultsClustering(Model model, String name) {
        String sql = "SELECT quality, program_configs.program_config_id, data_configs.data_config_id, clustering_quality_measures.alias AS quality_alias, program_configs.name AS program, data_configs.name AS data FROM run_results " +
                     "INNER JOIN run_results_executions ON (run_results.id = run_results_executions.run_result_id) " +
                     "INNER JOIN run_results_clusterings ON (run_results_clusterings.run_results_execution_id = run_results_executions.id) " +
                     "INNER JOIN run_results_clustering_qualities ON (run_results_clustering_qualities.run_results_clustering_id = run_results_clusterings.id) " +
                     "INNER JOIN clustering_quality_measures ON (clustering_quality_measures.id = run_results_clustering_qualities.clustering_quality_measure_id) " +
                     "INNER JOIN program_configs ON (program_configs.id = run_results_clustering_qualities.program_config_id) " +
                     "INNER JOIN data_configs ON (data_configs.id = run_results_clustering_qualities.data_config_id) " +
                     "WHERE unique_run_identifier = '" + name + "' " +
                     "ORDER BY program ASC, data ASC, quality_alias ASC";

        ParameterOptimizationResult result = new ParameterOptimizationResult();
        result.setName(name);

        String currentProgram = "";
        String program = "";
        int programId;
        String currentData = "";
        String data = "";
        int dataId;
        String quality;
        double qualityValue;

        ParameterOptimizationResultProgram resultProgram = new ParameterOptimizationResultProgram();
        ParameterOptimizationResultData resultData = new ParameterOptimizationResultData();
        ParameterOptimizationResultQuality resultQuality;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            program = new String((byte[])row.get("program"));
            programId = (int)row.get("program_config_id");
            data = new String((byte[])row.get("data"));
            dataId = (int)row.get("data_config_id");
            quality = new String((byte[])row.get("quality_alias"));

            qualityValue = Double.parseDouble(new String((byte[])row.get("quality")));

            if (!program.equals(currentProgram)) {
                currentProgram = program;
                resultProgram = new ParameterOptimizationResultProgram();
                resultProgram.setName(currentProgram);
                resultProgram.setId(programId);
                result.addToPrograms(resultProgram);

                currentData = data;
                resultData = new ParameterOptimizationResultData();
                resultData.setName(currentData);
                resultData.setId(dataId);
                resultProgram.addToData(resultData);
            } else {
                if (!data.equals(currentData)) {
                    currentData = data;
                    resultData = new ParameterOptimizationResultData();
                    resultData.setName(currentData);
                    resultData.setId(dataId);
                    resultProgram.addToData(resultData);
                }
            }

            resultQuality = new ParameterOptimizationResultQuality();
            resultQuality.setName(quality);
            resultQuality.setValue(qualityValue);
            resultData.addToQualities(resultQuality);
        }

        model.addAttribute("result", result);

        return "results/clustering/show";
    }

    /* Display page for data analysis run results */
    public String showResultsDataAnalysis(Model model, String name) {
        String sql = "SELECT result.data_config_id, statistic_id, statistics.name, statistics.alias, data_configs.name AS data, data_configs.abs_path AS data_file, datasets.abs_path AS dataset_file FROM run_result_data_analysis_data_configs_statistics AS result " +
                     "INNER JOIN statistics ON (result.statistic_id = statistics.id) " +
                     "INNER JOIN data_configs ON (data_configs.id = result.data_config_id) " +
                     "INNER JOIN dataset_configs ON (dataset_configs.id = data_configs.dataset_config_id) " +
                     "INNER JOIN datasets ON (datasets.id = dataset_configs.dataset_id) " +
                     "WHERE unique_run_identifier = '" + name + "' " +
                     "ORDER BY data, statistic_id";

        DataAnalysisResult result = new DataAnalysisResult();
        result.setName(name);

        String currentDataConfig = "";
        String dataConfig = "";
        String currentDataStatistic = "";
        String dataStatistic = "";
        String dataStatisticAlias = "";

        DataAnalysisResultData resultDataConfig = new DataAnalysisResultData();
        DataAnalysisResultDataStatistic resultDataStatistic = new DataAnalysisResultDataStatistic();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            dataConfig = new String((byte[])row.get("data"));
            dataStatistic = new String((byte[])row.get("name"));
            dataStatisticAlias = new String((byte[])row.get("alias"));

            if (!dataConfig.equals(currentDataConfig)) {
                currentDataConfig = dataConfig;
                resultDataConfig = new DataAnalysisResultData();
                resultDataConfig.setName(currentDataConfig);

                //Read dataset file to determine number of samples and dimensionality
                Integer dimensionality = null;
                Integer numberOfSamples = 0;
                String datasetFile = new String((byte[])row.get("dataset_file"));
                try {
                    BufferedReader br = new BufferedReader(new FileReader(datasetFile));

                    String currentLine;
                    while ((currentLine = br.readLine()) != null) {
                        if (!currentLine.startsWith("//")) {
                            numberOfSamples++;
                            if (dimensionality == null) {
                                dimensionality = currentLine.split("\t").length - 1;
                            }
                        }
                    }
                } catch (Exception e) {}
                resultDataConfig.setNumberOfSamples(numberOfSamples);
                resultDataConfig.setDimensionality(dimensionality);

                boolean hasGoldstandard = false;
                String dataFile = new String((byte[])row.get("data_file"));
                try {
                    BufferedReader br = new BufferedReader(new FileReader(dataFile));

                    String currentLine;
                    while ((currentLine = br.readLine()) != null) {
                        hasGoldstandard = currentLine.startsWith("goldstandardConfig");
                    }
                } catch (Exception e) {}
                resultDataConfig.setHasGoldstandard(hasGoldstandard);

                result.addToDataConfigs(resultDataConfig);

                currentDataStatistic = dataStatistic;
                resultDataStatistic = new DataAnalysisResultDataStatistic();
                resultDataStatistic.setName(currentDataStatistic);
                resultDataStatistic.setAlias(dataStatisticAlias);

                if (currentDataStatistic.equals("ClassSizeDistributionDataStatistic")) {
                    resultDataConfig.setClassSizeDistribution(resultDataStatistic);
                } else if (currentDataStatistic.equals("NodeDegreeDistributionDataStatistic")) {
                    resultDataConfig.setNodeDegreeDistribution(resultDataStatistic);
                } else if (currentDataStatistic.equals("IntraInterDistributionDataStatistic")) {
                    resultDataConfig.setIntraInterSimilarityDistribution(resultDataStatistic);
                } else if (currentDataStatistic.equals("SimilarityDistributionDataStatistic")) {
                    resultDataConfig.setSimilarityDistribution(resultDataStatistic);
                } else {
                    resultDataConfig.addToDataStatistics(resultDataStatistic);
                }
            } else {
                if (!dataStatistic.equals(currentDataStatistic)) {
                    currentDataStatistic = dataStatistic;
                    resultDataStatistic = new DataAnalysisResultDataStatistic();
                    resultDataStatistic.setName(currentDataStatistic);
                    resultDataStatistic.setAlias(dataStatisticAlias);
                    if (currentDataStatistic.equals("ClassSizeDistributionDataStatistic")) {
                        resultDataConfig.setClassSizeDistribution(resultDataStatistic);
                    } else if (currentDataStatistic.equals("NodeDegreeDistributionDataStatistic")) {
                        resultDataConfig.setNodeDegreeDistribution(resultDataStatistic);
                    } else if (currentDataStatistic.equals("IntraInterDistributionDataStatistic")) {
                        resultDataConfig.setIntraInterSimilarityDistribution(resultDataStatistic);
                    } else if (currentDataStatistic.equals("SimilarityDistributionDataStatistic")) {
                        resultDataConfig.setSimilarityDistribution(resultDataStatistic);
                    } else {
                        resultDataConfig.addToDataStatistics(resultDataStatistic);
                    }
                }
            }

            //Read value from file
            String statisticFile = getPath() + "/results/" + name + "/analyses/" + currentDataConfig + "_" + dataStatistic + ".txt";
            resultDataStatistic.setFilePath(statisticFile);
            try {
                BufferedReader br = new BufferedReader(new FileReader(statisticFile));
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    resultDataStatistic.setValue(currentLine);
                }
            } catch (FileNotFoundException e) {
                resultDataStatistic.setValue("Error. File not found.");
            } catch (Exception e) {}
        }

        for (DataAnalysisResultData dataAnalysisResultData : result.getDataConfigs()) {
            Collections.sort(dataAnalysisResultData.getDataStatistics());
        }

        model.addAttribute("result", result);

        return "results/data_analysis/show";
    }

    /* Display page for clustering graphs */
    @RequestMapping(value="/results/show-clustering")
    public String showClustering(Model model,
                                 @RequestParam(value="name", required=true) String name,
                                 @RequestParam(value="program", required=true) String program,
                                 @RequestParam(value="data", required=true) String data,
                                 @RequestParam(value="param-set", required=false) String paramSet) {
        model.addAttribute("name", name);
        model.addAttribute("program", program);
        model.addAttribute("data", data);

        if (paramSet == null || paramSet.equals("")) {
            return "results/clustering/clusteringGraph";
        } else {
            model.addAttribute("paramSet", paramSet);
            return "results/showClustering";
        }
    }

    /* Find clustering for parameter optimization runs
     * Figures out which files to fetch for the clustering graph
     */
    @RequestMapping(value="/results/load-clustering")
    public String loadClustering(Model model,
                                 @RequestParam(value="name", required=true) String name,
                                 @RequestParam(value="program", required=true) String program,
                                 @RequestParam(value="data", required=true) String data,
                                 @RequestParam(value="param-set", required=true) String paramSet) {
        //Get iteration number from database
        String sql = "SELECT iteration FROM parameter_optimization_iterations WHERE unique_run_identifier = '" + name + "' " +
                     "AND program_config_id = '" + program + "' " +
                     "AND data_config_id = '" + data + "' ";

        //Narrow down the selection to the parameter set
        String[] parameters = StringUtils.split(paramSet, ',');
        for (int i = 0; i < parameters.length; i++) {
            String[] parts = StringUtils.split(parameters[i], '=');
            sql += "AND (param_set_as_string LIKE '%" + parts[0] + "=" + parts[1] + "' OR param_set_as_string LIKE '%" + parts[0] + "=" + parts[1] + ",%') ";
        }
        sql += "LIMIT 1";

        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        int iteration = (int)row.get("iteration");

        model.addAttribute("iteration", iteration);

        //Get program config name from database
        sql = "SELECT name FROM program_configs WHERE id = '" + program + "'";
        row = jdbcTemplate.queryForMap(sql);
        String programConfigName = new String((byte[])row.get("name"));

        //Get data config name from database
        sql = "SELECT name FROM data_configs WHERE id = '" + data + "'";
        row = jdbcTemplate.queryForMap(sql);
        String dataConfigName = new String((byte[])row.get("name"));

        File clusterFile = new File(getPath() + "/results/" + name + "/clusters/" + programConfigName + "_" + dataConfigName + "." + iteration + ".results.conv");

        File pcaDirectory = new File(getPath() + "/results/" + name + "/inputs/" + programConfigName + "_" + dataConfigName);

        File pcaFile = findPca(pcaDirectory);

        model.addAttribute("clusterFile", clusterFile.getPath());
        model.addAttribute("pcaFile", pcaFile.getPath());
        model.addAttribute("name", name);
        model.addAttribute("program", program);
        model.addAttribute("data", data);

        return "results/loadClustering";
    }

    /* Find clustering for clustering runs
     * Figures out which files to fetch for the clustering graph
     */
    @RequestMapping(value="/results/load-clustering-cluster")
    public String loadClustering(Model model,
                                 @RequestParam(value="name", required=true) String name,
                                 @RequestParam(value="program", required=true) String program,
                                 @RequestParam(value="data", required=true) String data) {
        //Get program config name from database
        String sql = "SELECT name FROM program_configs WHERE id = '" + program + "'";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        String programConfigName = new String((byte[])row.get("name"));

        //Get data config name from database
        sql = "SELECT name FROM data_configs WHERE id = '" + data + "'";
        row = jdbcTemplate.queryForMap(sql);
        String dataConfigName = new String((byte[])row.get("name"));

        File clusterFile = new File(getPath() + "/results/" + name + "/clusters/" + programConfigName + "_" + dataConfigName + ".1.results.conv");

        File pcaDirectory = new File(getPath() + "/results/" + name + "/inputs/" + programConfigName + "_" + dataConfigName);

        File pcaFile = findPca(pcaDirectory);

        model.addAttribute("clusterFile", clusterFile.getPath());
        model.addAttribute("pcaFile", pcaFile.getPath());
        model.addAttribute("name", name);
        model.addAttribute("program", program);
        model.addAttribute("data", data);

        return "results/loadClustering";
    }

    /* Given a cluster file and a pca file
     * Create a CSV file for Highcharts
       To display */
    @RequestMapping(value="/results/get-clustering")
    public void getClustering(HttpServletResponse response, @RequestParam(value="clusterFile", required=true) String clusterFile, @RequestParam(value="pcaFile", required=true) String pcaFile) {
        //We want to generate a CSV file, so we set the header accordingly
        response.setContentType("application/csv");
        response.setHeader("content-disposition","attachment;filename =filename.csv");

        String[] clusters;
        String clusterString = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(clusterFile));

            int i = 1;
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (i == 2) {
                    clusterString = currentLine.split("\t")[1];
                    break;
                }
                i++;
            }

            clusters = clusterString.split(";");

            HashMap<String, String> coordinates = new HashMap<String, String>();

            br = new BufferedReader(new FileReader(pcaFile));
            while ((currentLine = br.readLine()) != null) {
                String[] parts = currentLine.split("\t");

                coordinates.put(parts[0], parts[1] + "," + parts[2]);
                i++;
            }

            ServletOutputStream  writer = response.getOutputStream();

            i = 1;
            for (String cluster : clusters) {
                writer.print("Cluster " + i + "\n");

                String[] clusterObjects = cluster.split(",");
                for (String clusterObject : clusterObjects) {
                    writer.print(clusterObject + "," + coordinates.get(clusterObject.split(":")[0]) + "\n");
                }

                i++;
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {}
    }

    public String showResults(Model model, @RequestParam(value="name", required=true) String name, @RequestParam(value="program", required=true) String program, @RequestParam(value="data", required=true) String data, boolean cluster) {
        String sql = "SELECT * FROM " +
                     "(SELECT DISTINCT value, paramname FROM parameter_optimization_iterations " +
                     "WHERE program_config_id = '" + program + "' AND data_config_id = '" + data + "') AS blabla " +
                     "ORDER BY paramname, length(value), value";

        Map<String, ArrayList<String>> paramValues = new HashMap<String, ArrayList<String>>();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        String currentParam = "";
        ArrayList<String> values = new ArrayList<String>();
        for (Map<String, Object> row : rows) {
            String param = new String((byte[])row.get("paramname"));
            String value = new String((byte[])row.get("value"));
            if (!param.equals(currentParam)) {
                currentParam = param;
                values = new ArrayList<String>();
                paramValues.put(param, values);
            }

            values.add(value);
        }

        model.addAttribute("parameters", paramValues);
        model.addAttribute("name", name);
        model.addAttribute("program", program);
        model.addAttribute("data", data);

        if (!cluster) {
            return "results/showParameterSlider";
        } else {
            return "results/showClusteringSlider";
        }
    }

    @RequestMapping(value="/results/get-parameter-sliders")
    public String showParameterSliders(Model model, @RequestParam(value="name", required=true) String name, @RequestParam(value="program", required=true) String program, @RequestParam(value="data", required=true) String data) {
        return showResults(model, name, program, data, false);
    }

    @RequestMapping(value="/results/get-clustering-sliders")
    public String showClusteringSliders(Model model, @RequestParam(value="name", required=true) String name, @RequestParam(value="program", required=true) String program, @RequestParam(value="data", required=true) String data) {
        return showResults(model, name, program, data, true);
    }

    @RequestMapping(value="/results/get-parameter-graph")
    public void getParameterGraph(Model model, @RequestParam(value="active-parameter", required=true) String activeParameter,
                                  @RequestParam(value="parameters", required=true) String parameters,
                                  @RequestParam(value="name") String name,
                                  @RequestParam(value="program") String program,
                                  @RequestParam(value="data") String data, HttpServletResponse response) {
        //We want to generate a CSV file, so we set the header accordingly
        response.setContentType("application/csv");
        response.setHeader("content-disposition","attachment;filename =filename.csv");

        String sql = "SELECT poi.value AS value, poi.clustering_quality_measure_id, poi.quality AS quality, quality.alias AS quality_alias, poi.clustering_quality_measure_id AS quality_id FROM parameter_optimization_iterations AS poi " +
                     "INNER JOIN clustering_quality_measures AS quality ON (poi.clustering_quality_measure_id = quality.id) " +
                     "WHERE unique_run_identifier = '" + name + "' " +
                     "AND program_config_id = '" + program + "' " +
                     "AND data_config_id = '" + data + "' " +
                     "AND paramname = '" + activeParameter + "' ";

        //Narrow down the selection to the locked parameters
        String[] lockedParameters = StringUtils.split(parameters, ',');
        for (int i = 0; i < lockedParameters.length; i++) {
            String[] parts = StringUtils.split(lockedParameters[i], '=');
            sql += "AND (param_set_as_string LIKE '%" + parts[0] + "=" + parts[1] + "' OR param_set_as_string LIKE '%" + parts[0] + "=" + parts[1] + ",%')";
        }

        sql += "ORDER BY length(value), value, iteration, quality_id ASC";

        String qualitiesSql = "SELECT DISTINCT quality.alias AS quality_alias, poi.clustering_quality_measure_id AS quality_id FROM parameter_optimization_iterations AS poi " +
                              "INNER JOIN clustering_quality_measures AS quality ON (poi.clustering_quality_measure_id = quality.id) " +
                              "WHERE unique_run_identifier = '" + name + "' " +
                              "AND program_config_id = '" + program + "' " +
                              "AND data_config_id = '" + data + "' " +
                              "ORDER BY quality_id ASC";

        try {
            ServletOutputStream  writer = response.getOutputStream();

            ArrayList<Integer> qualities = new ArrayList<Integer>();
            ArrayList<String> rowToPrint = new ArrayList<String>();
            ArrayList<String> headerRow = new ArrayList<String>();
            Integer currentQualityId;

            //Print out header
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(qualitiesSql);
            for (Map row : rows) {
                headerRow.add(new String((byte[])row.get("quality_alias")));
                qualities.add((Integer)row.get("quality_id"));
            }

            writer.print(activeParameter + "," + StringUtils.join(headerRow, ',') + "\n");

            rows = jdbcTemplate.queryForList(sql);

            String currentValue = "";
            String oldValue = "";
            int qualityIndex = 0;
            for (Map row : rows) {
                if (currentValue.equals("")) {
                    currentValue = new String((byte[])row.get("value"));
                    oldValue = currentValue;
                } else {
                    currentValue = new String((byte[])row.get("value"));
                }

                //Check if we have reached a new value
                if (!currentValue.equals(oldValue)) {
                    //A new row should be written out at this point
                    writer.print(oldValue + "," + StringUtils.join(rowToPrint, ',') + "\n");
                    qualityIndex = 0;
                    oldValue = currentValue;
                    rowToPrint = new ArrayList<String>();
                }

                //If there are missing quality values, write out 0's
                currentQualityId = (int)row.get("quality_id");
                while (currentQualityId > qualities.get(qualityIndex)) {
                    rowToPrint.add("0");
                    qualityIndex++;
                }
                rowToPrint.add(Double.toString((double)row.get("quality")));
                qualityIndex++;
            }

            writer.print(currentValue + "," + StringUtils.join(rowToPrint, ',') + "\n");

            writer.flush();
            writer.close();
        } catch(IOException e) {
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

    private File findPca(File directory) {
        File pcaFile = new File("");
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (FilenameUtils.getExtension(file.getPath()).equals("PCA")) {
                    pcaFile = file;
                }
            } else if (file.isDirectory()) {
                pcaFile = findPca(file);
            }
        }

        return pcaFile;
    }
}
