package org.eea.datalake.service;

import org.eea.datalake.service.model.S3PathResolver;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface S3ConvertService {

    void convertParquetToCSV(File parquetFile, File csvOutputFile);

    void convertParquetToJSON(File parquetFile, File jsonOutputFile);

    void convertParquetToXML(File parquetFile, File xmlOutputFile);

    void convertParquetToXLSX(File parquetFile, File xmlOutputFile);

    void convert(S3PathResolver s3PathResolver, String nameDataset) throws IOException;
}
