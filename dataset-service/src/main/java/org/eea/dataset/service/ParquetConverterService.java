package org.eea.dataset.service;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.model.ImportFileInDremioInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ParquetConverterService {

    void convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception;

}
