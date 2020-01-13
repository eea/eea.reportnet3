package org.eea.dataset.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class DatasetMetabaseServiceImpl.
 */
@Service("datasetMetabaseService")
public class DatasetMetabaseServiceImpl implements DatasetMetabaseService {

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The data set metabase mapper. */
  @Autowired
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The statistics repository. */
  @Autowired
  private StatisticsRepository statisticsRepository;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The user management controller zuul. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /** The resource management controller zuul. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  @Lazy
  private KafkaSenderUtils kafkaSenderUtils;



  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMetabaseServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   * @return the data set id by dataflow id
   */
  @Override
  public List<DataSetMetabaseVO> getDataSetIdByDataflowId(Long idFlow) {

    List<DataSetMetabase> datasets = dataSetMetabaseRepository.findByDataflowId(idFlow);
    return dataSetMetabaseMapper.entityListToClass(datasets);
  }



  /**
   * Fill dataset.
   *
   * @param dataset the dataset
   * @param datasetName the dataset name
   * @param idDataFlow the id data flow
   * @param datasetSchemaId the dataset schema id
   */
  private void fillDataset(DataSetMetabase dataset, String datasetName, Long idDataFlow,
      String datasetSchemaId) {

    dataset.setDataSetName(datasetName);
    dataset.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    dataset.setDataflowId(idDataFlow);
    dataset.setDatasetSchema(datasetSchemaId);

    PartitionDataSetMetabase partition = new PartitionDataSetMetabase();
    partition.setUsername("root");
    partition.setIdDataSet(dataset);

    dataset.setPartitions(Arrays.asList(partition));
  }


  /**
   * Gets the dataset name.
   *
   * @param idDataset the id dataset
   * @return the dataset name
   */
  @Override
  public DataSetMetabaseVO findDatasetMetabase(Long idDataset) {
    Optional<DataSetMetabase> datasetMetabase = dataSetMetabaseRepository.findById(idDataset);
    return dataSetMetabaseMapper.entityToClass(datasetMetabase.get());
  }

  /**
   * Delete design dataset.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteDesignDataset(Long datasetId) {
    dataSetMetabaseRepository.deleteById(datasetId);
  }

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   * @return true, if successful
   */
  @Override
  public boolean updateDatasetName(Long datasetId, String datasetName) {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId).orElse(null);
    if (datasetMetabase != null) {
      datasetMetabase.setDataSetName(datasetName);
      dataSetMetabaseRepository.save(datasetMetabase);
      return true;
    }
    return false;
  }



  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   * @return the statistics
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  public StatisticsVO getStatistics(final Long datasetId)
      throws EEAException, InstantiationException, IllegalAccessException {

    List<Statistics> statistics = statisticsRepository.findStatisticsByIdDataset(datasetId);
    return processStatistics(statistics);
  }


  /**
   * Sets the entity property.
   *
   * @param object the object
   * @param fieldName the field name
   * @param fieldValue the field value
   *
   * @return the boolean
   */
  public static Boolean setEntityProperty(Object object, String fieldName, String fieldValue) {
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      try {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        if (field.getType().equals(Long.class)) {
          field.set(object, Long.valueOf(fieldValue));
        } else if (field.getType().equals(Boolean.class)) {
          field.set(object, Boolean.valueOf(fieldValue));
        } else {
          field.set(object, fieldValue);
        }

        return true;
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    return false;
  }


  /**
   * Process statistics.
   *
   * @param statistics the statistics
   * @return the statistics VO
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  private StatisticsVO processStatistics(List<Statistics> statistics)
      throws InstantiationException, IllegalAccessException {

    StatisticsVO stats = new StatisticsVO();

    List<Statistics> statisticsTables = statistics.stream()
        .filter(s -> StringUtils.isNotBlank(s.getIdTableSchema())).collect(Collectors.toList());
    List<Statistics> statisticsDataset = statistics.stream()
        .filter(s -> StringUtils.isBlank(s.getIdTableSchema())).collect(Collectors.toList());

    Map<String, List<Statistics>> tablesMap = statisticsTables.stream()
        .collect(Collectors.groupingBy(Statistics::getIdTableSchema, Collectors.toList()));

    // Dataset level stats
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    statisticsDataset.stream().forEach(s -> {
      setEntityProperty(instance, s.getStatName(), s.getValue());
    });
    stats = (StatisticsVO) instance;

    // Table statistics
    stats.setTables(new ArrayList<>());
    for (List<Statistics> listStats : tablesMap.values()) {
      Class<?> clazzTable = TableStatisticsVO.class;
      Object instanceTable = clazzTable.newInstance();
      listStats.stream().forEach(s -> {
        setEntityProperty(instanceTable, s.getStatName(), s.getValue());
      });
      stats.getTables().add((TableStatisticsVO) instanceTable);
    }

    return stats;


  }



  /**
   * Gets the global statistics.
   *
   * @param dataschemaId the dataschema id
   * @return the global statistics
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  public List<StatisticsVO> getGlobalStatistics(String dataschemaId)
      throws EEAException, InstantiationException, IllegalAccessException {

    List<StatisticsVO> statistics = new ArrayList<>();

    List<Statistics> stats = statisticsRepository.findStatisticsByIdDatasetSchema(dataschemaId);

    Map<ReportingDataset, List<Statistics>> statsMap =
        stats.stream().collect(Collectors.groupingBy(Statistics::getDataset, Collectors.toList()));

    statsMap.values().stream().forEach(s -> {
      try {
        statistics.add(processStatistics(s));
      } catch (InstantiationException | IllegalAccessException e) {
        LOG_ERROR.error("Error getting global statistics. Error message: {}", e.getMessage(), e);
      }
    });

    return statistics;
  }



  /**
   * Creates the group provider and add user.
   *
   * @param datasetId the dataset id
   * @param userMail the user mail
   * @param dataflowId the dataflow id
   */
  @Override
  public void createGroupProviderAndAddUser(Long datasetId, String userMail, Long dataflowId) {

    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_PROVIDER));

    userManagementControllerZuul.addContributorToResource(datasetId,
        ResourceGroupEnum.DATASET_PROVIDER, userMail);

    userManagementControllerZuul.addContributorToResource(dataflowId,
        ResourceGroupEnum.DATAFLOW_PROVIDER, userMail);

    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_CUSTODIAN));

    userManagementControllerZuul.addUserToResource(datasetId, ResourceGroupEnum.DATASET_CUSTODIAN);

  }


  /**
   * Creates the group dc and add user.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createGroupDcAndAddUser(Long datasetId) {

    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_COLLECTION, SecurityRoleEnum.DATA_CUSTODIAN));

    userManagementControllerZuul.addUserToResource(datasetId,
        ResourceGroupEnum.DATACOLLECTION_CUSTODIAN);

  }

  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);

    return resourceInfoVO;
  }



  /**
   * Creates the empty dataset.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param representative the representative
   * @return the future
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  @org.springframework.transaction.annotation.Transactional(
      value = "metabaseDataSetsTransactionManager")
  public Future<Long> createEmptyDataset(TypeDatasetEnum datasetType, String datasetName,
      String datasetSchemaId, Long dataflowId, Date dueDate, RepresentativeVO representative,
      Integer iterationDC) throws EEAException {

    if (datasetType != null && dataflowId != null) {
      DataSetMetabase dataset;

      switch (datasetType) {
        case REPORTING:
          dataset = new ReportingDataset();
          DataProviderVO provider =
              representativeControllerZuul.findDataProviderById(representative.getDataProviderId());
          datasetName = provider.getLabel();
          fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
          dataset.setDataProviderId(representative.getDataProviderId());
          reportingDatasetRepository.save((ReportingDataset) dataset);
          if (StringUtils.isNotBlank(representative.getProviderAccount())) {
            this.createGroupProviderAndAddUser(dataset.getId(), representative.getProviderAccount(),
                dataflowId);
          }
          break;
        case DESIGN:
          dataset = new DesignDataset();
          fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
          designDatasetRepository.save((DesignDataset) dataset);
          this.createSchemaGroupAndAddUser(dataset.getId());
          break;
        case COLLECTION:
          dataset = new DataCollection();
          fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
          ((DataCollection) dataset).setDueDate(dueDate);
          dataCollectionRepository.save((DataCollection) dataset);
          this.createGroupDcAndAddUser(dataset.getId());
          if (iterationDC == 0) {
            // Notification
            kafkaSenderUtils
                .releaseNotificableKafkaEvent(EventType.ADD_DATACOLLECTION_COMPLETED_EVENT, null,
                    NotificationVO.builder()
                        .user((String) ThreadPropertiesManager.getVariable("user"))
                        .dataflowId(dataflowId).build());

          }
          break;
        default:
          throw new EEAException("Unsupported datasetType: " + datasetType);
      }

      recordStoreControllerZull.createEmptyDataset("dataset_" + dataset.getId(), datasetSchemaId);

      return new AsyncResult<Long>(dataset.getId());
    }

    throw new EEAException("createEmptyDataset: Bad arguments");

  }


  /**
   * Creates the schema group and add user.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createSchemaGroupAndAddUser(Long datasetId) {

    // Create group Dataschema-X-DATA_CUSTODIAN
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_CUSTODIAN));

    // Create group Dataschema-X-DATA_PROVIDER
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_PROVIDER));

    // Add user to new group Dataschema-X-DATA_CUSTODIAN
    userManagementControllerZuul.addUserToResource(datasetId,
        ResourceGroupEnum.DATASCHEMA_CUSTODIAN);
  }



}
