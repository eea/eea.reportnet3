package org.eea.dataset.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.eea.dataset.mapper.DataCollectionMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.model.FKDataCollection;
import org.eea.dataset.service.model.IntegrityDataCollection;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The Class DataCollectionServiceImpl.
 */
@Service("dataCollectionService")
public class DataCollectionServiceImpl implements DataCollectionService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant CHUNK_SIZE. */
  private static final int CHUNK_SIZE = 10;

  /** The Constant NAME_DC: {@value}. */
  private static final String NAME_DC = "Data Collection - %s";

  /** The Constant NAME_EU: {@value}. */
  private static final String NAME_EU = "EU Dataset - %s";

  /** The Constant UPDATE_DATAFLOW_STATUS: {@value}. */
  private static final String UPDATE_DATAFLOW_STATUS =
      "update dataflow set status = '%s', manual_acceptance = '%s', deadline_date = '%s' where id = %d";

  /** The Constant UPDATE_REPRESENTATIVE_HAS_DATASETS: {@value}. */
  private static final String UPDATE_REPRESENTATIVE_HAS_DATASETS =
      "update representative set has_datasets = %b where id = %d;";

  /** The Constant INSERT_DC_INTO_DATASET: {@value}. */
  private static final String INSERT_DC_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema) values ('%s', %d, '%s', '%s') returning id";

  /** The Constant INSERT_EU_INTO_DATASET: {@value}. */
  private static final String INSERT_EU_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema) values ('%s', %d, '%s', '%s') returning id";

  /** The Constant INSERT_DC_INTO_DATA_COLLECTION: {@value}. */
  private static final String INSERT_DC_INTO_DATA_COLLECTION =
      "insert into data_collection (id, due_date) values (%d, '%s')";

  /** The Constant INSERT_EU_INTO_EU_DATASET: {@value}. */
  private static final String INSERT_EU_INTO_EU_DATASET = "insert into eu_dataset (id) values (%d)";

  /** The Constant INSERT_RD_INTO_DATASET: {@value}. */
  private static final String INSERT_RD_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema, data_provider_id, status) values ('%s', %d, '%s', '%s', %d, 'PENDING') returning id";

  /** The Constant INSERT_RD_INTO_REPORTING_DATASET: {@value}. */
  private static final String INSERT_RD_INTO_REPORTING_DATASET =
      "insert into reporting_dataset (id) values (%d)";

  /** The Constant INSERT_INTO_PARTITION_DATASET: {@value}. */
  private static final String INSERT_INTO_PARTITION_DATASET =
      "insert into partition_dataset (user_name, id_dataset) values ('root', %d)";

  /** The metabase data source. */
  @Autowired
  @Qualifier("metabaseDatasource")
  private DataSource metabaseDataSource;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The data collection mapper. */
  @Autowired
  private DataCollectionMapper dataCollectionMapper;

  /** The design dataset service. */
  @Autowired
  @Lazy
  private DesignDatasetService designDatasetService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The resource management controller zuul. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /** The user management controller zuul. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset schema service. */
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /** The foreign relations repository. */
  @Autowired
  private ForeignRelationsRepository foreignRelationsRepository;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The eu dataset repository. */
  @Autowired
  private EUDatasetRepository euDatasetRepository;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * Gets the dataflow status.
   *
   * @param dataflowId the dataflow id
   * @return the dataflow status
   */
  @Override
  public TypeStatusEnum getDataflowStatus(Long dataflowId) {
    try {
      DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(dataflowId);
      return (dataflowVO != null) ? dataflowVO.getStatus() : null;
    } catch (Exception e) {
      LOG_ERROR.error("Error in isDesignDataflow", e);
      return null;
    }
  }

  /**
   * Gets the data collection id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data collection id by dataflow id
   */
  @Override
  public List<DataCollectionVO> getDataCollectionIdByDataflowId(Long idFlow) {
    List<DataCollection> datacollections = dataCollectionRepository.findByDataflowId(idFlow);
    return dataCollectionMapper.entityListToClass(datacollections);
  }

  /**
   * Undo data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   */
  @Override
  public void undoDataCollectionCreation(List<Long> datasetIds, Long dataflowId,
      boolean isCreation) {

    releaseLockAndNotification(dataflowId, "Error creating schemas", isCreation);

    int size = datasetIds.size();
    for (int i = 0; i < size; i += CHUNK_SIZE) {
      resourceManagementControllerZuul
          .deleteResourceByDatasetId(datasetIds.subList(i, i + 10 > size ? size : i + 10));
    }
    dataCollectionRepository.deleteDatasetById(datasetIds);
    dataflowControllerZuul.updateDataFlowStatus(dataflowId, TypeStatusEnum.DESIGN, null);
  }

  /**
   * Release lock and notification.
   *
   * @param dataflowId the dataflow id
   * @param errorMessage the error message
   * @param isCreation the is creation
   */
  private void releaseLockAndNotification(Long dataflowId, String errorMessage,
      boolean isCreation) {
    String methodSignature = isCreation ? LockSignature.CREATE_DATA_COLLECTION.getValue()
        : LockSignature.UPDATE_DATA_COLLECTION.getValue();
    EventType failEvent = isCreation ? EventType.ADD_DATACOLLECTION_FAILED_EVENT
        : EventType.UPDATE_DATACOLLECTION_FAILED_EVENT;

    // Release the lock
    Map<String, Object> lockCriteria = new HashMap<>();
    lockCriteria.put(LiteralConstants.SIGNATURE, methodSignature);
    lockCriteria.put(LiteralConstants.DATAFLOWID, dataflowId);
    lockService.removeLockByCriteria(lockCriteria);

    // Release the notification
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(failEvent, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataflowId).error(errorMessage).build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing {} event: ", failEvent, e);
    }
  }

  /**
   * Update data collection.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @Async
  public void updateDataCollection(Long dataflowId) {
    manageDataCollection(dataflowId, null, false, false, false);
  }

  /**
   * Creates the empty data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck the manual check
   */
  @Override
  @Async
  public void createEmptyDataCollection(Long dataflowId, Date dueDate,
      boolean stopAndNotifySQLErrors, boolean manualCheck, boolean showPublicInfo) {

    manageDataCollection(dataflowId, dueDate, true, stopAndNotifySQLErrors, manualCheck);

    updateReportingDatasetsVisibility(dataflowId, showPublicInfo);

  }

  /**
   * Update reporting datasets visibility.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  private void updateReportingDatasetsVisibility(Long dataflowId, boolean showPublicInfo) {

    dataflowControllerZuul.updateDataFlowPublicStatus(dataflowId, showPublicInfo);

  }


  /**
   * Manage data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param isCreation the is creation
   * @param stopAndNotifySQLErrors the stop and notify SQL errors
   * @param manualCheck the manual check
   */
  private void manageDataCollection(Long dataflowId, Date dueDate, boolean isCreation,
      boolean stopAndNotifySQLErrors, boolean manualCheck) {
    String time = Timestamp.valueOf(LocalDateTime.now()).toString();

    boolean rulesOk = true;

    // 1. Get the design datasets
    List<DesignDatasetVO> designs = designDatasetService.getDesignDataSetIdByDataflowId(dataflowId);

    // we look if all SQL QC's are working correctly, if not we disable it before do a dc
    if (isCreation) {
      LOG.info("Validate SQL Rules in Dataflow {}, Data Collection creation proccess.", dataflowId);
      List<Boolean> rulesWithError = new ArrayList<>();
      designs.stream().forEach(dataset -> {
        recordStoreControllerZuul.createUpdateQueryView(dataset.getId(), false);
        List<RuleVO> rulesSql =
            rulesControllerZuul.findSqlSentencesByDatasetSchemaId(dataset.getDatasetSchema());
        if (null != rulesSql && !rulesSql.isEmpty()) {
          rulesSql.stream().forEach(ruleVO -> rulesWithError.add(rulesControllerZuul
              .validateSqlRuleDataCollection(dataset.getId(), dataset.getDatasetSchema(), ruleVO)));
        }
      });
      LOG.info(
          "Data Collection creation proccess stopped: there are SQL rules containing: {} errors",
          rulesWithError.size());
      if (stopAndNotifySQLErrors) {
        rulesOk = checkSQLRulesErrors(dataflowId, rulesOk, designs, rulesWithError);
      }
    }
    if (rulesOk) {
      // 2. Get the representatives who are going to provide data
      List<RepresentativeVO> representatives = representativeControllerZuul
          .findRepresentativesByIdDataFlow(dataflowId).stream()
          .filter(representative -> !representative.getHasDatasets()).collect(Collectors.toList());

      if (representatives.isEmpty()) {
        releaseLockAndNotification(dataflowId, "No representatives without datasets", isCreation);
        return;
      }

      // 3. Get the providers associated with representatives
      List<DataProviderVO> dataProviders =
          representativeControllerZuul.findDataProvidersByIds(representatives.stream()
              .map(RepresentativeVO::getDataProviderId).collect(Collectors.toList()));

      // 4. Map representatives to providers
      Map<Long, String> map = mapRepresentativesToProviders(representatives, dataProviders);

      List<Long> dataCollectionIds = new ArrayList<>();
      Map<Long, List<String>> datasetIdsEmails = new HashMap<>();
      Map<Long, String> datasetIdsAndSchemaIds = new HashMap<>();
      Map<Long, String> datasetIdsAndSchemaIdsFromDC = new HashMap<>();
      Map<Long, String> datasetIdsAndSchemaIdsFromEU = new HashMap<>();
      List<Long> euDatasetIds = new ArrayList<>();

      try (Connection connection = metabaseDataSource.getConnection();
          Statement statement = connection.createStatement()) {

        processDataCollectionAndRoles(dataflowId, dueDate, isCreation, manualCheck, time, designs,
            representatives, map, dataCollectionIds, datasetIdsEmails, datasetIdsAndSchemaIds,
            datasetIdsAndSchemaIdsFromDC, datasetIdsAndSchemaIdsFromEU, euDatasetIds, connection,
            statement);
      } catch (SQLException e) {
        LOG_ERROR.error("Error rolling back: ", e);
      }
    }
  }

  /**
   * Check SQL rules errors.
   *
   * @param dataflowId the dataflow id
   * @param rulesOk the rules ok
   * @param designs the designs
   * @param rulesWithError the rules with error
   * @return true, if successful
   */
  private boolean checkSQLRulesErrors(Long dataflowId, boolean rulesOk,
      List<DesignDatasetVO> designs, List<Boolean> rulesWithError) {
    long errorsCount = rulesWithError.stream().filter(ruleStatus -> Boolean.FALSE).count();
    int disabledRules = rulesControllerZuul.getAllDisabledRules(dataflowId, designs);
    if (errorsCount > 0 || disabledRules > 0) {
      NotificationVO notificationVO = NotificationVO.builder()
          .user((String) ThreadPropertiesManager.getVariable("user")).dataflowId(dataflowId)
          .invalidRules(rulesControllerZuul.getAllUncheckedRules(dataflowId, designs))
          .disabledRules(disabledRules).build();
      LOG.info("Data Collection creation proccess stopped: there are SQL rules containing errors");
      // remove lock
      Map<String, Object> createDataCollection = new HashMap<>();
      createDataCollection.put(LiteralConstants.SIGNATURE,
          LockSignature.CREATE_DATA_COLLECTION.getValue());
      createDataCollection.put(LiteralConstants.DATAFLOWID, dataflowId);
      lockService.removeLockByCriteria(createDataCollection);
      // release notification
      rulesOk = false;
      releaseNotification(EventType.DISABLE_RULES_ERROR_EVENT, notificationVO);
    }
    return rulesOk;
  }

  /**
   * Process data collection and roles.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param isCreation the is creation
   * @param manualCheck the manual check
   * @param time the time
   * @param designs the designs
   * @param representatives the representatives
   * @param map the map
   * @param dataCollectionIds the data collection ids
   * @param datasetIdsEmails the dataset ids emails
   * @param datasetIdsAndSchemaIds the dataset ids and schema ids
   * @param datasetIdsAndSchemaIdsFromDC the dataset ids and schema ids from DC
   * @param datasetIdsAndSchemaIdsFromEU the dataset ids and schema ids from EU
   * @param euDatasetIds the eu dataset ids
   * @param connection the connection
   * @param statement the statement
   * @throws SQLException the SQL exception
   */
  private void processDataCollectionAndRoles(Long dataflowId, Date dueDate, boolean isCreation,
      boolean manualCheck, String time, List<DesignDatasetVO> designs,
      List<RepresentativeVO> representatives, Map<Long, String> map, List<Long> dataCollectionIds,
      Map<Long, List<String>> datasetIdsEmails, Map<Long, String> datasetIdsAndSchemaIds,
      Map<Long, String> datasetIdsAndSchemaIdsFromDC,
      Map<Long, String> datasetIdsAndSchemaIdsFromEU, List<Long> euDatasetIds,
      Connection connection, Statement statement) throws SQLException {
    try {
      connection.setAutoCommit(false);

      if (isCreation) {
        // 5. Set dataflow to DRAFT
        statement.addBatch(String.format(UPDATE_DATAFLOW_STATUS, TypeStatusEnum.DRAFT, manualCheck,
            dueDate, dataflowId));
      }

      for (RepresentativeVO representative : representatives) {
        statement.addBatch(
            String.format(UPDATE_REPRESENTATIVE_HAS_DATASETS, true, representative.getId()));
      }

      List<FKDataCollection> newReportingDatasetsRegistry = new ArrayList<>();
      List<FKDataCollection> newDCsRegistry = new ArrayList<>();
      List<FKDataCollection> newEUsRegistry = new ArrayList<>();
      List<IntegrityDataCollection> lIntegrityDataCollections = new ArrayList<>();
      for (DesignDatasetVO design : designs) {
        RulesSchemaVO rulesSchemaVO =
            rulesControllerZuul.findRuleSchemaByDatasetId(design.getDatasetSchema());
        List<IntegrityVO> integritieVOs = findIntegrityVO(rulesSchemaVO);
        if (isCreation) {
          // 6. Create DataCollection in metabase
          Long dataCollectionId = persistDC(statement, design, time, dataflowId, dueDate);
          dataCollectionIds.add(dataCollectionId);
          datasetIdsAndSchemaIds.put(dataCollectionId, design.getDatasetSchema());
          datasetIdsAndSchemaIdsFromDC.put(dataCollectionId, design.getDatasetSchema());

          // 6b. Create the EU Dataset
          Long euDatasetId = persistEU(statement, design, time, dataflowId);
          euDatasetIds.add(euDatasetId);
          datasetIdsAndSchemaIds.put(euDatasetId, design.getDatasetSchema());
          datasetIdsAndSchemaIdsFromEU.put(euDatasetId, design.getDatasetSchema());

          prepareFKAndIntegrityForEUandDC(dataCollectionId, newDCsRegistry,
              lIntegrityDataCollections, design, integritieVOs);
          prepareFKAndIntegrityForEUandDC(euDatasetId, newEUsRegistry, lIntegrityDataCollections,
              design, integritieVOs);
        }

        // 7. Create Reporting Dataset in metabase
        createReportingDatasetInMetabase(dataflowId, time, representatives, map, datasetIdsEmails,
            datasetIdsAndSchemaIds, statement, newReportingDatasetsRegistry,
            lIntegrityDataCollections, design, integritieVOs);
      }

      statement.executeBatch();
      // 8. Create permissions
      createPermissions(datasetIdsEmails, dataCollectionIds, euDatasetIds, dataflowId);
      // 9. Delete editors
      removePermissionEditors(dataflowId);

      connection.commit();
      // Add into the foreign_relations table from metabase the dataset origin-destination
      // relation, if applies
      addForeignRelationsFromNewReportings(newReportingDatasetsRegistry);
      addForeignRelationsFromNewDCandEUs(newDCsRegistry);
      addForeignRelationsFromNewDCandEUs(newEUsRegistry);
      if (lIntegrityDataCollections != null) {
        addDatasetForeignRelations(lIntegrityDataCollections);
      }
      LOG.info("Metabase changes completed on DataCollection creation");

      // 10. Create schemas for each dataset
      // This method will release the lock
      recordStoreControllerZuul.createSchemas(datasetIdsAndSchemaIds, dataflowId, isCreation, true);
    } catch (SQLException e) {
      LOG_ERROR.error("Error persisting changes. Rolling back...", e);
      releaseLockAndRollback(connection, dataflowId, isCreation);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating permissions. Rolling back...", e);
      releaseLockAndRollback(connection, dataflowId, isCreation);
    } finally {
      connection.setAutoCommit(true);
    }
  }

  /**
   * Map representatives to providers.
   *
   * @param representatives the representatives
   * @param dataProviders the data providers
   * @return the map
   */
  private Map<Long, String> mapRepresentativesToProviders(List<RepresentativeVO> representatives,
      List<DataProviderVO> dataProviders) {
    Map<Long, String> map = new HashMap<>();
    for (RepresentativeVO representative : representatives) {
      for (DataProviderVO dataProvider : dataProviders) {
        if (dataProvider.getId().equals(representative.getDataProviderId())) {
          map.put(representative.getDataProviderId(), dataProvider.getLabel());
          dataProviders.remove(dataProvider);
          break;
        }
      }
    }
    return map;
  }

  /**
   * Creates the reporting dataset in metabase.
   *
   * @param dataflowId the dataflow id
   * @param time the time
   * @param representatives the representatives
   * @param map the map
   * @param datasetIdsEmails the dataset ids emails
   * @param datasetIdsAndSchemaIds the dataset ids and schema ids
   * @param statement the statement
   * @param newReportingDatasetsRegistry the new reporting datasets registry
   * @param lIntegrityDataCollections the l integrity data collections
   * @param design the design
   * @param integritieVOs the integritie V os
   * @throws SQLException the SQL exception
   */
  private void createReportingDatasetInMetabase(Long dataflowId, String time,
      List<RepresentativeVO> representatives, Map<Long, String> map,
      Map<Long, List<String>> datasetIdsEmails, Map<Long, String> datasetIdsAndSchemaIds,
      Statement statement, List<FKDataCollection> newReportingDatasetsRegistry,
      List<IntegrityDataCollection> lIntegrityDataCollections, DesignDatasetVO design,
      List<IntegrityVO> integritieVOs) throws SQLException {
    for (RepresentativeVO representative : representatives) {
      // Here we save the reporting datasets.
      Long datasetId = persistRD(statement, design, representative, time, dataflowId,
          map.get(representative.getDataProviderId()));
      List<String> emails = representative.getLeadReporters().stream().map(LeadReporterVO::getEmail)
          .collect(Collectors.toList());
      if (emails.isEmpty()) {
        datasetIdsEmails.put(datasetId, null);
      } else {
        datasetIdsEmails.put(datasetId, emails);
      }
      datasetIdsAndSchemaIds.put(datasetId, design.getDatasetSchema());

      FKDataCollection newReporting = new FKDataCollection();
      newReporting.setRepresentative(map.get(representative.getDataProviderId()));
      newReporting.setIdDatasetSchemaOrigin(design.getDatasetSchema());
      newReporting.setIdDatasetOrigin(datasetId);
      newReporting
          .setFks(datasetSchemaService.getReferencedFieldsBySchema(design.getDatasetSchema()));
      newReportingDatasetsRegistry.add(newReporting);
      if (!integritieVOs.isEmpty()) {
        for (IntegrityVO integritieVO : integritieVOs) {
          IntegrityDataCollection integrityDataCollection = new IntegrityDataCollection();
          integrityDataCollection.setIdDatasetOrigin(datasetId);
          integrityDataCollection.setIdDatasetSchemaOrigin(design.getDatasetSchema());
          integrityDataCollection.setDataProviderId(representative.getDataProviderId());
          integrityDataCollection
              .setIdDatasetSchemaReferenced(integritieVO.getReferencedDatasetSchemaId());
          lIntegrityDataCollections.add(integrityDataCollection);
        }
      }
    }
  }

  /**
   * Prepare FK and integrity for E uand DC.
   *
   * @param datasetIdFromDCorEU the dataset id from D cor EU
   * @param newDCandEUsRegistry the new D cand E us registry
   * @param lIntegrityDataCollections the l integrity data collections
   * @param design the design
   * @param integritieVOs the integritie V os
   */
  private void prepareFKAndIntegrityForEUandDC(Long datasetIdFromDCorEU,
      List<FKDataCollection> newDCandEUsRegistry,
      List<IntegrityDataCollection> lIntegrityDataCollections, DesignDatasetVO design,
      List<IntegrityVO> integritieVOs) {

    FKDataCollection newReporting = new FKDataCollection();
    newReporting.setIdDatasetSchemaOrigin(design.getDatasetSchema());
    newReporting.setIdDatasetOrigin(datasetIdFromDCorEU);
    newReporting
        .setFks(datasetSchemaService.getReferencedFieldsBySchema(design.getDatasetSchema()));
    newDCandEUsRegistry.add(newReporting);
    if (!integritieVOs.isEmpty()) {
      for (IntegrityVO integritieVO : integritieVOs) {
        IntegrityDataCollection integrityDataCollection = new IntegrityDataCollection();
        integrityDataCollection.setIdDatasetOrigin(datasetIdFromDCorEU);
        integrityDataCollection.setIdDatasetSchemaOrigin(design.getDatasetSchema());
        integrityDataCollection
            .setIdDatasetSchemaReferenced(integritieVO.getReferencedDatasetSchemaId());
        lIntegrityDataCollections.add(integrityDataCollection);
      }
    }
  }

  /**
   * Removes the permission editors.
   *
   * @param dataflowId the dataflow id
   */
  private void removePermissionEditors(Long dataflowId) {

    List<DesignDataset> designDatasetIds = designDatasetRepository.findByDataflowId(dataflowId);
    List<ResourceInfoVO> resources = new ArrayList<>();
    resources.add(resourceManagementControllerZuul.getResourceDetail(dataflowId,
        ResourceGroupEnum.DATAFLOW_EDITOR_WRITE));
    resources.add(resourceManagementControllerZuul.getResourceDetail(dataflowId,
        ResourceGroupEnum.DATAFLOW_EDITOR_READ));
    for (DesignDataset designDataset : designDatasetIds) {
      resources.add(resourceManagementControllerZuul.getResourceDetail(designDataset.getId(),
          ResourceGroupEnum.DATASCHEMA_EDITOR_READ));
      resources.add(resourceManagementControllerZuul.getResourceDetail(designDataset.getId(),
          ResourceGroupEnum.DATASCHEMA_EDITOR_WRITE));
    }
    resourceManagementControllerZuul.deleteResource(
        resources.parallelStream().filter(resourceinfoVO -> resourceinfoVO.getResourceId() != null)
            .collect(Collectors.toList()));
  }

  /**
   * Find integrity VO.
   *
   * @param rulesSchemaVO the rules schema VO
   * @return the list
   */
  List<IntegrityVO> findIntegrityVO(RulesSchemaVO rulesSchemaVO) {
    List<IntegrityVO> integritiesVO = new ArrayList<>();
    if (rulesSchemaVO != null && rulesSchemaVO.getRules() != null) {
      integritiesVO = rulesSchemaVO.getRules().stream().filter(
          rule -> EntityTypeEnum.DATASET.equals(rule.getType()) && rule.getIntegrityVO() != null)
          .map(RuleVO::getIntegrityVO).collect(Collectors.toList());
    }
    return integritiesVO;
  }

  /**
   * Adds the dataset foreign relations.
   *
   * @param lIntegrityDataCollections the l integrity data collections
   */
  private void addDatasetForeignRelations(List<IntegrityDataCollection> lIntegrityDataCollections) {
    List<ForeignRelations> foreignRelationsList = new ArrayList<>();
    for (IntegrityDataCollection integrityDataCollection : lIntegrityDataCollections) {
      ForeignRelations foreignRelation = new ForeignRelations();
      DataSetMetabase datasetOrigin = new DataSetMetabase();
      datasetOrigin.setId(integrityDataCollection.getIdDatasetOrigin());
      foreignRelation.setIdDatasetOrigin(datasetOrigin);
      DataSetMetabase datasetDestination = new DataSetMetabase();
      DatasetTypeEnum typeDataset =
          datasetMetabaseService.getDatasetType(integrityDataCollection.getIdDatasetOrigin());
      if (DatasetTypeEnum.REPORTING.equals(typeDataset)) {
        Optional<DataSetMetabase> datasetMetabase =
            dataSetMetabaseRepository.findFirstByDatasetSchemaAndDataProviderId(
                integrityDataCollection.getIdDatasetSchemaReferenced(),
                integrityDataCollection.getDataProviderId());
        if (datasetMetabase.isPresent()) {
          datasetDestination.setId(datasetMetabase.get().getId());
        }
      } else if (DatasetTypeEnum.COLLECTION.equals(typeDataset)) {
        Optional<DataCollection> datasetCollection = dataCollectionRepository
            .findFirstByDatasetSchema(integrityDataCollection.getIdDatasetSchemaReferenced());
        if (datasetCollection.isPresent()) {
          datasetDestination.setId(datasetCollection.get().getId());
        }
      } else if (DatasetTypeEnum.EUDATASET.equals(typeDataset)) {
        Optional<EUDataset> euDataset = euDatasetRepository
            .findFirstByDatasetSchema(integrityDataCollection.getIdDatasetSchemaReferenced());
        if (euDataset.isPresent()) {
          datasetDestination.setId(euDataset.get().getId());
        }
      }
      foreignRelation.setIdDatasetDestination(datasetDestination);
      foreignRelation.setIdPk(integrityDataCollection.getIdDatasetSchemaOrigin());
      foreignRelation.setIdFkOrigin(integrityDataCollection.getIdDatasetSchemaReferenced());
      foreignRelationsList.add(foreignRelation);
    }

    if (!foreignRelationsList.isEmpty()) {
      foreignRelationsRepository.saveAll(foreignRelationsList);
    }
  }

  /**
   * Release lock and rollback.
   *
   * @param connection the connection
   * @param dataflowId the dataflow id
   * @param isCreation the is creation
   * @throws SQLException the SQL exception
   */
  private void releaseLockAndRollback(Connection connection, Long dataflowId, boolean isCreation)
      throws SQLException {

    releaseLockAndNotification(dataflowId, "Error creating datasets on the metabase", isCreation);
    connection.rollback();
  }

  /**
   * Persist DC.
   *
   * @param metabaseStatement the metabase statement
   * @param design the design
   * @param time the time
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @return the long
   * @throws SQLException the SQL exception
   */
  private Long persistDC(Statement metabaseStatement, DesignDatasetVO design, String time,
      Long dataflowId, Date dueDate) throws SQLException {
    try (ResultSet rs = metabaseStatement.executeQuery(String.format(INSERT_DC_INTO_DATASET, time,
        dataflowId, String.format(NAME_DC, design.getDataSetName().replace("'", "''")),
        design.getDatasetSchema()))) {
      rs.next();
      Long datasetId = rs.getLong(1);
      metabaseStatement.addBatch(String.format(INSERT_DC_INTO_DATA_COLLECTION, datasetId, dueDate));
      metabaseStatement.addBatch(String.format(INSERT_INTO_PARTITION_DATASET, datasetId));
      return datasetId;
    }
  }

  /**
   * Persist EU.
   *
   * @param metabaseStatement the metabase statement
   * @param design the design
   * @param time the time
   * @param dataflowId the dataflow id
   * @return the long
   * @throws SQLException the SQL exception
   */
  private Long persistEU(Statement metabaseStatement, DesignDatasetVO design, String time,
      Long dataflowId) throws SQLException {
    try (ResultSet rs = metabaseStatement.executeQuery(String.format(INSERT_EU_INTO_DATASET, time,
        dataflowId, String.format(NAME_EU, design.getDataSetName().replace("'", "''")),
        design.getDatasetSchema()))) {
      rs.next();
      Long datasetId = rs.getLong(1);
      metabaseStatement.addBatch(String.format(INSERT_EU_INTO_EU_DATASET, datasetId));
      metabaseStatement.addBatch(String.format(INSERT_INTO_PARTITION_DATASET, datasetId));
      return datasetId;
    }
  }

  /**
   * Persist RD.
   *
   * @param metabaseStatement the metabase statement
   * @param design the design
   * @param representative the representative
   * @param time the time
   * @param dataflowId the dataflow id
   * @param dataProviderLabel the data provider label
   * @return the long
   * @throws SQLException the SQL exception
   */
  private Long persistRD(Statement metabaseStatement, DesignDatasetVO design,
      RepresentativeVO representative, String time, Long dataflowId, String dataProviderLabel)
      throws SQLException {
    try (ResultSet rs =
        metabaseStatement.executeQuery(String.format(INSERT_RD_INTO_DATASET, time, dataflowId,
            dataProviderLabel, design.getDatasetSchema(), representative.getDataProviderId()))) {
      rs.next();
      Long datasetId = rs.getLong(1);
      metabaseStatement.addBatch(String.format(INSERT_RD_INTO_REPORTING_DATASET, datasetId));
      metabaseStatement.addBatch(String.format(INSERT_INTO_PARTITION_DATASET, datasetId));
      return datasetId;
    }
  }

  /**
   * Creates the permissions.
   *
   * @param datasetIdsEmails the dataset ids emails
   * @param dataCollectionIds the data collection ids
   * @param euDatasetIds the eu dataset ids
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void createPermissions(Map<Long, List<String>> datasetIdsEmails,
      List<Long> dataCollectionIds, List<Long> euDatasetIds, Long dataflowId) throws EEAException {

    List<ResourceInfoVO> groups = new ArrayList<>();
    List<ResourceAssignationVO> assignments = new ArrayList<>();

    createGroupsAndAssings(dataflowId, dataCollectionIds, euDatasetIds, datasetIdsEmails, groups,
        assignments);

    // Persist changes in KeyCloak guaranteeing transactionality
    // Insert in chunks to prevent Hystrix timeout
    try {
      persistGroups(groups);
      LOG.info("Groups successfully created: dataflowId={}", dataflowId);

      persistAssignments(assignments);
      LOG.info("Users successfully assigned to groups: dataflowId={}", dataflowId);
    } catch (Exception e) {
      // Rollback group creation
      int size = groups.size();
      for (int i = 0; i < size; i += 10) {
        resourceManagementControllerZuul
            .deleteResourceByDatasetId(groups.subList(i, i + 10 > size ? size : i + 10).stream()
                .map(ResourceInfoVO::getResourceId).collect(Collectors.toList()));
      }
      throw new EEAException(e);
    }
  }

  /**
   * Persist groups.
   *
   * @param groups the groups
   */
  private void persistGroups(List<ResourceInfoVO> groups) {
    int size = groups.size();
    for (int i = 0; i < size; i += 10) {
      resourceManagementControllerZuul
          .createResources(groups.subList(i, i + 10 > size ? size : i + 10));
    }
  }

  /**
   * Persist assignments.
   *
   * @param providerAssignments the provider assignments
   */
  private void persistAssignments(List<ResourceAssignationVO> providerAssignments) {
    int size = providerAssignments.size();
    for (int i = 0; i < size; i += 10) {
      userManagementControllerZuul.addContributorsToResources(
          providerAssignments.subList(i, i + 10 > size ? size : i + 10));
    }
  }

  /**
   * Find users by group.
   *
   * @param group the group
   * @return the list
   */
  private List<UserRepresentationVO> findUsersByGroup(String group) {
    List<UserRepresentationVO> rtn = userManagementControllerZuul.getUsersByGroup(group);
    return null != rtn ? rtn : new ArrayList<>();
  }

  /**
   * Creates the groups and assings.
   *
   * @param dataflowId the dataflow id
   * @param dataCollectionIds the data collection ids
   * @param euDatasetIds the eu dataset ids
   * @param datasetIdsEmails the dataset ids emails
   * @param groups the groups
   * @param assignments the assignments
   */
  private void createGroupsAndAssings(Long dataflowId, List<Long> dataCollectionIds,
      List<Long> euDatasetIds, Map<Long, List<String>> datasetIdsEmails,
      List<ResourceInfoVO> groups, List<ResourceAssignationVO> assignments) {

    List<UserRepresentationVO> stewards =
        findUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(dataflowId));
    List<UserRepresentationVO> custodians =
        findUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(dataflowId));

    for (Long dataCollectionId : dataCollectionIds) {

      // Create DataCollection-%s-DATA_STEWARD
      groups.add(createGroup(dataCollectionId, ResourceTypeEnum.DATA_COLLECTION,
          SecurityRoleEnum.DATA_STEWARD));

      // Create DataCollection-%s-DATA_CUSTODIAN
      groups.add(createGroup(dataCollectionId, ResourceTypeEnum.DATA_COLLECTION,
          SecurityRoleEnum.DATA_CUSTODIAN));

      // Assign DataCollection-%s-DATA_STEWARD
      for (UserRepresentationVO steward : stewards) {
        assignments.add(createAssignments(dataCollectionId, steward.getEmail(),
            ResourceGroupEnum.DATACOLLECTION_STEWARD));
      }

      // Assign DataCollection-%s-DATA_CUSTODIAN
      for (UserRepresentationVO custodian : custodians) {
        assignments.add(createAssignments(dataCollectionId, custodian.getEmail(),
            ResourceGroupEnum.DATACOLLECTION_CUSTODIAN));
      }
    }

    for (Long euDatasetId : euDatasetIds) {

      // Create EUDataset-%s-DATA_STEWARD
      groups.add(
          createGroup(euDatasetId, ResourceTypeEnum.EU_DATASET, SecurityRoleEnum.DATA_STEWARD));

      // Create EUDataset-%s-DATA_CUSTODIAN
      groups.add(
          createGroup(euDatasetId, ResourceTypeEnum.EU_DATASET, SecurityRoleEnum.DATA_CUSTODIAN));

      // Assign DataCollection-%s-DATA_STEWARD
      for (UserRepresentationVO steward : stewards) {
        assignments.add(createAssignments(euDatasetId, steward.getEmail(),
            ResourceGroupEnum.EUDATASET_STEWARD));
      }

      // Assign EUDataset-%s-DATA_CUSTODIAN
      for (UserRepresentationVO custodian : custodians) {
        assignments.add(createAssignments(euDatasetId, custodian.getEmail(),
            ResourceGroupEnum.EUDATASET_CUSTODIAN));
      }
    }

    for (Map.Entry<Long, List<String>> entry : datasetIdsEmails.entrySet()) {

      // Create Dataset-%s-DATA_STEWARD
      groups.add(
          createGroup(entry.getKey(), ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_STEWARD));

      // Create Dataset-%s-DATA_CUSTODIAN
      groups.add(
          createGroup(entry.getKey(), ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_CUSTODIAN));

      // Create Dataset-%s-LEAD_REPORTER
      groups.add(
          createGroup(entry.getKey(), ResourceTypeEnum.DATASET, SecurityRoleEnum.LEAD_REPORTER));

      // Assign Dataset-%s-DATA_STEWARD
      for (UserRepresentationVO steward : stewards) {
        assignments.add(createAssignments(entry.getKey(), steward.getEmail(),
            ResourceGroupEnum.DATASET_STEWARD));
      }

      // Assign Dataset-%s-DATA_CUSTODIAN
      for (UserRepresentationVO custodian : custodians) {
        assignments.add(createAssignments(entry.getKey(), custodian.getEmail(),
            ResourceGroupEnum.DATASET_CUSTODIAN));
      }

      if (null != entry.getValue()) {
        for (String email : entry.getValue()) {
          // Assign Dataset-%s-LEAD_REPORTER
          assignments.add(
              createAssignments(entry.getKey(), email, ResourceGroupEnum.DATASET_LEAD_REPORTER));

          // Assign Dataflow-%s-LEAD_REPORTER
          assignments
              .add(createAssignments(dataflowId, email, ResourceGroupEnum.DATAFLOW_LEAD_REPORTER));
        }
      }
    }
  }

  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   *
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);
    resourceInfoVO.setName(type + "-" + datasetId + "-" + role);

    return resourceInfoVO;
  }

  /**
   * Creates the assignments.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   *
   * @return the resource assignation VO
   */
  private ResourceAssignationVO createAssignments(Long id, String email, ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
  }


  /**
   * Find id dataset destination.
   *
   * @param idDatasetSchemaDestination the id dataset schema destination
   * @param listFkData the list fk data
   *
   * @return the long
   */
  private Long findIdDatasetDestination(String idDatasetSchemaDestination,
      List<FKDataCollection> listFkData) {
    Long idDataset = 0L;
    Optional<FKDataCollection> fk = listFkData.stream()
        .filter(fkey -> fkey.getIdDatasetSchemaOrigin().equals(idDatasetSchemaDestination))
        .findFirst();
    if (fk.isPresent()) {
      idDataset = fk.get().getIdDatasetOrigin();
    }
    return idDataset;
  }

  /**
   * Adds the foreign relations from new reportings.
   *
   * @param datasetsRegistry the datasets registry
   */
  @Override
  public void addForeignRelationsFromNewReportings(List<FKDataCollection> datasetsRegistry) {
    List<ForeignRelations> foreignRelations = new ArrayList<>();
    Map<String, List<FKDataCollection>> groupByRepresentative = datasetsRegistry.stream()
        .collect(Collectors.groupingBy(FKDataCollection::getRepresentative));

    groupByRepresentative.forEach((representative, listFkData) -> {
      for (FKDataCollection fkData : listFkData) {
        if (fkData.getFks() != null && !fkData.getFks().isEmpty()) {
          for (ReferencedFieldSchema referenced : fkData.getFks()) {
            ForeignRelations foreign = new ForeignRelations();
            foreign.setIdPk(referenced.getIdPk().toString());
            DataSetMetabase dsOrigin = new DataSetMetabase();
            dsOrigin.setId(fkData.getIdDatasetOrigin());
            foreign.setIdDatasetOrigin(dsOrigin);
            DataSetMetabase dsDestination = new DataSetMetabase();
            dsDestination.setId(
                findIdDatasetDestination(referenced.getIdDatasetSchema().toString(), listFkData));
            foreign.setIdDatasetDestination(dsDestination);
            foreignRelations.add(foreign);
          }
        }
      }

    });
    // Save all the FK relations between datasets into the metabase
    if (!foreignRelations.isEmpty()) {
      foreignRelationsRepository.saveAll(foreignRelations);
    }
  }


  /**
   * Adds the foreign relations from new DC and EUs.
   *
   * @param newDCandEUsRegistry the new DC and EUs registry
   */
  private void addForeignRelationsFromNewDCandEUs(List<FKDataCollection> newDCandEUsRegistry) {
    List<ForeignRelations> foreignRelations = new ArrayList<>();

    for (FKDataCollection fkData : newDCandEUsRegistry) {
      if (fkData.getFks() != null && !fkData.getFks().isEmpty()) {
        for (ReferencedFieldSchema referenced : fkData.getFks()) {
          ForeignRelations foreign = new ForeignRelations();
          foreign.setIdPk(referenced.getIdPk().toString());
          DataSetMetabase dsOrigin = new DataSetMetabase();
          dsOrigin.setId(fkData.getIdDatasetOrigin());
          foreign.setIdDatasetOrigin(dsOrigin);
          DataSetMetabase dsDestination = new DataSetMetabase();
          dsDestination.setId(findIdDatasetDestination(referenced.getIdDatasetSchema().toString(),
              newDCandEUsRegistry));
          foreign.setIdDatasetDestination(dsDestination);
          foreignRelations.add(foreign);
        }
      }
    }

    // Save all the FK relations between dc and eus into the metabase
    if (!foreignRelations.isEmpty()) {
      foreignRelationsRepository.saveAll(foreignRelations);
    }
  }


  /**
   * Release notification.
   *
   * @param eventType the event type
   * @param notificationVO the notification VO
   */
  private void releaseNotification(EventType eventType, NotificationVO notificationVO) {
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Unable to release notification: {}, {}", eventType, notificationVO);
    }
  }

}
