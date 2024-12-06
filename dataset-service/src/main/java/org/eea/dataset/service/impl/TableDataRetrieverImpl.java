package org.eea.dataset.service.impl;

import lombok.RequiredArgsConstructor;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.TableDataRetriever;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@Service
@RequiredArgsConstructor
public class TableDataRetrieverImpl implements TableDataRetriever {

  private final DatasetMetabaseService datasetMetabaseService;
  private final S3Helper s3Helper;
  private final DataSetMetabaseRepository dataSetMetabaseRepository;
  private final DatasetSchemaService datasetSchemaService;
  private final DataFlowController dataFlowController;
  private final DataCollectionController.DataCollectionControllerZuul dataCollectionControllerZuul;
  private final S3Service s3Service;

  private static final Logger LOG = LoggerFactory.getLogger(TableDataRetrieverImpl.class);

  @Override
  public ResponseEntity<?> getTablesUpdatedAfterRelease(Long dpDatasetId) {
    String message;
    HttpStatus status;
    try {
      DataSetMetabase datasetMetabase = getDataSetMetabase(dpDatasetId);
      Long dcDatasetId = dataCollectionControllerZuul.findDataCollectionIdByDatasetSchemaId(datasetMetabase.getDatasetSchema());
      Long dataProviderCode = dataSetMetabaseRepository.findDataProviderIdById(dpDatasetId);
      String dpFolderName = s3Service.formatFolderName(dataProviderCode, S3_DATA_PROVIDER_PATTERN);
      String datasetSchemaId = datasetMetabase.getDatasetSchema();
      if (isBigData(datasetMetabase.getDataflowId())) {
        List<String> tableNamesFromSchema = getTableNamesFromSchema(datasetSchemaId);
        List<S3Object> dcList = getListOfS3Files(dcDatasetId, S3_TABLE_NAME_ROOT_DC_FOLDER_PATH, dataProviderCode)
            .stream()
            .filter(s3Object -> tableNamesFromSchema.contains(getTableNameFromKey(s3Object.key())))
            .filter(s3Object -> providerFolderNameExists(s3Object.key(), dpFolderName))
            .sorted(Comparator.comparing(S3Object::lastModified).reversed())
            .collect(Collectors.toList());
        List<S3Object> dpList = getListOfS3Files(dpDatasetId, S3_PROVIDER_PATH, dataProviderCode)
            .stream()
            .filter(s3Object -> !s3Object.key().endsWith("/"))
            .filter(s3Object -> tableNamesFromSchema.contains(getTableNameFromKey(s3Object.key())))
            .sorted(Comparator.comparing(S3Object::lastModified).reversed())
            .collect(Collectors.toList());
        return new ResponseEntity<>(compareLists(dcList, dpList, datasetSchemaService.getDataSchemaById(datasetSchemaId)), HttpStatus.OK);
      }
    } catch (EEAException e) {
      message = e.getMessage();
      status = HttpStatus.BAD_REQUEST;
      LOG.error("Error while processing data. Message: {}", e.getMessage());
      return new ResponseEntity<>(message, status);
    } catch (Exception e) {
      message = e.getMessage();
      status = HttpStatus.INTERNAL_SERVER_ERROR;
      LOG.error("Unexpected error! Error while processing data. Message: {}", e.getMessage());
      return new ResponseEntity<>(message, status);
    }
    message = "Please provide the correct data";
    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
  }

  @Override
  public ResponseEntity<?> checkDatasetEditedAfterRelease(Long datasetId) {
    String message;
    HttpStatus status;
    ResponseEntity<?> result = getTablesUpdatedAfterRelease(datasetId);
    if (result != null && result.getStatusCode() == HttpStatus.OK && result.getBody() != null) {
      status = HttpStatus.OK;
      return new ResponseEntity<>(checkIfAnyTableHasChanges(result.getBody()), status);
    }
    message = "Please provide the correct data";
    return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
  }

  /**
   * Return the list of tables on the reporting dataset
   * And the flag that tables needs a release or not.
   * True if the table needs a release , false if not
   *
   * @param dcList          Data collection list
   * @param dpList          Data provider list
   * @param dataSetSchemaVO The schema from mongo
   * @return The list
   */
  private HashMap<String, Boolean> compareLists(List<S3Object> dcList, List<S3Object> dpList, DataSetSchemaVO dataSetSchemaVO) {
    HashMap<String, Boolean> finalComparisonResult = new HashMap<>();
    if (dpList.isEmpty() && !dcList.isEmpty()) {
      dataSetSchemaVO.getTableSchemas().stream().map(TableSchemaVO::getIdTableSchema).forEach(s -> finalComparisonResult.putIfAbsent(s, true));
      return finalComparisonResult;
    }

    if (dcList.isEmpty()) {
      dataSetSchemaVO.getTableSchemas().stream().map(TableSchemaVO::getIdTableSchema).forEach(s -> finalComparisonResult.putIfAbsent(s, false));
      return finalComparisonResult;
    }

    HashMap<String, S3Object> dcTableMap = new HashMap<>();
    for (S3Object dc : dcList) {
      String dcTableName = getTableNameFromKey(dc.key());
      dcTableMap.putIfAbsent(dcTableName, dc);
      //there is a table without data
      if (!dpList.stream().map(s3Object -> getTableNameFromKey(s3Object.key())).collect(Collectors.toList()).contains(dcTableName)) {
        String tableSchemaId = dataSetSchemaVO.getTableSchemas().stream().filter(tableSchemaVO -> tableSchemaVO.getNameTableSchema().equalsIgnoreCase(dcTableName)).map(TableSchemaVO::getIdTableSchema).findFirst().orElse(null);
        finalComparisonResult.putIfAbsent(tableSchemaId, true);
      }
    }

    for (S3Object dp : dpList) {
      String dpTableName = getTableNameFromKey(dp.key());
      S3Object correspondingDc = dcTableMap.get(dpTableName);

      if (correspondingDc != null) {
        String tableSchemaId = dataSetSchemaVO.getTableSchemas().stream().filter(tableSchemaVO -> tableSchemaVO.getNameTableSchema().equalsIgnoreCase(dpTableName)).map(TableSchemaVO::getIdTableSchema).findFirst().orElse(null);
        if (dp.lastModified().isAfter(correspondingDc.lastModified())) {
          finalComparisonResult.putIfAbsent(tableSchemaId, true);
        } else if (dp.lastModified().isBefore(correspondingDc.lastModified())) {
          finalComparisonResult.putIfAbsent(tableSchemaId, false);
        }
      }
    }
    return finalComparisonResult;
  }

  private DataSetMetabase getDataSetMetabase(Long dpDatasetId) throws EEAException {
    return dataSetMetabaseRepository.findById(dpDatasetId)
        .orElseThrow(() -> new EEAException(EEAErrorMessage.DATASET_NOTFOUND));
  }

  private List<S3Object> getListOfS3Files(Long datasetId, String path, Long dataProviderCode) {
    DataSetMetabaseVO dataset = datasetMetabaseService.findDatasetMetabase(datasetId);
    S3PathResolver s3Path = new S3PathResolver(dataset.getDataflowId(), datasetId, path);
    s3Path.setDataProviderId(dataProviderCode);
    return s3Helper.getFilenamesFromTableNames(s3Path);
  }

  private String getTableNameFromKey(String key) {
    return key.split("/")[4];
  }

  private boolean providerFolderNameExists(String key, String providerFolder) {
    return key.split("/")[5].equalsIgnoreCase(providerFolder);
  }

  private boolean isBigData(Long dataflowId) {
    return dataFlowController.isBigDataflow(dataflowId);
  }

  private List<String> getTableNamesFromSchema(String datasetSchemaId) {
    return datasetSchemaService.getDataSchemaById(datasetSchemaId).getTableSchemas().stream().map(TableSchemaVO::getNameTableSchema).collect(Collectors.toList());
  }

  private boolean checkIfAnyTableHasChanges(Object hashMap) {
      return ((HashMap<String, Boolean>) hashMap).values()
          .stream()
          .anyMatch(Boolean::booleanValue);
  }
}
