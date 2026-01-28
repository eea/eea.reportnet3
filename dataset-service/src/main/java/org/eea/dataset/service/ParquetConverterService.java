package org.eea.dataset.service;

import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.model.ImportFileInDremioInfo;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;

import java.io.File;
import java.util.List;

public interface ParquetConverterService {

    void convertCsvFilesToParquetFiles(ImportFileInDremioInfo importFileInDremioInfo, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception;

    void removeCsvFilesThatWillBeReplaced(S3PathResolver s3PathResolver, String tableSchemaName, String s3PathForCsvFolder, Long datasetId);

    FileTreatmentHelper getFileTreatmentHelper();

    File exportParquetToCsvFile(String existingTableQueryPath, String exportTableQueryPath, String exportTableS3Path, String exportedFileName, String exportedFilePath, S3PathResolver s3ExportPathResolver) throws Exception;

    void updateImportStatistics(String tableSchemaId, String numberOfRecordsToBeInserted, DataSetMetabase dataSetMetabase, String fileExtension);

    void deleteAllDataBeforeImport (ImportFileInDremioInfo importFileInDremioInfo, String datasetSchemaId) throws Exception;

    void handleEtlImportDataset(ImportFileInDremioInfo importFileInDremioInfo, File etlImportFolder, List<File> csvFiles, DataSetSchema dataSetSchema) throws Exception;
}
