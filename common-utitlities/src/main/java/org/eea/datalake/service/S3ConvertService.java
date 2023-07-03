package org.eea.datalake.service;

import java.io.File;
import java.io.IOException;

public interface S3ConvertService {

    void convertParquetToCSV(File parquetFile, File csvOutputFile);

    void convertParquetToJSON(File parquetFile, File jsonOutputFile);

    void convertParquetToXML(File parquetFile, File xmlOutputFile);

    void convertParquetToXLSX(File parquetFile, File xmlOutputFile);
}
