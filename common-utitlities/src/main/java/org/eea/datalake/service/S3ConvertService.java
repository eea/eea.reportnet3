package org.eea.datalake.service;

import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;
import java.util.zip.ZipOutputStream;

public interface S3ConvertService {


    void convertParquetToJSON(List<S3Object> exportFilenames, String tableName, Long datasetId, BufferedWriter bufferedWriter);

    void convertParquetToXML(File parquetFile, File xmlOutputFile);

    void convertParquetToXLSX(File parquetFile, File xmlOutputFile);

    void convertParquetToCSV(List<S3Object> exportFilenames, String nameDataset, Long datasetId);

    void convertParquetToJSON(List<S3Object> exportFilenames, String nameDataset, Long datasetId);

    void convertParquetToCSVinZIP(List<S3Object> exportFilenames, String nameDataset, Long datasetId, ZipOutputStream out);
}
