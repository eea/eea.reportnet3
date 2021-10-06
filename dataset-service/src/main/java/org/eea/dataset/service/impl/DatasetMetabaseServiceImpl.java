package org.eea.dataset.service.impl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.domain.TestDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.persistence.metabase.repository.TestDatasetRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.collaboration.CollaborationController.CollaborationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.ums.ResourceAssignationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DatasetMetabaseServiceImpl.
 */
@Service("datasetMetabaseService")
public class DatasetMetabaseServiceImpl implements DatasetMetabaseService {

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;


  /** The collaboration controller zuul. */
  @Autowired
  private CollaborationControllerZuul collaborationControllerZuul;

  /** The data set metabase mapper. */
  @Autowired
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

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

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The kafka sender utils. */
  @Autowired
  @Lazy
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The foreign relations repository.
   */
  @Autowired
  private ForeignRelationsRepository foreignRelationsRepository;

  /** The eu dataset repository. */
  @Autowired
  private EUDatasetRepository euDatasetRepository;

  /** The test dataset repository. */
  @Autowired
  private TestDatasetRepository testDatasetRepository;

  /** The reference dataset repository. */
  @Autowired
  private ReferenceDatasetRepository referenceDatasetRepository;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMetabaseServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant STATUS_TECHNICALLY_ACCEPTED: {@value}. */
  private static final String STATUS_TECHNICALLY_ACCEPTED =
      "Feedback status changed: Technically Accepted, Message: ";

  /** The Constant STATUS_CORRECTION_REQUESTED: {@value}. */
  private static final String STATUS_CORRECTION_REQUESTED =
      "Feedback status changed: Correction Requested, Message: ";

  /** The Constant STATUS_CHANGED: {@value}. */
  private static final String STATUS_CHANGED = "Feedback status changed, Message: ";

  /**
   * Gets the data set id by dataflow id.
   *
   * @param idFlow the id flow
   *
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
   *
   * @return the dataset name
   */
  @Override
  public DataSetMetabaseVO findDatasetMetabase(Long idDataset) {
    Optional<DataSetMetabase> datasetMetabase = dataSetMetabaseRepository.findById(idDataset);
    DataSetMetabaseVO metabaseVO = new DataSetMetabaseVO();
    if (datasetMetabase.isPresent()) {
      metabaseVO = dataSetMetabaseMapper.entityToClass(datasetMetabase.get());
      metabaseVO.setDatasetTypeEnum(getDatasetType(idDataset));

    }
    return metabaseVO;
  }

  /**
   * Delete design dataset.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  @Caching(evict = {@CacheEvict(value = "dataFlowId", key = "#datasetId"),
      @CacheEvict(value = "datasetSchemaByDatasetId", key = "#datasetId")})
  public void deleteDesignDataset(Long datasetId) {
    dataSetMetabaseRepository.deleteNativeDataset(datasetId);
  }

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   *
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
   * Update dataset status.
   *
   * @param datasetStatusMessageVO the dataset status message VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateDatasetStatus(DatasetStatusMessageVO datasetStatusMessageVO)
      throws EEAException {
    DataSetMetabase datasetMetabase =
        dataSetMetabaseRepository.findById(datasetStatusMessageVO.getDatasetId()).orElse(null);
    if (datasetMetabase != null) {
      datasetMetabase.setStatus(datasetStatusMessageVO.getStatus());
      dataSetMetabaseRepository.save(datasetMetabase);
    } else {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    MessageVO message = new MessageVO();
    String messageStatus = "";
    if (DatasetStatusEnum.TECHNICALLY_ACCEPTED.equals(datasetStatusMessageVO.getStatus())) {
      messageStatus = STATUS_TECHNICALLY_ACCEPTED;
    } else if (DatasetStatusEnum.CORRECTION_REQUESTED.equals(datasetStatusMessageVO.getStatus())) {
      messageStatus = STATUS_CORRECTION_REQUESTED;
    } else {
      messageStatus = STATUS_CHANGED;
    }
    message.setContent(messageStatus + datasetStatusMessageVO.getMessage());
    message.setProviderId(datasetMetabase.getDataProviderId());
    message.setAutomatic(true);

    // Send message to provider
    Optional<DesignDataset> designDataset =
        designDatasetRepository.findFirstByDatasetSchema(datasetMetabase.getDatasetSchema());
    collaborationControllerZuul.createMessage(datasetStatusMessageVO.getDataflowId(), message);
    collaborationControllerZuul.notifyNewMessages(datasetStatusMessageVO.getDataflowId(),
        datasetMetabase.getDataProviderId(), datasetMetabase.getId(), datasetMetabase.getStatus(),
        designDataset.isPresent() ? designDataset.get().getDataSetName() : null,
        EventType.UPDATED_DATASET_STATUS.toString());
    LOG.info("Automatic feedback message created of dataflow {}. Message: {}",
        datasetStatusMessageVO.getDataflowId(), message.getContent());
  }


  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics
   *
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  public StatisticsVO getStatistics(final Long datasetId)
      throws InstantiationException, IllegalAccessException {

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
   *
   * @return the statistics VO
   *
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
    statisticsDataset.stream()
        .forEach(s -> setEntityProperty(instance, s.getStatName(), s.getValue()));
    stats = (StatisticsVO) instance;

    // Table statistics
    stats.setTables(new ArrayList<>());
    for (List<Statistics> listStats : tablesMap.values()) {
      Class<?> clazzTable = TableStatisticsVO.class;
      Object instanceTable = clazzTable.newInstance();
      listStats.stream()
          .forEach(s -> setEntityProperty(instanceTable, s.getStatName(), s.getValue()));
      stats.getTables().add((TableStatisticsVO) instanceTable);
    }

    return stats;


  }


  /**
   * Gets the global statistics.
   *
   * @param dataschemaId the dataschema id
   *
   * @return the global statistics
   *
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  public List<StatisticsVO> getGlobalStatistics(String dataschemaId)
      throws EEAException, InstantiationException, IllegalAccessException {

    List<StatisticsVO> statistics = new ArrayList<>();

    List<Statistics> stats = statisticsRepository.findStatisticsByIdDatasetSchema(dataschemaId);

    Map<DataSetMetabase, List<Statistics>> statsMap =
        stats.stream().collect(Collectors.groupingBy(Statistics::getDataset, Collectors.toList()));

    Map<DataSetMetabase, List<Statistics>> statsMapsFilteredByReportings = statsMap.entrySet()
        .stream().filter(dsMetabase -> dsMetabase.getKey() instanceof ReportingDataset)
        .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

    statsMapsFilteredByReportings.values().stream().forEach(s -> {
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
   * @param datasetIdsEmail the dataset ids email
   * @param dataflowId the dataflow id
   */
  @Override
  public void createGroupProviderAndAddUser(Map<Long, String> datasetIdsEmail, Long dataflowId) {

    List<ResourceInfoVO> groups = new ArrayList<>();
    Set<Long> datasetIds = datasetIdsEmail.keySet();
    for (Long datasetId : datasetIds) {
      groups.add(createGroup(datasetId, ResourceTypeEnum.DATASET, SecurityRoleEnum.LEAD_REPORTER));
      groups.add(createGroup(datasetId, ResourceTypeEnum.DATASET, SecurityRoleEnum.DATA_CUSTODIAN));
    }
    resourceManagementControllerZuul.createResources(groups);
    List<ResourceAssignationVO> resourcesProviders = new ArrayList<>();
    List<ResourceAssignationVO> resourcesCustodian = new ArrayList<>();

    datasetIdsEmail.forEach((Long id, String email) -> {

      ResourceAssignationVO resourceDP =
          fillResourceAssignation(id, email, ResourceGroupEnum.DATASET_LEAD_REPORTER);
      resourcesProviders.add(resourceDP);

      ResourceAssignationVO resourceDC =
          fillResourceAssignation(id, email, ResourceGroupEnum.DATASET_CUSTODIAN);
      resourcesCustodian.add(resourceDC);

    });

    userManagementControllerZuul.addContributorsToResources(resourcesProviders);
    userManagementControllerZuul.addUserToResources(resourcesCustodian);
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
   *
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
   * Creates the schema group and add user.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createSchemaGroup(Long datasetId) {

    // Create group Dataschema-X-DATA_CUSTODIAN
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_CUSTODIAN));

    // Create group Dataschema-X-LEAD_REPORTER
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.LEAD_REPORTER));

    // Create group Dataschema-X-REPORTER_READ
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.REPORTER_READ));

    // Create group Dataschema-X-EDITOR_READ
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.EDITOR_READ));

    // Create group Dataschema-X-EDITOR_WRITE
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.EDITOR_WRITE));

    // Create group Dataschema-X-DATA_STEWARD
    resourceManagementControllerZuul.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_STEWARD));

  }


  /**
   * Creates the empty dataset.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param datasetSchemaId the dataset schema id
   * @param dataflowId the dataflow id
   * @param dueDate the due date
   * @param representatives the representatives
   * @param iterationDC the iteration DC
   *
   * @return the future
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  @org.springframework.transaction.annotation.Transactional(
      value = "metabaseDataSetsTransactionManager")
  public Future<Long> createEmptyDataset(DatasetTypeEnum datasetType, String datasetName,
      String datasetSchemaId, Long dataflowId, Date dueDate, List<RepresentativeVO> representatives,
      Integer iterationDC) throws EEAException {

    if (datasetType != null && dataflowId != null) {
      try {
        DataSetMetabase dataset;
        Map<Long, String> datasetIdsEmail = new HashMap<>();
        Long idDesignDataset = 0L;
        switch (datasetType) {
          case REPORTING:
            for (RepresentativeVO representative : representatives) {
              datasetIdsEmail
                  .putAll(fillAndSaveReportingDataset(representative, dataflowId, datasetSchemaId));
            }
            this.createGroupProviderAndAddUser(datasetIdsEmail, dataflowId);
            if (iterationDC == 0) {
              // Notification
              kafkaSenderUtils.releaseNotificableKafkaEvent(
                  EventType.ADD_DATACOLLECTION_COMPLETED_EVENT, null,
                  NotificationVO.builder()
                      .user(SecurityContextHolder.getContext().getAuthentication().getName())
                      .dataflowId(dataflowId).build());

            }
            break;
          case DESIGN:
            dataset = new DesignDataset();
            fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
            designDatasetRepository.save((DesignDataset) dataset);
            recordStoreControllerZuul.createEmptyDataset(
                LiteralConstants.DATASET_PREFIX + dataset.getId(), datasetSchemaId);
            this.createSchemaGroup(dataset.getId());
            idDesignDataset = dataset.getId();
            break;
          case COLLECTION:
            dataset = new DataCollection();
            fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
            ((DataCollection) dataset).setDueDate(dueDate);
            dataCollectionRepository.save((DataCollection) dataset);
            recordStoreControllerZuul.createEmptyDataset(
                LiteralConstants.DATASET_PREFIX + dataset.getId(), datasetSchemaId);
            LOG.info("New Data Collection created into the dataflow {}. DatasetId {} with name {}",
                dataflowId, dataset.getId(), datasetName);
            this.createGroupDcAndAddUser(dataset.getId());
            break;
          case TEST:
            dataset = new TestDataset();
            fillDataset(dataset, datasetName, dataflowId, datasetSchemaId);
            testDatasetRepository.save((TestDataset) dataset);
            recordStoreControllerZuul.createEmptyDataset(
                LiteralConstants.DATASET_PREFIX + dataset.getId(), datasetSchemaId);
            LOG.info("New Test Dataset created into the dataflow {}. DatasetId {} with name {}",
                dataflowId, dataset.getId(), datasetName);
            this.createGroupDcAndAddUser(dataset.getId());
            break;
          default:
            throw new EEAException("Unsupported datasetType: " + datasetType);
        }

        return new AsyncResult<>(idDesignDataset);

      } catch (EEAException e) {
        DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataflowId);
        EventType failEvent = EventType.ADD_DATACOLLECTION_FAILED_EVENT;
        if (null != dataflow && TypeDataflowEnum.REFERENCE.equals(dataflow.getType())) {
          failEvent = EventType.REFERENCE_DATAFLOW_PROCESS_FAILED_EVENT;
          LOG_ERROR.error("Error processing the reference dataflow {}. Error message: {}",
              dataflowId, e.getMessage(), e);
        } else {
          LOG_ERROR.error("Error creating a new empty data collection. Error message: {}",
              e.getMessage(), e);
        }

        // Error notification
        kafkaSenderUtils.releaseNotificableKafkaEvent(failEvent, null,
            NotificationVO.builder()
                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                .dataflowId(dataflowId).error(e.getMessage()).build());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            EEAErrorMessage.EXECUTION_ERROR);
      }
    }
    throw new EEAException("createEmptyDataset: Bad arguments");

  }


  /**
   * Fill resource assignation.
   *
   * @param id the id
   * @param email the email
   * @param group the group
   *
   * @return the resource assignation VO
   */
  private ResourceAssignationVO fillResourceAssignation(Long id, String email,
      ResourceGroupEnum group) {

    ResourceAssignationVO resource = new ResourceAssignationVO();
    resource.setResourceId(id);
    resource.setEmail(email);
    resource.setResourceGroup(group);

    return resource;
  }


  /**
   * Fill and save reporting dataset.
   *
   * @param representative the representative
   * @param dataflowId the dataflow id
   * @param datasetSchemaId the dataset schema id
   *
   * @return the map
   */
  private Map<Long, String> fillAndSaveReportingDataset(RepresentativeVO representative,
      Long dataflowId, String datasetSchemaId) {

    ReportingDataset dataset = new ReportingDataset();
    Map<Long, String> datasetIdsEmail = new HashMap<>();
    DataProviderVO provider =
        representativeControllerZuul.findDataProviderById(representative.getDataProviderId());

    fillDataset(dataset, provider.getLabel(), dataflowId, datasetSchemaId);
    dataset.setDataProviderId(representative.getDataProviderId());
    Long idDataset = reportingDatasetRepository.save(dataset).getId();
    for (String email : representative.getLeadReporters().stream().map(LeadReporterVO::getEmail)
        .collect(Collectors.toList())) {
      datasetIdsEmail.put(idDataset, email);
    }
    recordStoreControllerZuul.createEmptyDataset(LiteralConstants.DATASET_PREFIX + idDataset,
        datasetSchemaId);
    LOG.info("New Reporting Dataset into the dataflow {}. DatasetId {} with name {}", dataflowId,
        idDataset, provider.getLabel());

    return datasetIdsEmail;
  }

  /**
   * Find dataset schema id by id.
   *
   * @param datasetId the dataset id
   *
   * @return the string
   */
  @Override
  @CheckForNull
  @Cacheable(value = "datasetSchemaByDatasetId", key = "#datasetId")
  public String findDatasetSchemaIdById(long datasetId) {
    return dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
  }


  /**
   * Adds the foreign relation into the metabase.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetIdDestination the dataset id destination
   * @param idPk the id pk
   * @param idFkOrigin the id fk origin
   */
  @Override
  public void addForeignRelation(Long datasetIdOrigin, Long datasetIdDestination, String idPk,
      String idFkOrigin) {
    ForeignRelations foreign = new ForeignRelations();
    DataSetMetabase dsOrigin = new DataSetMetabase();
    DataSetMetabase dsDestination = new DataSetMetabase();
    dsOrigin.setId(datasetIdOrigin);
    dsDestination.setId(datasetIdDestination);
    foreign.setIdDatasetOrigin(dsOrigin);
    foreign.setIdDatasetDestination(dsDestination);
    foreign.setIdPk(idPk);
    foreign.setIdFkOrigin(idFkOrigin);

    foreignRelationsRepository.save(foreign);
  }


  /**
   * Delete foreign relation from the metabase.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetIdDestination the dataset id destination
   * @param idPk the id pk
   * @param idFkOrigin the id fk origin
   */
  @Override
  public void deleteForeignRelation(Long datasetIdOrigin, Long datasetIdDestination, String idPk,
      String idFkOrigin) {
    foreignRelationsRepository.deleteFKByOriginDestinationAndPkAndIdFkOrigin(datasetIdOrigin,
        datasetIdDestination, idPk, idFkOrigin);
  }


  /**
   * Gets the dataset destination foreign relation. It's used to know the datasetId destination of a
   * FK
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idPk the id pk
   *
   * @return the dataset destination foreign relation
   */
  @Override
  public Long getDatasetDestinationForeignRelation(Long datasetIdOrigin, String idPk) {
    Long idDestination = 0L;
    List<Long> datasetsId =
        foreignRelationsRepository.findDatasetDestinationByOriginAndPk(datasetIdOrigin, idPk);
    if (datasetsId != null && !datasetsId.isEmpty()) {
      idDestination = datasetsId.get(0);
    }
    return idDestination;
  }

  /**
   * Gets the dataset type.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset type
   */

  @Override
  @Cacheable(value = "datasetType", key = "#datasetId")
  public DatasetTypeEnum getDatasetType(Long datasetId) {
    DatasetTypeEnum type = null;

    if (designDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.DESIGN;
    } else if (reportingDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REPORTING;
    } else if (dataCollectionRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.COLLECTION;
    } else if (euDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.EUDATASET;
    } else if (testDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.TEST;
    } else if (referenceDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REFERENCE;
    }

    return type;
  }


  /**
   * Gets the integrity dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetOriginSchemaId the dataset origin schema id
   * @param datasetReferencedSchemaId the dataset referenced schema id
   * @return the integrity dataset id
   */
  @Override
  public Long getIntegrityDatasetId(Long datasetIdOrigin, String datasetOriginSchemaId,
      String datasetReferencedSchemaId) {
    ForeignRelations foreignRelation =
        foreignRelationsRepository.findFirstByIdDatasetOrigin_idAndIdPkAndIdFkOrigin(
            datasetIdOrigin, datasetOriginSchemaId, datasetReferencedSchemaId).orElse(null);

    return foreignRelation != null ? foreignRelation.getIdDatasetDestination().getId() : null;

  }


  /**
   * Creates the foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @Override
  public void createForeignRelationship(long datasetOriginId, long datasetReferencedId,
      String originDatasetSchemaId, String referencedDatasetSchemaId) {
    ForeignRelations foreignRelations = new ForeignRelations();
    DataSetMetabase dataSetReferencedMetabase = new DataSetMetabase();
    dataSetReferencedMetabase.setId(datasetReferencedId);
    foreignRelations.setIdDatasetDestination(dataSetReferencedMetabase);
    DataSetMetabase dataSetOriginMetabase = new DataSetMetabase();
    dataSetOriginMetabase.setId(datasetOriginId);
    foreignRelations.setIdDatasetOrigin(dataSetOriginMetabase);
    foreignRelations.setIdPk(originDatasetSchemaId);
    foreignRelations.setIdFkOrigin(referencedDatasetSchemaId);
    foreignRelationsRepository.save(foreignRelations);
    LOG.info(
        "New create ForeignRelationship created for the combination of : datasetOriginId {} "
            + ", datasetReferencedId {} , originDatasetSchemaId {} , referencedDatasetSchemaId {}",
        datasetOriginId, datasetReferencedId, originDatasetSchemaId, referencedDatasetSchemaId);

  }


  /**
   * Update foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @Override
  public void updateForeignRelationship(long datasetOriginId, long datasetReferencedId,
      String originDatasetSchemaId, String referencedDatasetSchemaId) {
    Optional<ForeignRelations> foreignRelations = foreignRelationsRepository
        .findFirstByIdDatasetOrigin_idAndIdDatasetDestination_idAndIdPkAndIdFkOrigin(
            datasetOriginId, datasetReferencedId, originDatasetSchemaId, referencedDatasetSchemaId);
    if (foreignRelations.isPresent()) {
      foreignRelationsRepository.delete(foreignRelations.get());
    }
    createForeignRelationship(datasetOriginId, datasetReferencedId, originDatasetSchemaId,
        referencedDatasetSchemaId);

    LOG.info(
        "New update ForeignRelationship created for the combination of : datasetOriginId {} "
            + ", datasetReferencedId {} , originDatasetSchemaId {} , referencedDatasetSchemaId {}",
        datasetOriginId, datasetReferencedId, originDatasetSchemaId, referencedDatasetSchemaId);
  }


  /**
   * Gets the dataset id by dataset schema id and data provider id.
   *
   * @param referencedDatasetSchemaId the referenced dataset schema id
   * @param dataProviderId the data provider id
   * @return the dataset id by dataset schema id and data provider id
   */
  @Override
  public Long getDatasetIdByDatasetSchemaIdAndDataProviderId(String referencedDatasetSchemaId,
      Long dataProviderId) {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository
        .findFirstByDatasetSchemaAndDataProviderId(referencedDatasetSchemaId, dataProviderId)
        .orElse(null);
    return datasetMetabase != null ? datasetMetabase.getId() : null;
  }


  /**
   * Count dataset name by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   * @return the long
   */
  @Override
  public Long countDatasetNameByDataflowId(Long dataflowId, String datasetSchemaName) {
    return dataSetMetabaseRepository.countByDataSetNameIgnoreCaseAndDataflowId(datasetSchemaName,
        dataflowId);
  }

  /**
   * Gets the dataset ids by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the dataset ids by dataflow id and data provider id
   */
  @Override
  public List<Long> getDatasetIdsByDataflowIdAndDataProviderId(Long dataflowId,
      Long dataProviderId) {
    return dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(dataflowId,
        dataProviderId);
  }

  /**
   * Gets the user provider ids by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the user provider ids by dataflow id
   */
  @Override
  public List<Long> getUserProviderIdsByDataflowId(Long dataflowId) {

    List<Long> providerIds = new ArrayList<>();
    Collection<? extends GrantedAuthority> authorities =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities();

    for (DataSetMetabase dataset : dataSetMetabaseRepository
        .findByDataflowIdAndProviderIdNotNull(dataflowId)) {
      if (authorities
          .contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATASET_LEAD_REPORTER.getAccessRole(dataset.getId())))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATASET_REPORTER_READ.getAccessRole(dataset.getId())))
          || authorities.contains(new SimpleGrantedAuthority(
              ObjectAccessRoleEnum.DATASET_REPORTER_WRITE.getAccessRole(dataset.getId())))) {
        providerIds.add(dataset.getDataProviderId());
      }
    }

    return providerIds;
  }



  /**
   * Gets the last dataset validation for release.
   *
   * @param datasetId the dataset id
   * @return the last dataset validation for release
   */
  @Override
  public Long getLastDatasetValidationForRelease(Long datasetId) {
    DataSetMetabase dataset =
        dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());
    List<Long> datasets = dataSetMetabaseRepository.getDatasetIdsByDataflowIdAndDataProviderId(
        dataset.getDataflowId(), dataset.getDataProviderId());
    Collections.sort(datasets);
    Long nextIdValidation = null;
    if (!datasets.get(datasets.size() - 1).equals(datasetId)) {
      int index = datasets.indexOf(datasetId);
      nextIdValidation = datasets.get(++index);
    }
    return nextIdValidation;

  }


  /**
   * Gets the design datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the design datasets summary list
   */
  private List<DatasetsSummaryVO> getDesignDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<DesignDataset> designDatasets = designDatasetRepository.findByDataflowId(dataflowId);
    for (DesignDataset designDataset : designDatasets) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(designDataset.getId());
      datasetsSummary.setDataSetName(designDataset.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.DESIGN);
      datasetsSummaryList.add(datasetsSummary);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the reference datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the reference datasets summary list
   */
  private List<DatasetsSummaryVO> getReferenceDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<ReferenceDataset> referenceDatasets =
        referenceDatasetRepository.findByDataflowId(dataflowId);
    for (ReferenceDataset referenceDataset : referenceDatasets) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(referenceDataset.getId());
      datasetsSummary.setDataSetName(referenceDataset.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);
      datasetsSummaryList.add(datasetsSummary);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the test datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the test datasets summary list
   */
  private List<DatasetsSummaryVO> getTestDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<TestDataset> testDatasets = testDatasetRepository.findByDataflowId(dataflowId);
    for (TestDataset testDataset : testDatasets) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(testDataset.getId());
      datasetsSummary.setDataSetName(testDataset.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.TEST);
      datasetsSummaryList.add(datasetsSummary);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the data collections summary list.
   *
   * @param dataflowId the dataflow id
   * @return the data collections summary list
   */
  private List<DatasetsSummaryVO> getDataCollectionsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<DataCollection> dataCollections = dataCollectionRepository.findByDataflowId(dataflowId);
    for (DataCollection dataCollection : dataCollections) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(dataCollection.getId());
      datasetsSummary.setDataSetName(dataCollection.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);
      datasetsSummaryList.add(datasetsSummary);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the EU datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the EU datasets summary list
   */
  private List<DatasetsSummaryVO> getEUDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<EUDataset> eudatasets = euDatasetRepository.findByDataflowId(dataflowId);
    for (EUDataset euDataset : eudatasets) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(euDataset.getId());
      datasetsSummary.setDataSetName(euDataset.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
      datasetsSummaryList.add(datasetsSummary);
    }
    return datasetsSummaryList;
  }

  /**
   * Gets the reporting datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the reporting datasets summary list
   */
  private List<DatasetsSummaryVO> getReportingDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    List<ReportingDataset> reportingDatasets =
        reportingDatasetRepository.findByDataflowId(dataflowId);
    List<RepresentativeVO> representatives = representativeControllerZuul
        .findRepresentativesByDataFlowIdAndProviderIdList(dataflowId, reportingDatasets.stream()
            .map(ReportingDataset::getDataProviderId).collect(Collectors.toList()));
    for (ReportingDataset reportingDataset : reportingDatasets) {
      DatasetsSummaryVO datasetsSummary = new DatasetsSummaryVO();
      datasetsSummary.setId(reportingDataset.getId());
      datasetsSummary.setDataSetName(reportingDataset.getDataSetName());
      datasetsSummary.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
      datasetsSummaryList.add(datasetsSummary);
      for (RepresentativeVO representative : representatives) {
        DataProviderVO dataProvider =
            representativeControllerZuul.findDataProviderById(representative.getDataProviderId());
        datasetsSummary.setDataProviderCode(dataProvider.getCode());
        datasetsSummary.setDataProviderName(dataProvider.getLabel());
      }
    }

    return datasetsSummaryList;
  }

  /**
   * Gets the datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the datasets summary list
   */
  @Override
  public List<DatasetsSummaryVO> getDatasetsSummaryList(Long dataflowId) {
    List<DatasetsSummaryVO> datasetsSummaryList = new ArrayList<>();
    datasetsSummaryList.addAll(getDesignDatasetsSummaryList(dataflowId));
    datasetsSummaryList.addAll(getReferenceDatasetsSummaryList(dataflowId));
    datasetsSummaryList.addAll(getTestDatasetsSummaryList(dataflowId));
    datasetsSummaryList.addAll(getDataCollectionsSummaryList(dataflowId));
    datasetsSummaryList.addAll(getEUDatasetsSummaryList(dataflowId));
    datasetsSummaryList.addAll(getReportingDatasetsSummaryList(dataflowId));
    return datasetsSummaryList;
  }

}
