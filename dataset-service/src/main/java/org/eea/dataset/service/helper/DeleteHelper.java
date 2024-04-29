package org.eea.dataset.service.helper;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class DeleteHelper {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DeleteHelper.class);

  /**
   * The connection url.
   */
  @Value("${spring.datasource.url}")
  private String connectionUrl;

  /**
   * The connection username.
   */
  @Value("${spring.datasource.dataset.username}")
  private String connectionUsername;

  /**
   * The connection password.
   */
  @Value("${spring.datasource.dataset.password}")
  private String connectionPassword;

  /**
   * The connection driver.
   */
  @Value("${spring.datasource.driverClassName}")
  private String connectionDriver;

  /** The kafka sender helper. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The job controller zuul */
  @Autowired
  private JobController.JobControllerZuul jobControllerZuul;


  /**
   * Instantiates a new file loader helper.
   */
  public DeleteHelper() {
    super();
  }

  /**
   * Execute delete table process.
   *
   * @param datasetId     the dataset id
   * @param tableSchemaId the table schema id
   * @param jobId         the job ID of the delete process
   */
  @Async
  public void executeDeleteTableProcess(final Long datasetId, String tableSchemaId, Long jobId) {
    try {
      LOG.info("Deleting table {} from dataset {}", tableSchemaId, datasetId);
      datasetService.deleteTableBySchema(tableSchemaId, datasetId, false);
      // now the view is not updated, update the check to false
      datasetService.updateCheckView(datasetId, false);
      // delete the temporary table from etlExport
      datasetService.deleteTempEtlExport(datasetId);
      EventType eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
          ? EventType.DELETE_TABLE_COMPLETED_EVENT
          : EventType.DELETE_TABLE_SCHEMA_COMPLETED_EVENT;

      // after the table has been deleted, an event is sent to notify it
      Map<String, Object> value = new HashMap<>();
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
          .tableSchemaId(tableSchemaId).build();
      DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
      notificationVO.setDatasetName(datasetMetabaseVO.getDataSetName());
      notificationVO.setDataflowId(datasetMetabaseVO.getDataflowId());
      notificationVO.setDataflowName(
          dataflowControllerZuul.getMetabaseById(datasetMetabaseVO.getDataflowId()).getName());

      value.put(LiteralConstants.DATASET_ID, datasetId);

      try {
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
      } catch (EEAException e) {
        LOG_ERROR.error("Error releasing notification for datasetId {} and tableSchemaId {} Message: {}", datasetId, tableSchemaId, e.getMessage(), e);
      }

      if (jobId != null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
      }
      LOG.info("Successfully deleted table data for datasetId {} and tableSchemaId {}", datasetId, tableSchemaId);
    } catch (Exception e) {
      if (jobId != null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
      }

      throw e;
    } finally {
      // Release the lock manually
      Map<String, Object> deleteImportTable = new HashMap<>();
      deleteImportTable.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_IMPORT_TABLE.getValue());
      deleteImportTable.put(LiteralConstants.DATASETID, datasetId);
      deleteImportTable.put(LiteralConstants.TABLESCHEMAID, tableSchemaId);
      lockService.removeLockByCriteria(deleteImportTable);
    }
  }

  /**
   * Execute delete dataset process.
   *
   * @param datasetId             the dataset id
   * @param deletePrefilledTables the delete prefilled tables
   * @param technicallyAccepted   the technically accepted
   * @param jobId                 the job ID
   */
  @Async
  public void executeDeleteDatasetProcess(final Long datasetId, Boolean deletePrefilledTables,
                                          boolean technicallyAccepted, Long jobId) {
    try {
      LOG.info("Deleting data from dataset {}", datasetId);
      datasetService.deleteImportData(datasetId, deletePrefilledTables);
      // now the view is not updated, update the check to false
      datasetService.updateCheckView(datasetId, false);
      // delete the temporary table from etlExport
      datasetService.deleteTempEtlExport(datasetId);
      EventType eventType = DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))
          ? EventType.DELETE_DATASET_DATA_COMPLETED_EVENT
          : EventType.DELETE_DATASET_SCHEMA_COMPLETED_EVENT;

      // If technically accepted is false, it will be notified and the dataset validated
      if (!technicallyAccepted) {
        // after the dataset values have been deleted, an event is sent to notify it
        Map<String, Object> value = new HashMap<>();
        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .datasetId(datasetId).build();
        DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
        notificationVO.setDatasetName(datasetMetabaseVO.getDataSetName());
        notificationVO.setDataflowId(datasetMetabaseVO.getDataflowId());
        notificationVO.setDataflowName(
            dataflowControllerZuul.getMetabaseById(datasetMetabaseVO.getDataflowId()).getName());

        value.put(LiteralConstants.DATASET_ID, datasetId);

        try {
          kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value, notificationVO);
        } catch (EEAException e) {
          LOG_ERROR.error("Error releasing notification for datasetId {} Message: {}", datasetId, e.getMessage());
        }
      }

      if (jobId != null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
      }
      LOG.info("Successfully deleted dataset data for datasetId {}", datasetId);
    } catch (Exception e) {
      if (jobId != null) {
        jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FAILED);
      }

      throw e;
    } finally {
      // Release the lock manually
      Map<String, Object> deleteDatasetValues = new HashMap<>();
      deleteDatasetValues.put(LiteralConstants.SIGNATURE,
              LockSignature.DELETE_DATASET_VALUES.getValue());
      deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(deleteDatasetValues);
    }
  }


  /**
   * Execute delete import data async before replacing.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   */
  @Async
  public void executeDeleteImportDataAsyncBeforeReplacing(Long datasetId, Long integrationId,
      IntegrationOperationTypeEnum operation) {
    datasetService.deleteImportData(datasetId, false);
    LOG.info("All data value deleted from datasetId {}. Next step call the FME by the kafka event",
        datasetId);
    // now the view is not updated, update the check to false
    datasetService.updateCheckView(datasetId, false);
    // delete the temporary table from etlExport
    datasetService.deleteTempEtlExport(datasetId);
    // Send the kafka event after deleting to call FME
    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, datasetId);
    value.put(LiteralConstants.INTEGRATION_ID, integrationId);
    value.put(LiteralConstants.OPERATION, operation);
    kafkaSenderUtils.releaseKafkaEvent(EventType.DATA_DELETE_TO_REPLACE_COMPLETED_EVENT, value);
    LOG.info("Successfully deleted data before replacing for datasetId {} and integrationId {}", datasetId, integrationId);
  }


  /**
   * Delete record values by provider.
   *
   * @param datasetId the dataset id
   * @param providerCode the provider code
   */
  @Transactional
  public void deleteRecordValuesByProvider(Long datasetId, String providerCode, String processId) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    RecordValue recordValue = recordRepository.findFirstByDataProviderCode(providerCode);
    if (recordValue!=null) {
      LOG.info("Deleting data with providerCode: {} for release processId {}", providerCode, processId);
      try {
        LOG.info("Release process: Executing delete operation for datasetId {}, providerCode {}", datasetId, providerCode);
        Long totalCountOfRecords = recordRepository.countRecordValueByDataProviderCode(providerCode);
        String datasetName = "dataset_" + datasetId;
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connectionDriver);
        dataSource.setUrl(connectionUrl);
        dataSource.setUsername(connectionUsername);
        dataSource.setPassword(connectionPassword);
        while (totalCountOfRecords>0) {
          LOG.info("Release process: executing delete for 100000 records out of {} for datasetId {}, providerCode {}", totalCountOfRecords, datasetId, providerCode);
          JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
          StringBuilder deleteSql = new StringBuilder("WITH rows AS (SELECT id FROM ");
          deleteSql.append(datasetName).append(".record_value where data_provider_code = ? LIMIT 100000) ");
          deleteSql.append("DELETE FROM ");
          deleteSql.append(datasetName).append(".record_value rv ");
          deleteSql.append("USING rows WHERE rv.id = rows.id;");
          jdbcTemplate.update(deleteSql.toString(), providerCode);
          LOG.info("Release process: deleted 100000 records for datasetId {}, providerCode {}, counting again", datasetId, providerCode);
          totalCountOfRecords = recordRepository.countRecordValueByDataProviderCode(providerCode);
          LOG.info("Release process: executing delete for datasetId {}, providerCode {}, records remaining {}", datasetId, providerCode, totalCountOfRecords);
        }
        LOG.info("Release process: Executed delete operation for datasetId {}, providerCode {}", datasetId, providerCode);
      } catch (Exception er) {
        LOG.error("Release process: error executing delete operation for datasetId {}, providerCode {}, {}", datasetId, providerCode, er.getMessage());
        throw er;
      }
    }
    // now the view is not updated, update the check to false
    datasetService.updateCheckView(datasetId, false);
    // delete the temporary table from etlExport
    LOG.info("Deleting table tempEtlExport for dataset ", datasetId);
    datasetService.deleteTempEtlExport(datasetId);
  }

}
