package org.eea.dataset.controller;

import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eea.datalake.service.S3ConvertService;
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

    @Qualifier("getS3Client")
    @Autowired
    S3Client s3Client;

    private static final Logger LOG = LoggerFactory.getLogger(TestDremioControllerImpl.class);

    @GetMapping("run")
    public void run() {
        SqlRowSet rs = dremioJdbcTemplate.queryForRowSet("SELECT * FROM \"rn3-dataset.rn3-dataset\".\"tab.csv\";");
        while (rs.next()) {
            LOG.info(rs.getString("A") + "," + rs.getString("B") + "," + rs.getString("C") + "," + rs.getString("D"));
        }
    }

    @GetMapping("/exportJSON")
    public void exportJSON(@ApiParam(type = "String", value = "filename")
        @RequestParam("filename") String filename,
        HttpServletResponse response) {

        try {
            File myFile = getFile(filename);

            File toExport = new File("/reportnet3-data/input/importFiles/"+filename+".json");
            s3ConvertService.convertParquetToJSON(myFile, toExport);

            download(filename, response, toExport);
        } catch (IOException | ResponseStatusException ex) {
            LOG.error("IOException/ResponseStatusException ", ex);
        } catch (S3Exception e) {
            LOG.error("S3Exception ", e);
        }
    }

    @GetMapping("/exportXML")
    public void exportXML(@ApiParam(type = "String", value = "filename")
        @RequestParam("filename") String filename,
        HttpServletResponse response) {

        try {
            File myFile = getFile(filename);

            File toExport = new File("/reportnet3-data/input/importFiles/"+filename+".xml");
            s3ConvertService.convertParquetToXML(myFile, toExport);

            download(filename, response, toExport);
        } catch (IOException | ResponseStatusException ex) {
            LOG.error("IOException/ResponseStatusException ", ex);
        } catch (S3Exception e) {
            LOG.error("S3Exception ", e);
        }
    }

    @GetMapping("/exportExcel")
    public void exportExcel(@ApiParam(type = "String", value = "filename")
        @RequestParam("filename") String filename,
        HttpServletResponse response) {

        try {
            File myFile = getFile(filename);

            File toExport = new File("/reportnet3-data/input/importFiles/"+filename+".xlsx");
            s3ConvertService.convertParquetToXLSX(myFile, toExport);

            download(filename, response, toExport);
        } catch (IOException | ResponseStatusException ex) {
            LOG.error("IOException/ResponseStatusException ", ex);
        } catch (S3Exception e) {
            LOG.error("S3Exception ", e);
        }
    }

    private void download(String filename, HttpServletResponse response, File toExport)
        throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + FilenameUtils.getName(filename));
        LOG.info("FilenameUtils.getName(filename) value: {}", FilenameUtils.getName(filename));

        OutputStream out = response.getOutputStream();
        FileInputStream in = new FileInputStream(toExport);
        // copy from in to out
        IOUtils.copyLarge(in, out);
        out.close();
        in.close();
        // delete the file after downloading it
        FileUtils.forceDelete(toExport);
    }

    private File getFile(String filename) throws IOException {
        GetObjectRequest objectRequest = GetObjectRequest
            .builder()
            .key(filename +".parquet")
            .bucket("rn3-dataset")
            .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        byte[] data = objectBytes.asByteArray();

        // Write the data to a local file.
        File myFile = new File("/reportnet3-data/input/importFiles/"+ filename +".parquet");
        LOG.info("Local file {}", myFile);
        OutputStream os = new FileOutputStream(myFile);
        os.write(data);
        LOG.info("Successfully obtained bytes from an S3 object");
        os.close();
        return myFile;
    }
}
