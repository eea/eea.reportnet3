package org.eea.datalake.service;

import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;
import java.util.zip.ZipOutputStream;

public interface S3ConvertService {


    void convertParquetToJSON(List<S3Object> exportFilenames, String tableName, Long datasetId, BufferedWriter bufferedWriter, DatasetTypeEnum datasetTypeEnum);

    void convertParquetToXML(File parquetFile, File xmlOutputFile);

    void convertParquetToXLSX(File parquetFile, File xmlOutputFile);

    void convertParquetToCSVinZIP(File csvFile, String tableName, ZipOutputStream out);

    File createCSVFile(List<S3Object> exportFilenames, String tableName, Long datasetId, DatasetTypeEnum datasetTypeEnum);

    File createEmptyCSVFile(String tableName, Long datasetId, List<String> headers);

    void createJsonFile(List<S3Object> exportFilenames, String tableName, Long datasetId, DatasetTypeEnum datasetTypeEnum);
}
