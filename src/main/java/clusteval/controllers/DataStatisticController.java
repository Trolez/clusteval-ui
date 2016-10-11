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
public class DataStatisticController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${port}")
    private int port;

    @Value("${clientId}")
    private int clientId;

    @RequestMapping(value="/data-statistics/getIntraInterSimilarityDistributionFile")
    public void getIntraInterSimilarityDistributionFile(HttpServletResponse response, @RequestParam(value="file", required=true) String file) {
        //We want to generate a CSV file, so we set the header accordingly
        response.setContentType("application/csv");
        response.setHeader("content-disposition","attachment;filename =filename.csv");

        try {
            ServletOutputStream  writer = response.getOutputStream();
            BufferedReader br = new BufferedReader(new FileReader(file));

            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                writer.println(currentLine.replace('\t', ','));
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {}
    }
}
