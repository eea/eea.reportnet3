package org.eea.dataset.service.impl;

import org.apache.commons.io.IOUtils;
import org.eea.dataset.service.BigDataDatasetService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.ParquetConverterService;
import org.eea.dataset.service.S3HandlerService;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
public class BigDataDatasetServiceImpl implements BigDataDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(BigDataDatasetServiceImpl.class);

    @Value("${importPath}")
    private String importPath;

    @Autowired
    DatasetService datasetService;

    @Autowired
    S3HandlerService s3HandlerService;

    @Autowired
    ParquetConverterService parquetConverterService;

    @Override
    public void importBigData(Long datasetId, Long dataflowId, Long providerId, String tableSchemaId,
                              MultipartFile file, Boolean replace, Long integrationId, String delimiter, String fmeJobId) {


        /*
         * Part 1 is done:
         * Lets say we got a zip file
         *
         * extract it
         *
         * convert csv files to parquet
         *
         * send parquet files to s3
         *
         * */

        List<File> filesToImport = storeImportFiles(file, datasetId);
        Map<String, String> parquetFileNamesAndPaths =  parquetConverterService.convertCsvFilesToParquetFiles(filesToImport);
        for (Map.Entry<String, String> parquetFileNameAndPath : parquetFileNamesAndPaths.entrySet()) {
            s3HandlerService.uploadFileToBucket("reportnet", "", parquetFileNameAndPath.getKey(), parquetFileNameAndPath.getValue());
        }






        /*
         * Part 2:
         *
         * Add job and handle it
         * */

        /*
         * Part 3:
         *
         * Add checks for wrong filenames or sth
         * */

        /*
         * Part 4:
         *
         * Case where zip file is in s3 and we need to download it first
         * */

        /*
         * Part 5:
         *
         * Case where we get notification from s3 that zip file has been uploaded
         * */
    }

    private List<File> storeImportFiles(MultipartFile multipartFile, Long datasetId){
        List<File> files = new ArrayList<>();

        try (InputStream input = multipartFile.getInputStream()) {

            // Prepare the folder where files will be stored
            File root = new File(importPath);
            File folder = new File(root, datasetId.toString());
            String saveLocationPath = folder.getCanonicalPath();

            if (!folder.exists()) {
                folder.mkdir();
            }


            try (ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry = zip.getNextEntry();

                try {
                    while (null != entry) {
                        String entryName = entry.getName();
                        String mimeType = datasetService.getMimetype(entryName);
                        File file = new File(folder, entryName);
                        String filePath = file.getCanonicalPath();

                        // Prevent Zip Slip attack or skip if the entry is a directory
                        if ((entryName.split("/").length > 1)
                                || !FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || entry.isDirectory()
                                || !filePath.startsWith(saveLocationPath + File.separator)) {
                            LOG.error("Ignored file from ZIP: {}", entryName);
                            entry = zip.getNextEntry();
                            continue;
                        }

                        // Store the file in the persistence volume
                        try (FileOutputStream output = new FileOutputStream(file)) {
                            IOUtils.copyLarge(zip, output);
                            LOG.info("Stored file {}", file.getPath());
                        } catch (Exception e) {
                            LOG.error("Unexpected error! Error in copyLarge for saveLocationPath {}. Message: {}", saveLocationPath, e.getMessage());
                            throw e;
                        }

                        entry = zip.getNextEntry();
                        files.add(file);
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error processing file! Message: {}", e.getMessage());
                    throw e;
                }


            } catch (Exception e) {
                LOG.error("Unexpected error! Error in unzipAndStore for datasetId {}. Message: {}", datasetId, e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            LOG.error("Unexpected error! Error in fileManagement for datasetId {} and  Message: {}", datasetId, e.getMessage());
        }
        return files;
    }
}
