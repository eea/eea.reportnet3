package org.eea.datalake.service;

import java.io.File;
import java.io.IOException;

public interface S3ConvertService {

    void convertParquetToCSV(File parquetFile, File csvOutputFile) throws IOException;

    void convertParquetToJSON(File parquetFile, File jsonOutputFile) throws IOException;
}
