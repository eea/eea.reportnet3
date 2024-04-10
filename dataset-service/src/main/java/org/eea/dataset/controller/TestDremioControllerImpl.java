package org.eea.dataset.controller;

import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eea.datalake.service.S3ConvertService;
import org.eea.datalake.service.S3Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RequestMapping("/dremio")
@RestController
@Import(org.eea.datalake.service.impl.S3ConvertServiceImpl.class)
public class TestDremioControllerImpl {

    @Qualifier("dremioJdbcTemplate")
    @Autowired
    JdbcTemplate dremioJdbcTemplate;

    @Autowired
    S3ConvertService s3ConvertService;

/*    @Qualifier("getS3Client")
    @Autowired
    S3Client s3Client;*/

    private static final Logger LOG = LoggerFactory.getLogger(TestDremioControllerImpl.class);

    @GetMapping("run")
    public void run() {
        SqlRowSet rs = dremioJdbcTemplate.queryForRowSet("SELECT * FROM \"rn3-dataset.rn3-dataset\".\"tab.csv\";");
        while (rs.next()) {
            LOG.info(rs.getString("A") + "," + rs.getString("B") + "," + rs.getString("C") + "," + rs.getString("D"));
        }
    }
}
