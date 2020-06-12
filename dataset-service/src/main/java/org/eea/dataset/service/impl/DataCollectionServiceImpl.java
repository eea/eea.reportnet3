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
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.model.FKDataCollection;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The Class DataCollectionServiceImpl.
 */
@Service("dataCollectionService")
public class DataCollectionServiceImpl implements DataCollectionService {

  /**
   * The metabase data source.
   */
  @Autowired
  @Qualifier("metabaseDatasource")
  private DataSource metabaseDataSource;

  /**
   * The data collection repository.
   */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /**
   * The data collection mapper.
   */
  @Autowired
  private DataCollectionMapper dataCollectionMapper;

  /**
   * The design dataset service.
   */
  @Autowired
  private DesignDatasetService designDatasetService;

  /**
   * The representative controller zuul.
   */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The dataflow controller zuul.
   */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * The resource management controller zuul.
   */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /**
   * The user management controller zuul.
   */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /**
   * The lock service.
   */
  @Autowired
  private LockService lockService;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The dataset schema service.
   */
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  /**
   * The foreign relations repository.
   */
  @Autowired
  private ForeignRelationsRepository foreignRelationsRepository;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant NAME_DC.
   */
  private static final String NAME_DC = "Data Collection - %s";

  /**
   * The Constant UPDATE_DATAFLOW_STATUS.
   */
  private static final String UPDATE_DATAFLOW_STATUS =
      "update dataflow set status = '%s', deadline_date = '%s' where id = %d";

  private static final String UPDATE_REPRESENTATIVE_HAS_DATASETS =
      "update representative set has_datasets = %b where id = %d;";

  /**
   * The Constant INSERT_DC_INTO_DATASET.
   */
  private static final String INSERT_DC_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema) values ('%s', %d, '%s', '%s') returning id";

  /**
   * The Constant INSERT_DC_INTO_DATA_COLLECTION.
   */
  private static final String INSERT_DC_INTO_DATA_COLLECTION =
      "insert into data_collection (id, due_date) values (%d, '%s')";

  /**
   * The Constant INSERT_RD_INTO_DATASET.
   */
  private static final String INSERT_RD_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema, data_provider_id) values ('%s', %d, '%s', '%s', %d) returning id";

  /**
   * The Constant INSERT_RD_INTO_REPORTING_DATASET.
   */
  private static final String INSERT_RD_INTO_REPORTING_DATASET =
      "insert into reporting_dataset (id) values (%d)";

  /**
   * The Constant INSERT_INTO_PARTITION_DATASET.
   */
  private static final String INSERT_INTO_PARTITION_DATASET =
      "insert into partition_dataset (user_name, id_dataset) values ('root', %d)";

  /**
   * Gets the dataflow status.
   *
   * @param dataflowId the dataflow id
   *
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
   *
   * @return the data collection id by dataflow id
   */
  @Override
  public List<DataCollectionVO> getDataCollectionIdByDataflowId(Long idFlow) {
    List<DataCollection> datacollections = dataCollectionRepository.findByDataflowId(idFlow);
    return dataCollectionMapper.entityListToClass(datacollections);
  }

  /**
   * Rollback data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   */
  @Override
  public void undoDataCollectionCreation(List<Long> datasetIds, Long dataflowId,
      boolean isCreation) {

    releaseLockAndNotification(dataflowId, "Error creating schemas", isCreation);

    int size = datasetIds.size();
    for (int i = 0; i < size; i += 10) {
      resourceManagementControllerZuul
          .deleteResourceByDatasetId(datasetIds.subList(i, i + 10 > size ? size : i + 10));
    }
    dataCollectionRepository.deleteDatasetById(datasetIds);
    dataflowControllerZuul.updateDataFlowStatus(dataflowId, TypeStatusEnum.DESIGN, null);
  }

  private void releaseLockAndNotification(Long dataflowId, String errorMessage,
      boolean isCreation) {
    String methodSignature = isCreation ? LockSignature.CREATE_DATA_COLLECTION.getValue()
        : LockSignature.UPDATE_DATA_COLLECTION.getValue();
    EventType failEvent = isCreation ? EventType.ADD_DATACOLLECTION_FAILED_EVENT
        : EventType.UPDATE_DATACOLLECTION_FAILED_EVENT;

    // Release the lock
    List<Object> criteria = new ArrayList<>();
    criteria.add(methodSignature);
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

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
    manageDataCollection(dataflowId, null, false);
  }

  /**
   * Creates the empty data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   */
  @Override
  @Async
  public void createEmptyDataCollection(Long dataflowId, Date dueDate) {
    manageDataCollection(dataflowId, dueDate, true);
  }

  /**
   * Manage data collection.
   *
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param isCreation the is creation
   */
  private void manageDataCollection(Long dataflowId, Date dueDate, boolean isCreation) {
    String time = Timestamp.valueOf(LocalDateTime.now()).toString();

    // 1. Get the design datasets
    List<DesignDatasetVO> designs = designDatasetService.getDesignDataSetIdByDataflowId(dataflowId);

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

    List<Long> dataCollectionIds = new ArrayList<>();
    Map<Long, String> datasetIdsEmails = new HashMap<>();
    Map<Long, String> datasetIdsAndSchemaIds = new HashMap<>();

    try (Connection connection = metabaseDataSource.getConnection();
        Statement statement = connection.createStatement()) {

      try {
        connection.setAutoCommit(false);

        if (isCreation) {
          // 5. Set dataflow to DRAFT
          statement.addBatch(
              String.format(UPDATE_DATAFLOW_STATUS, TypeStatusEnum.DRAFT, dueDate, dataflowId));
        }

        for (RepresentativeVO representative : representatives) {
          statement.addBatch(
              String.format(UPDATE_REPRESENTATIVE_HAS_DATASETS, true, representative.getId()));
        }

        List<FKDataCollection> newReportingDatasetsRegistry = new ArrayList<>();

        for (DesignDatasetVO design : designs) {
          if (isCreation) {
            // 6. Create DataCollection in metabase
            Long dataCollectionId = persistDC(statement, design, time, dataflowId, dueDate);
            dataCollectionIds.add(dataCollectionId);
            datasetIdsAndSchemaIds.put(dataCollectionId, design.getDatasetSchema());
          }

          // 7. Create Reporting Dataset in metabase

          for (RepresentativeVO representative : representatives) {
            Long datasetId = persistRD(statement, design, representative, time, dataflowId,
                map.get(representative.getDataProviderId()));
            datasetIdsEmails.put(datasetId, representative.getProviderAccount());
            datasetIdsAndSchemaIds.put(datasetId, design.getDatasetSchema());

            FKDataCollection newReporting = new FKDataCollection();
            newReporting.setRepresentative(map.get(representative.getDataProviderId()));
            newReporting.setIdDatasetSchemaOrigin(design.getDatasetSchema());
            newReporting.setIdDatasetOrigin(datasetId);
            newReporting.setFks(
                datasetSchemaService.getReferencedFieldsBySchema(design.getDatasetSchema()));
            newReportingDatasetsRegistry.add(newReporting);
          }
        }

        statement.executeBatch();
        // 8. Create permissions
        createPermissions(datasetIdsEmails, dataCollectionIds, dataflowId);
        connection.commit();
        // Add into the foreign_relations table from metabase the dataset origin-destination
        // relation, if applies
        addForeignRelationsFromNewReportings(newReportingDatasetsRegistry);

        LOG.info("Metabase changes completed on DataCollection creation");

        // 9. Create schemas for each dataset
        // This method will release the lock
        recordStoreControllerZull.createSchemas(datasetIdsAndSchemaIds, dataflowId, isCreation);
      } catch (SQLException e) {
        LOG_ERROR.error("Error persisting changes. Rolling back...", e);
        releaseLockAndRollback(connection, dataflowId, isCreation);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating permissions. Rolling back...", e);
        releaseLockAndRollback(connection, dataflowId, isCreation);
      } finally {
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      LOG_ERROR.error("Error rolling back: ", e);
    }
  }

  /**
   * Release lock and rollback.
   *
   * @param connection the connection
   * @param dataflowId the dataflow id
   *
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
   *
   * @return the long
   *
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
   * Persist RD.
   *
   * @param metabaseStatement the metabase statement
   * @param design the design
   * @param representative the representative
   * @param time the time
   * @param dataflowId the dataflow id
   * @param dataProviderLabel the data provider label
   *
   * @return the long
   *
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
   * @param dataflowId the dataflow id
   *
   * @throws EEAException the EEA exception
   */
  private void createPermissions(Map<Long, String> datasetIdsEmails, List<Long> dataCollectionIds,
      Long dataflowId) throws EEAException {

    List<ResourceInfoVO> groups = new ArrayList<>();
    List<ResourceAssignationVO> providerAssignments = new ArrayList<>();
    List<ResourceAssignationVO> custodianAssignments = new ArrayList<>();

    // Create DataCollection groups and assign custodian to self user
    for (Long dataCollectionId : dataCollectionIds) {
      groups.add(createGroup(dataCollectionId, ResourceTypeEnum.DATA_COLLECTION,
          SecurityRoleEnum.DATA_CUSTODIAN));
      custodianAssignments.add(
          createAssignments(dataCollectionId, null, ResourceGroupEnum.DATACOLLECTION_CUSTODIAN));
    }

    // Create DATASET_PROVIDER and DATA_CUSTODIAN groups
    // Assign DATAFLOW_PROVIDER and DATA_PROVIDER to representatives and DATA_CUSTODIAN to self user
    for (Map.Entry<Long, String> entry : datasetIdsEmails.entrySet()) {
      groups.add(
          createGroup(entry.getKey(), ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_PROVIDER));
      providerAssignments.add(
          createAssignments(entry.getKey(), entry.getValue(), ResourceGroupEnum.DATASET_PROVIDER));
      providerAssignments.add(
          createAssignments(dataflowId, entry.getValue(), ResourceGroupEnum.DATAFLOW_PROVIDER));
      groups.add(
          createGroup(entry.getKey(), ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_CUSTODIAN));
      custodianAssignments
          .add(createAssignments(entry.getKey(), null, ResourceGroupEnum.DATASET_CUSTODIAN));
    }

    // Persist changes in KeyCloak guaranteeing transactionality
    // Insert in chunks to prevent Hystrix timeout
    int chunks = 0;
    try {

      // Persist groups
      int size = groups.size();
      for (int i = 0; i < size; i += 10, chunks++) {
        resourceManagementControllerZuul
            .createResources(groups.subList(i, i + 10 > size ? size : i + 10));
      }

      // Persist provider assignments
      size = providerAssignments.size();
      for (int i = 0; i < size; i += 10) {
        userManagementControllerZuul.addContributorsToResources(
            providerAssignments.subList(i, i + 10 > size ? size : i + 10));
      }

      // Persist custodian assignments
      size = custodianAssignments.size();
      for (int i = 0; i < size; i += 10) {
        userManagementControllerZuul
            .addUserToResources(custodianAssignments.subList(i, i + 10 > size ? size : i + 10));
      }
    } catch (Exception e) {
      // Undo group creation
      int size = chunks * 10 < groups.size() ? chunks * 10 : groups.size();
      List<Long> rollback = groups.subList(0, size).stream().map(ResourceInfoVO::getResourceId)
          .collect(Collectors.toList());
      for (int i = 0; i < size; i += 10) {
        resourceManagementControllerZuul
            .deleteResourceByDatasetId(rollback.subList(i, i + 10 > size ? size : i + 10));
      }
      throw new EEAException(e);
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
}
