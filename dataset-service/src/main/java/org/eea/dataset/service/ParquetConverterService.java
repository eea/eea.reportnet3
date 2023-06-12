package org.eea.dataset.service;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ParquetConverterService {

    Map<String, String> convertCsvFilesToParquetFiles(List<File> csvFiles);
    void convertCsvToParquet(String csvFilePath, String parquetFilePath);

}
