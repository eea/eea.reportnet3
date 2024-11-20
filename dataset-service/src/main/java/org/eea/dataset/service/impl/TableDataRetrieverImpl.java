package org.eea.dataset.service.impl;

import lombok.RequiredArgsConstructor;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.S3_PROVIDER_PATH;
import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_ROOT_DC_FOLDER_PATH;

@Service
@RequiredArgsConstructor
public class TableDataRetrieverImpl implements TableDataRetriever {

  private final DatasetMetabaseService datasetMetabaseService;
  private final S3Helper s3Helper;
  private final DataSetMetabaseRepository dataSetMetabaseRepository;
  private final DatasetSchemaService datasetSchemaService;
  private final DataFlowController dataFlowController;
  private final DataCollectionController.DataCollectionControllerZuul dataCollectionControllerZuul;
  private final DatasetService datasetService;

  private static final Logger LOG = LoggerFactory.getLogger(TableDataRetrieverImpl.class);

  @Override
  public ResponseEntity<?> getTablesUpdatedAfterRelease(Long dpDatasetId) {
    String message;
    HttpStatus status;
    try {
      DataSetMetabase datasetMetabase = getDataSetMetabase(dpDatasetId);
      Long dcDatasetId = dataCollectionControllerZuul.findDataCollectionIdByDatasetSchemaId(datasetMetabase.getDatasetSchema());
      Long dataProviderCode = datasetService.getDataProviderIdById(dpDatasetId);

      if (isBigData(datasetMetabase.getDataflowId())) {
        List<S3Object> dcList = getListOfS3Files(dcDatasetId, S3_TABLE_NAME_ROOT_DC_FOLDER_PATH, dataProviderCode);
        List<S3Object> dpList = getListOfS3Files(dpDatasetId, S3_PROVIDER_PATH, dataProviderCode).stream().filter(t -> !t.key().contains("/import")).collect(Collectors.toList());
        String datasetSchemaId = datasetMetabase.getDatasetSchema();
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
    return new ResponseEntity<>(new HashMap<>(), HttpStatus.BAD_REQUEST);
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

  private HashMap<String, Boolean> compareLists(List<S3Object> dcList, List<S3Object> dpList, DataSetSchemaVO dataSetSchemaVO) {
    HashMap<String, S3Object> dcTableMap = new HashMap<>();
    for (S3Object dc : dcList) {
      String dcTableName = getTableName(dc.key());
      dcTableMap.put(dcTableName, dc);
    }

    HashMap<String, Boolean> comparisonResult = new HashMap<>();
    for (S3Object dp : dpList) {
      String dpTableName = getTableName(dp.key());
      S3Object correspondingDc = dcTableMap.get(dpTableName);

      if (correspondingDc != null) {
        String tableSchemaId = dataSetSchemaVO.getTableSchemas().stream().filter(tableSchemaVO -> tableSchemaVO.getNameTableSchema().equalsIgnoreCase(dpTableName)).map(TableSchemaVO::getIdTableSchema).findFirst().orElse(null);
        if (dp.lastModified().isAfter(correspondingDc.lastModified())) {
          comparisonResult.put(tableSchemaId, true);
        } else if (dp.lastModified().isBefore(correspondingDc.lastModified())) {
          comparisonResult.put(tableSchemaId, false);
        }
      }
    }

    return comparisonResult;

  }

  private String getTableName(String key) {
    return key.split("/")[4];
  }

  private boolean isBigData(Long dataflowId) {
    return dataFlowController.isBigDataflow(dataflowId);
  }

}
