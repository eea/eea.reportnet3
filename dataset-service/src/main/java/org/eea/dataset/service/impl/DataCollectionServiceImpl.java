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
import javax.sql.DataSource;
import org.eea.dataset.mapper.DataCollectionMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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

/** The Class DataCollectionServiceImpl. */
@Service("dataCollectionService")
public class DataCollectionServiceImpl implements DataCollectionService {

  /** The metabase data source. */
  @Autowired
  @Qualifier("metabaseDataSource")
  private DataSource metabaseDataSource;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The data collection mapper. */
  @Autowired
  private DataCollectionMapper dataCollectionMapper;

  /** The design dataset service. */
  @Autowired
  private DesignDatasetService designDatasetService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataCollectionServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant NAME_DC. */
  private static final String NAME_DC = "Data Collection - %s";

  /** The Constant UPDATE_DATAFLOW_STATUS. */
  private static final String UPDATE_DATAFLOW_STATUS =
      "update dataflow set status = '%s' where id = %d";

  /** The Constant INSERT_DC_INTO_DATASET. */
  private static final String INSERT_DC_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema) values ('%s', %d, '%s', '%s') returning id";

  /** The Constant INSERT_DC_INTO_DATA_COLLECTION. */
  private static final String INSERT_DC_INTO_DATA_COLLECTION =
      "insert into data_collection (id, due_date) values (%d, '%s')";

  /** The Constant INSERT_RD_INTO_DATASET. */
  private static final String INSERT_RD_INTO_DATASET =
      "insert into dataset (date_creation, dataflowid, dataset_name, dataset_schema, data_provider_id) values ('%s', %d, '%s', '%s', %d) returning id";

  /** The Constant INSERT_RD_INTO_REPORTING_DATASET. */
  private static final String INSERT_RD_INTO_REPORTING_DATASET =
      "insert into reporting_dataset (id) values (%d)";

  /** The Constant INSERT_INTO_PARTITION_DATASET. */
  private static final String INSERT_INTO_PARTITION_DATASET =
      "insert into partition_dataset (user_name, id_dataset) values ('root', %d)";

  /**
   * Creates the permissions.
   *
   * @param datasetIdsEmails the dataset ids emails
   * @param dataCollectionIds the data collection ids
   * @param dataflowId the dataflow id
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private void createPermissions(Map<Long, String> datasetIdsEmails, List<Long> dataCollectionIds,
      Long dataflowId) throws EEAException {
    try {
      datasetMetabaseService.createGroupProviderAndAddUser(datasetIdsEmails, dataflowId);
      for (Long dataCollectionId : dataCollectionIds) {
        datasetMetabaseService.createGroupDcAndAddUser(dataCollectionId);
      }
    } catch (Exception e) {
      throw new EEAException(e);
    }
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
        dataflowId, String.format(NAME_DC, design.getDataSetName()), design.getDatasetSchema()))) {
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
   * @return the long
   * @throws SQLException the SQL exception
   */
  private Long persistRD(Statement metabaseStatement, DesignDatasetVO design,
      RepresentativeVO representative, String time, Long dataflowId) throws SQLException {
    try (ResultSet rs = metabaseStatement.executeQuery(String.format(INSERT_RD_INTO_DATASET, time,
        dataflowId, String.format(NAME_DC, design.getDataSetName()), design.getDatasetSchema(),
        representative.getId()))) {
      rs.next();
      Long datasetId = rs.getLong(1);
      metabaseStatement.addBatch(String.format(INSERT_RD_INTO_REPORTING_DATASET, datasetId));
      metabaseStatement.addBatch(String.format(INSERT_INTO_PARTITION_DATASET, datasetId));
      return datasetId;
    }
  }

  /**
   * Release lock and rollback.
   *
   * @param connection the connection
   * @param dataflowId the dataflow id
   * @throws SQLException the SQL exception
   */
  private void releaseLockAndRollback(Connection connection, Long dataflowId) throws SQLException {

    // Release the lock
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.CREATE_DATA_COLLECTION.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    // Release the notification
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DATA_COLLECTION_CREATION_FAILED_EVENT,
          null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataflowId).error("Error creating datasets on the metabase").build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing {} event: ",
          EventType.DATA_COLLECTION_CREATION_COMPLETED_EVENT, e);
    }

    connection.rollback();
  }

  /**
   * Rollback data collection creation.
   *
   * @param datasetIds the dataset ids
   * @param dataflowId the dataflow id
   */
  @Override
  public void undoDataCollectionCreation(List<Long> datasetIds, Long dataflowId) {

    // Release the lock
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.CREATE_DATA_COLLECTION.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);

    // Release the notification
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DATA_COLLECTION_CREATION_FAILED_EVENT,
          null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .dataflowId(dataflowId).error("Error creating schemas").build());
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing {} event: ",
          EventType.DATA_COLLECTION_CREATION_COMPLETED_EVENT, e);
    }

    dataCollectionRepository.deleteDatasetById(datasetIds);
    dataCollectionRepository.updateDataflowStatus(dataflowId, TypeStatusEnum.DESIGN.getValue());
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

    String time = Timestamp.valueOf(LocalDateTime.now()).toString();

    // 1. Get the design datasets
    List<DesignDatasetVO> designs = designDatasetService.getDesignDataSetIdByDataflowId(dataflowId);

    // 2. Get the providers who are going to provide data
    List<RepresentativeVO> representatives =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);

    List<Long> dataCollectionIds = new ArrayList<>();
    Map<Long, String> datasetIdsEmails = new HashMap<>();
    Map<Long, String> datasetIdsAndSchemaIds = new HashMap<>();

    try (Connection connection = metabaseDataSource.getConnection();
        Statement statement = connection.createStatement();) {

      try {
        connection.setAutoCommit(false);

        // 3. Set dataflow to DRAFT
        statement.addBatch(String.format(UPDATE_DATAFLOW_STATUS, TypeStatusEnum.DRAFT, dataflowId));

        for (DesignDatasetVO design : designs) {
          // 4. Create DataCollection in metabase
          Long dataCollectionId = persistDC(statement, design, time, dataflowId, dueDate);
          dataCollectionIds.add(dataCollectionId);
          datasetIdsAndSchemaIds.put(dataCollectionId, design.getDatasetSchema());

          // 5. Create Reporting Dataset in metabase
          for (RepresentativeVO representative : representatives) {
            Long datasetId = persistRD(statement, design, representative, time, dataflowId);
            datasetIdsEmails.put(datasetId, representative.getProviderAccount());
            datasetIdsAndSchemaIds.put(datasetId, design.getDatasetSchema());
          }
        }

        statement.executeBatch();
        // 7. Create permissions
        createPermissions(datasetIdsEmails, dataCollectionIds, dataflowId);
        connection.commit();
        LOG.info("Metabase changes completed on DataCollection creation");

        // 6. Create schemas for each dataset
        // This method will release the lock
        recordStoreControllerZull.createSchemas(datasetIdsAndSchemaIds, dataflowId);
      } catch (SQLException e) {
        LOG_ERROR.error("Error persisting changes. Rolling back...", e);
        releaseLockAndRollback(connection, dataflowId);
      } catch (EEAException e) {
        LOG_ERROR.error("Error creating permissions. Rolling back...", e);
        releaseLockAndRollback(connection, dataflowId);
      } finally {
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      LOG_ERROR.error("Error rolling back: ", e);
    }
  }

  /**
   * Checks if is design dataflow.
   *
   * @param dataflowId the dataflow id
   * @return true, if is design dataflow
   */
  @Override
  public boolean isDesignDataflow(Long dataflowId) {
    DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(dataflowId);
    return (dataflowVO != null && TypeStatusEnum.DESIGN.equals(dataflowVO.getStatus()));
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



}
