package org.eea.dataset.controller;

import io.swagger.annotations.ApiParam;
import org.eea.datalake.service.S3ConvertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.*;

@RequestMapping("/dremio")
@RestController
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
            System.out.println(rs.getString("A") + "," + rs.getString("B") + "," + rs.getString("C") + "," + rs.getString("D"));
        }
    }


    @GetMapping("/export/{filename}")
    public void export(@ApiParam(type = "String",
        value = "Filename") @PathVariable("filename") String filename) throws IOException {

        try {
            GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(filename+".parquet")
                .bucket("rn3-dataset")
                .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();

            // Write the data to a local file.
            File myFile = new File("/reportnet3-data/input/importFiles/"+filename+".parquet");
            LOG.info("Local file {}", myFile);
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            System.out.println("Successfully obtained bytes from an S3 object");
            os.close();

            s3ConvertService.convertParquetToCSV(myFile, new File("/reportnet3-data/input/importFiles/"+filename+".csv"));


        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }


    }


}
