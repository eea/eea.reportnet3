package org.eea.dataset.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ParquetConverterService {

    void convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception;

    void removeCsvFilesThatWillBeReplaced(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForCsvFolder);

    FileTreatmentHelper getFileTreatmentHelper();

    File exportParquetToCsvFile(String existingTableQueryPath, String exportTableQueryPath, String exportTableS3Path, String exportedFileName, String exportedFilePath, S3PathResolver s3ExportPathResolver) throws Exception;

}
