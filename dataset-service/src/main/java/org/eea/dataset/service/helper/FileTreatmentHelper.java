package org.eea.dataset.service.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class FileTreatmentHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileTreatmentHelper.class);

  /**
   * The kafka sender helper.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataset schema service. */
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /**
   * Instantiates a new file loader helper.
   */
  public FileTreatmentHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the input stream
   * @param tableSchemaId the id table schema
   */
  @Async
  public void executeFileProcess(final Long datasetId, final String fileName, final InputStream is,
      String tableSchemaId) {


    // Integration process
    String fileExtension = null;
    String datasetSchemaId = null;
    try {
      fileExtension = datasetService.getMimetype(fileName);
      datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);

    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    IntegrationVO integrationVO = new IntegrationVO();
    Map<String, String> internalParameters = new HashMap<>();
    internalParameters.put("datasetSchemaId", datasetSchemaId);
    integrationVO.setInternalParameters(internalParameters);
    List<IntegrationVO> integrationAux = new ArrayList<>();
    List<IntegrationVO> integrations =
        integrationController.findAllIntegrationsByCriteria(integrationVO);
    List<String> auxExtensionList = new ArrayList<>();
    integrations.stream().forEach(integration -> {
      if (IntegrationOperationTypeEnum.IMPORT.equals(integration.getOperation())) {
        auxExtensionList.add(integration.getInternalParameters().get("fileExtension"));
        Map<String, String> externalParameters = new HashMap<>();
        byte[] imageBytes;
        try {
          imageBytes = IOUtils.toByteArray(is);
          String encodedString = Base64.getEncoder().encodeToString(imageBytes);
          is.close();
          externalParameters.put("fileIS", encodedString);
          integration.setExternalParameters(externalParameters);
        } catch (IOException e) {
          e.printStackTrace();
        }
        integrationAux.add(integration);
      }
    });
    if (auxExtensionList.contains(fileExtension)) {
      try {
        integrationController.executeIntegrationProcess(IntegrationToolTypeEnum.FME,
            IntegrationOperationTypeEnum.IMPORT, fileName, datasetId, integrationAux.get(0));
      } finally {
        removeLock(datasetId, tableSchemaId);
      }
    } else {
      // REP-3 file process
      try {
        LOG.info("Processing file");
        DataSetVO datasetVO = datasetService.processFile(datasetId, fileName, is, tableSchemaId);

        // map the VO to the entity
        datasetVO.setId(datasetId);
        final DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
        if (dataset == null) {
          throw new IOException("Error mapping file");
        }

        // Save empty table
        List<RecordValue> allRecords = dataset.getTableValues().get(0).getRecords();
        dataset.getTableValues().get(0).setRecords(new ArrayList<>());

        // Check if the table with idTableSchema has been populated already
        Long oldTableId = datasetService.findTableIdByTableSchema(datasetId, tableSchemaId);
        fillTableId(tableSchemaId, dataset.getTableValues(), oldTableId);

        if (null == oldTableId) {
          datasetService.saveTable(datasetId, dataset.getTableValues().get(0));
        }

        List<List<RecordValue>> listaGeneral = getListOfRecords(allRecords);

        // Obtain the data provider code to insert into the record
        Long providerId = 0L;
        DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(datasetId);
        if (metabase.getDataProviderId() != null) {
          providerId = metabase.getDataProviderId();
        }
        DataProviderVO provider = representativeControllerZuul.findDataProviderById(providerId);

        listaGeneral.parallelStream().forEach(value -> {
          value.stream().forEach(r -> r.setDataProviderCode(provider.getCode()));
          datasetService.saveAllRecords(datasetId, value);
        });

        LOG.info("File processed and saved into DB");
        releaseSuccessEvents((String) ThreadPropertiesManager.getVariable("user"), datasetId,
            tableSchemaId, fileName);
      } catch (Exception e) {
        LOG.error("Error loading file: " + fileName, e);
        releaseFailEvents((String) ThreadPropertiesManager.getVariable("user"), datasetId,
            tableSchemaId, fileName, "Fail importing file " + fileName);
      } finally {
        removeLock(datasetId, tableSchemaId);
      }
    }
  }

  /**
   * Release success events.
   *
   * @param user the user
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param fileName the file name
   */
  private void releaseSuccessEvents(String user, Long datasetId, String tableSchemaId,
      String fileName) {
    EventType eventType =
        datasetService.isReportingDataset(datasetId) ? EventType.LOAD_DATA_COMPLETED_EVENT
            : EventType.LOAD_SCHEMA_COMPLETED_EVENT;
    try {
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, NotificationVO.builder()
          .user(user).datasetId(datasetId).tableSchemaId(tableSchemaId).fileName(fileName).build());
    } catch (EEAException e) {
      LOG.error("Error realeasing event " + eventType, e);
    }
  }

  /**
   * Release fail events.
   *
   * @param user the user
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param fileName the file name
   * @param error the error
   */
  private void releaseFailEvents(String user, Long datasetId, String tableSchemaId, String fileName,
      String error) {
    EventType eventType =
        datasetService.isReportingDataset(datasetId) ? EventType.LOAD_DATA_FAILED_EVENT
            : EventType.LOAD_SCHEMA_FAILED_EVENT;
    try {
      Map<String, Object> value = new HashMap<>();
      value.put(LiteralConstants.DATASET_ID, datasetId);
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value,
          NotificationVO.builder().user(user).datasetId(datasetId).tableSchemaId(tableSchemaId)
              .fileName(fileName).error(error).build());
    } catch (EEAException e) {
      LOG.error("Error realeasing event " + eventType, e);
    }
  }

  /**
   * Removes the lock.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  private void removeLock(Long datasetId, String tableSchemaId) {
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.LOAD_TABLE.getValue());
    criteria.add(datasetId);
    criteria.add(tableSchemaId);
    lockService.removeLockByCriteria(criteria);
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }

  /**
   * Gets the list of records.
   *
   * @param allRecords the all records
   *
   * @return the list of records
   */
  private List<List<RecordValue>> getListOfRecords(List<RecordValue> allRecords) {
    List<List<RecordValue>> generalList = new ArrayList<>();
    // lists size
    final int BATCH = 1000;

    // dividing the number of records in different lists
    int nLists = (int) Math.ceil(allRecords.size() / (double) BATCH);
    if (nLists > 1) {
      for (int i = 0; i < (nLists - 1); i++) {
        generalList.add(new ArrayList<>(allRecords.subList(BATCH * i, BATCH * (i + 1))));
      }
    }
    generalList.add(new ArrayList<>(allRecords.subList(BATCH * (nLists - 1), allRecords.size())));

    return generalList;
  }
}
