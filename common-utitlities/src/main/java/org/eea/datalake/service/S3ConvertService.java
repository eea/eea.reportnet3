package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

public interface S3ConvertService {


    void convertParquetToJSON(File parquetFile, File jsonOutputFile);

    void convertParquetToXML(File parquetFile, File xmlOutputFile);

    void convertParquetToXLSX(File parquetFile, File xmlOutputFile);

    void convertParquetToCSV(List<S3Object> exportFilenames, String nameDataset, Long datasetId);

    void convertParquetToCSVinZIP(List<S3Object> exportFilenames, String nameDataset, Long datasetId, ZipOutputStream out);
}
