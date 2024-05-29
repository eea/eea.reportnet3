package org.eea.dataset.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.ReleaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.mapper.SnapshotSchemaMapper;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.*;
import org.eea.dataset.persistence.metabase.repository.*;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.pdf.ReceiptPDFGenerator;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.interfaces.vo.metabase.ReleaseReceiptVO;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.authorization.AdminUserAuthorization;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class DatasetSnapshotServiceImpl.
 */
@Service("datasetSnapshotService")
public class DatasetSnapshotServiceImpl implements DatasetSnapshotService {

  /**
   * The admin user.
   */
  @Value("${eea.keycloak.admin.user}")
  private String adminUser;

  /**
   * The admin pass.
   */
  @Value("${eea.keycloak.admin.password}")
  private String adminPass;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetSnapshotServiceImpl.class);

  /** The Constant FILE_PATTERN_NAME. */
  private static final String FILE_PATTERN_NAME = "schemaSnapshot_%s-DesignDataset_%s";

  /** The Constant FILE_PATTERN_NAME_RULES. */
  private static final String FILE_PATTERN_NAME_RULES = "rulesSnapshot_%s-DesignDataset_%s";

  /** The Constant FILE_PATTERN_NAME_UNIQUE. */
  private static final String FILE_PATTERN_NAME_UNIQUE = "uniqueSnapshot_%s-DesignDataset_%s";

  /** The Constant FILE_PATTERN_NAME_INTEGRITY. */
  private static final String FILE_PATTERN_NAME_INTEGRITY = "integritySnapshot_%s-DesignDataset_%s";

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The snapshot repository. */
  @Autowired
  private SnapshotRepository snapshotRepository;

  /** The snapshot mapper. */
  @Autowired
  private SnapshotMapper snapshotMapper;

  /** The snapshot schema repository. */
  @Autowired
  private SnapshotSchemaRepository snapshotSchemaRepository;

  /** The snapshot schema mapper. */
  @Autowired
  private SnapshotSchemaMapper snapshotSchemaMapper;

  /** The schema repository. */
  @Autowired
  private SchemasRepository schemaRepository;

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;


  /** The delete helper. */
  @Autowired
  private DeleteHelper deleteHelper;

  /** The schema service. */
  @Autowired
  private DatasetSchemaService schemaService;

  /** The metabase repository. */
  @Autowired
  private DataSetMetabaseRepository metabaseRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /** The receipt PDF generator. */
  @Autowired
  private ReceiptPDFGenerator receiptPDFGenerator;

  /** The unique constraint repository. */
  @Autowired
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The EU dataset repository. */
  @Autowired
  private EUDatasetRepository eUDatasetRepository;

  /** The release mapper. */
  @Autowired
  private ReleaseMapper releaseMapper;

  /** The reporting dataset service. */
  @Autowired
  private ReportingDatasetService reportingDatasetService;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The validation controller zuul. */
  @Autowired
  private ValidationControllerZuul validationControllerZuul;

  /** The job controller zuul */
  @Autowired
  private JobControllerZuul jobControllerZuul;

  @Autowired
  private ProcessControllerZuul processControllerZuul;

  @Autowired
  private AdminUserAuthorization adminUserAuthorization;

  @Autowired
  private RecordRepository recordRepository;

  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  /**
   * Gets the by id.
   *
   * @param idSnapshot the id snapshot
   * @return the by id
   * @throws EEAException the EEA exception
   */
  @Override
  public SnapshotVO getById(Long idSnapshot) throws EEAException {
    Snapshot snapshot = snapshotRepository.findById(idSnapshot).orElse(null);
    return snapshotMapper.entityToClass(snapshot);
  }

  /**
   * Gets the schema by id.
   *
   * @param idSnapshot the id snapshot
   * @return the schema by id
   * @throws EEAException the EEA exception
   */
  @Override
  public SnapshotVO getSchemaById(Long idSnapshot) throws EEAException {
    SnapshotSchema snapshot = snapshotSchemaRepository.findById(idSnapshot).orElse(null);
    return snapshotSchemaMapper.entityToClass(snapshot);
  }

  /**
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the snapshots by id dataset
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SnapshotVO> getSnapshotsByIdDataset(Long datasetId) throws EEAException {

    List<Snapshot> snapshots =
            snapshotRepository.findByReportingDatasetIdOrderByCreationDateDesc(datasetId);

    return snapshotMapper.entityListToClass(snapshots);
  }

  /**
   * Gets the snapshots enabled by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots enabled by id dataset
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SnapshotVO> getSnapshotsEnabledByIdDataset(Long datasetId) throws EEAException {

    List<Snapshot> snapshots =
            snapshotRepository.findByReportingDatasetIdAndEnabledTrueOrderByCreationDateDesc(datasetId);

    return snapshotMapper.entityListToClass(snapshots);
  }

  /**
   * Adds the snapshot.
   *
   * @param idDataset the id dataset
   * @param createSnapshotVO the create snapshot VO
   * @param partitionIdDestination the partition id destination
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   */
  @Override
  @Async
  public void addSnapshot(Long idDataset, CreateSnapshotVO createSnapshotVO,
                          Long partitionIdDestination, String dateRelease, boolean prefillingReference, String processId) {


    try {

      // 1. Create the snapshot in the metabase
      Snapshot snap = new Snapshot();

      //force date to UTC
      Instant utcInstant = Instant.now();
      Date dateReleaseUTC = new Date(Timestamp.from(utcInstant).getTime());
      snap.setCreationDate(dateReleaseUTC);
      snap.setDescription(createSnapshotVO.getDescription());
      DataSetMetabase dataset = new DataSetMetabase();
      dataset.setId(idDataset);
      if (DatasetTypeEnum.REPORTING
              .equals(datasetMetabaseService.findDatasetMetabase(idDataset).getDatasetTypeEnum())) {
        dataset = metabaseRepository.findById(idDataset).orElse(new DataSetMetabase());
        if (dataset.getDatasetSchema() != null) {
          DataCollection dataCollection = dataCollectionRepository
                  .findFirstByDatasetSchema(dataset.getDatasetSchema()).orElse(new DataCollection());
          snap.setDataCollectionId(dataCollection.getId());
        }
      }
      snap.setReportingDataset(dataset);
      snap.setDataSetName("snapshot from dataset_" + idDataset);
      if (Boolean.TRUE.equals(createSnapshotVO.getReleased())) {
        snap.setDcReleased(true);
      } else {
        snap.setDcReleased(false);
      }
      snap.setEnabled(true);
      snap.setEuReleased(false);

      Long dataflowId = metabaseRepository.findDataflowIdById(idDataset);
      if (snap.getReportingDataset() != null
              && snap.getReportingDataset().getDataProviderId() != null) {
        List<RepresentativeVO> representatives =
                representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);
        for (RepresentativeVO representative : representatives) {
          if (snap.getReportingDataset().getDataProviderId()
                  .equals(representative.getDataProviderId())) {
            snap.setRestrictFromPublic(representative.isRestrictFromPublic());
          }
        }
      }

      snap.setAutomatic(
              Boolean.TRUE.equals(createSnapshotVO.getAutomatic()) ? Boolean.TRUE : Boolean.FALSE);

      snapshotRepository.save(snap);
      LOG.info("Snapshot {} created into the metabase for datasetId {}, processId {}", snap.getId(), idDataset, processId);
      snap.getId();

      // 2. Create the data file of the snapshot, calling to recordstore-service
      // we need the partitionId. By now only consider the user root
      Long idPartition = obtainPartition(idDataset, "root").getId();

      // The partitionIdDestination will come with data only in the case of the EUDataset. We need
      // to put in that case
      // the partitionId of the destination, cause we try to make a snapshot from a DataCollection
      // to a EUDataset, so we need in
      // the snapshot files the partitionId of the EUDataset
      if (partitionIdDestination != null) {
        idPartition = partitionIdDestination;
      }

      recordStoreControllerZuul.createSnapshotData(idDataset, snap.getId(), idPartition,
              dateRelease, prefillingReference, processId);
      LOG.info("Successfully added snapshot for datasetId {}, processId {}", idDataset, processId);

    } catch (Exception e) {
      LOG.error("Error creating snapshot for datasetId {}, processId {}", idDataset, processId, e);
      Map<String, Object> value = new HashMap<>();
      ProcessVO processVO = null;
      if (processId!=null) {
        processVO = processControllerZuul.findById(processId);
        value.put(LiteralConstants.USER, processVO.getUser());
      }
      releaseEvent(EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT, idDataset, e.getMessage(), value);
      // Release the lock manually
      Map<String, Object> createSnapshot = new HashMap<>();
      createSnapshot.put(LiteralConstants.SIGNATURE, LockSignature.CREATE_SNAPSHOT.getValue());
      createSnapshot.put(LiteralConstants.DATASETID, idDataset);
      createSnapshot.put(LiteralConstants.RELEASED, createSnapshotVO.getReleased());
      lockService.removeLockByCriteria(createSnapshot);
    }
    // release snapshot when the user press create+release
  }

  /**
   * Release event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   * @param error the error
   */
  private void releaseEvent(EventType eventType, Long datasetId, String error, Map<String, Object> value) {
    try {
      String user = value!=null && value.get(LiteralConstants.USER)!=null ? (String) value.get(LiteralConstants.USER) : SecurityContextHolder.getContext().getAuthentication().getName();
      if (error == null) {
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value,
                NotificationVO.builder()
                        .user(user)
                        .datasetId(datasetId).build());
      } else {
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, value,
                NotificationVO.builder()
                        .user(user)
                        .datasetId(datasetId).error(error).build());
      }
    } catch (EEAException e) {
      LOG.error("Error releasing notification", e);
    }
  }

  /**
   * Removes the snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException {

    Snapshot snap = snapshotRepository.findById(idSnapshot).orElse(new Snapshot());
    if (snap.getAutomatic() != null && Boolean.TRUE.equals(snap.getAutomatic())) {
      LOG.error("Error deleting automatic snapshotId {}", idSnapshot);
      throw new EEAException(EEAErrorMessage.ERROR_DELETING_SNAPSHOT);
    }
    // Remove from the metabase
    snapshotRepository.deleteById(idSnapshot);
    // Delete the file
    recordStoreControllerZuul.deleteSnapshotData(idDataset, idSnapshot);

    LOG.info("Snapshot {} removed", idSnapshot);

  }

  /**
   * Restore snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param deleteData the delete data
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void restoreSnapshot(Long idDataset, Long idSnapshot, Boolean deleteData, String processId)
          throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZuul.restoreSnapshotData(idDataset, idSnapshot, idPartition,
            DatasetTypeEnum.REPORTING, false, deleteData, false, processId);
    LOG.info("Successfully restored snapshot with id {} for datasetId {} and processId {}", idSnapshot, idDataset, processId);
  }

  /**
   * Restore snapshot to clone data.
   *
   * @param datasetOrigin the dataset origin
   * @param idDatasetDestination the id dataset destination
   * @param idSnapshot the id snapshot
   * @param deleteData the delete data
   * @param datasetType the dataset type
   * @param prefillingReference the prefilling reference
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void restoreSnapshotToCloneData(Long datasetOrigin, Long idDatasetDestination,
                                         Long idSnapshot, Boolean deleteData, DatasetTypeEnum datasetType, boolean prefillingReference, String processId)
          throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(datasetOrigin, "root").getId();
    recordStoreControllerZuul.restoreSnapshotData(idDatasetDestination, idSnapshot, idPartition,
            datasetType, false, deleteData, prefillingReference, processId);
  }

  /**
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param dateRelease the date release
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void releaseSnapshot(Long idDataset, Long idSnapshot, String dateRelease, String processId)
          throws EEAException {

    Long providerId = 0L;
    DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(idDataset);
    if (metabase.getDataProviderId() != null) {
      providerId = metabase.getDataProviderId();
    }

    final Long idDataProvider = providerId;

    // Get the provider
    DataProviderVO provider = representativeControllerZuul.findDataProviderById(idDataProvider);

    // Get the dataCollection
    DataSetMetabase dataset = metabaseRepository.findById(idDataset).orElse(null);

    // Mark the released dataset with the status
    String datasetSchema = "";
    if (dataset != null) {
      DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
      datasetSchema = dataset.getDatasetSchema();
      dataset.setStatus(dataflow.isManualAcceptance() ? DatasetStatusEnum.FINAL_FEEDBACK
              : DatasetStatusEnum.RELEASED);
      metabaseRepository.save(dataset);
    }
    Optional<DataCollection> dataCollection =
            dataCollectionRepository.findFirstByDatasetSchema(datasetSchema);
    Long idDataCollection = dataCollection.isPresent() ? dataCollection.get().getId() : null;


    // Delete data of the same provider
    Date dateReleasing = null;
    if (StringUtils.isNotBlank(dateRelease)) {
      try {
        dateReleasing = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateRelease);
      } catch (ParseException e) {
        LOG.error("Error parsing the date of the release of snapshot with id {} for datasetId {} processId {}. Message: {}", idSnapshot, idDataset, processId, e.getMessage());
        throw new EEAException(
                "Error during the snapshot release. Problem parsing the date of the release with processId " + processId);
      }
    }
    deleteDataProvider(idDataset, idSnapshot, idDataProvider, provider, idDataCollection,
            dateReleasing, processId);

    ProcessVO processVO = null;
    if (processId!=null) {
      processVO = processControllerZuul.findById(processId);
    }
    String user = processVO!=null ? processVO.getUser() : SecurityContextHolder.getContext().getAuthentication().getName();

    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idDataset);
    value.put(LiteralConstants.USER, user);
    value.put("dateRelease", dateRelease);
    value.put("process_id", processId);
    LOG.info("The user releasing kafka event on DatasetSnapshotServiceImpl.releaseSnapshot for snapshotId {} and datasetId {} of processId {} is {}",
            idSnapshot, idDataset, processId, SecurityContextHolder.getContext().getAuthentication().getName());
    kafkaSenderUtils.releaseKafkaEvent(EventType.RELEASE_ONEBYONE_COMPLETED_EVENT, value);

    LOG.info("Successfully released snapshot with id {} for datasetId {} processId {} and dateRelease {}", idSnapshot, idDataset, processId, dateRelease);
  }

  /**
   * Delete data provider.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param idDataProvider the id data provider
   * @param provider the provider
   * @param idDataCollection the id data collection
   * @throws EEAException the EEA exception
   */
  private void deleteDataProvider(Long idDataset, Long idSnapshot, final Long idDataProvider,
                                  DataProviderVO provider, Long idDataCollection, Date dateRelease, String processId) throws EEAException {

    Map<String, Object> value = new HashMap<>();
    Boolean silentRelease = false;
    ProcessVO processVO = null;
    if (processId!=null) {
      processVO = processControllerZuul.findById(processId);
      value.put(LiteralConstants.USER, processVO.getUser());

      Long jobId = jobProcessControllerZuul.findJobIdByProcessId(processId);
      if(jobId != null){
        JobVO jobVO = jobControllerZuul.findJobById(jobId);
        if (jobVO != null) {
          Map<String, Object> parameters = jobVO.getParameters();
          if(parameters.containsKey("silentRelease")){
            silentRelease = (Boolean) parameters.get("silentRelease");
          }
        }
      }
    }

    Long idDataflow = datasetMetabaseService.findDatasetMetabase(idDataset).getDataflowId();
    if (provider != null && idDataCollection != null) {
      TenantResolver
              .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataCollection));
      deleteHelper.deleteRecordValuesByProvider(idDataCollection, provider.getCode(), processId);

      // Restore data from snapshot
      try {
        // Mark the receipt button as outdated because a new release has been done, so it would be
        // necessary to generate a new receipt
        List<RepresentativeVO> representatives = representativeControllerZuul
                .findRepresentativesByIdDataFlow(idDataflow).stream()
                .filter(r -> r.getDataProviderId().equals(idDataProvider)).collect(Collectors.toList());
        if (!representatives.isEmpty()) {
          RepresentativeVO representative = representatives.get(0);
          // We only update the representative if the receipt is not outdated
          if (Boolean.FALSE.equals(representative.getReceiptOutdated())) {
            representative.setReceiptOutdated(true);
            representativeControllerZuul.updateInternalRepresentative(representative);
            LOG.info(
                    "Receipt marked as outdated: dataflowId={}, datasetId={}, providerId={}, representativeId={}, processId={}",
                    idDataflow, idDataset, provider.getId(), representative.getId(), processId);
          }
        }

        // This method will release the lock and the notification
        restoreSnapshot(idDataCollection, idSnapshot, false, processId);

        if(!silentRelease) {
          // Mark the snapshot released
          snapshotRepository.releaseSnaphot(idDataset, idSnapshot);
          // Add the date of the release
          Optional<Snapshot> snapshot = snapshotRepository.findById(idSnapshot);
          if (snapshot.isPresent()) {
            // snapshot.get().setDateReleased(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            snapshot.get().setDateReleased(dateRelease);
            snapshotRepository.save(snapshot.get());
          }
        }

        LOG.info("Snapshot {} of processId {} released", idSnapshot, processId);
      } catch (EEAException e) {
        LOG.error("Error releasing snapshot {} of processId {},", idSnapshot, processId, e);
        if(!silentRelease) {
          releaseEvent(EventType.RELEASE_FAILED_EVENT, idSnapshot, e.getMessage(), value);
        }
        removeLockRelatedToCopyDataToEUDataset(idDataflow);
        releaseLocksRelatedToRelease(idDataflow, idDataProvider);
      }
    } else {
      LOG.error("Error in release snapshot {} of processId {}", idSnapshot, processId);
      if(!silentRelease) {
        releaseEvent(EventType.RELEASE_FAILED_EVENT, idSnapshot, "Error in release snapshot", value);
      }
      removeLockRelatedToCopyDataToEUDataset(idDataflow);
      releaseLocksRelatedToRelease(idDataflow, idDataProvider);
    }
  }

  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the schema snapshots by id dataset
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public List<SnapshotVO> getSchemaSnapshotsByIdDataset(Long datasetId) throws EEAException {

    List<SnapshotSchema> schemaSnapshots =
            snapshotSchemaRepository.findByDesignDatasetIdOrderByCreationDateDesc(datasetId);

    return snapshotSchemaMapper.entityListToClass(schemaSnapshots);

  }

  /**
   * Adds the schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idDatasetSchema the id dataset schema
   * @param description the description
   */
  @Override
  @Async
  public void addSchemaSnapshot(Long idDataset, String idDatasetSchema, String description) {
    try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
      // 1. Create the snapshot in the metabase
      SnapshotSchema snap = new SnapshotSchema();

      //force date to UTC
      Instant utcInstant = Instant.now();
      Date dateReleaseUTC = new Date(Timestamp.from(utcInstant).getTime());
      snap.setCreationDate(dateReleaseUTC);
      snap.setDescription(description);
      DesignDataset designDataset = new DesignDataset();
      designDataset.setId(idDataset);
      snap.setDesignDataset(designDataset);
      snap.setDataSetName("snapshot schema from design dataset_" + idDataset);
      snapshotSchemaRepository.save(snap);
      LOG.info(
              "Snapshot schema created into the metabase: datasetId={}, datasetSchemaId={}, snapshotId={}",
              idDataset, idDatasetSchema, snap.getId());

      // 2. Create the schema file from the document in Mongo
      DataSetSchema schema = schemaRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
      ObjectMapper objectMapper = new ObjectMapper();
      Long idSnapshot = snap.getId();
      String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      objectMapper.writeValue(outStream, schema);
      documentControllerZuul.uploadSchemaSnapshotDocument(outStream.toByteArray(), idDataset,
              nameFile);

      // Also, we need to create a rules file from the schema in Mongo
      RulesSchema rules = rulesRepository.findByIdDatasetSchema(new ObjectId(idDatasetSchema));
      ObjectMapper objectMapperRules = new ObjectMapper();
      String nameFileRules = String.format(FILE_PATTERN_NAME_RULES, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      outStream.reset();
      objectMapperRules.writeValue(outStream, rules);
      documentControllerZuul.uploadSchemaSnapshotDocument(outStream.toByteArray(), idDataset,
              nameFileRules);

      // We need to create a file related to the Unique catalogue
      List<UniqueConstraintSchema> listUnique =
              uniqueConstraintRepository.findByDatasetSchemaId(new ObjectId(idDatasetSchema));
      ObjectMapper objectMapperUnique = new ObjectMapper();
      String nameFileUnique = String.format(FILE_PATTERN_NAME_UNIQUE, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      outStream.reset();
      objectMapperUnique.writeValue(outStream, listUnique);
      documentControllerZuul.uploadSchemaSnapshotDocument(outStream.toByteArray(), idDataset,
              nameFileUnique);

      // We need to create a file related to the IntegritySchema
      List<IntegrityVO> listIntegrity =
              rulesControllerZuul.getIntegrityRulesByDatasetSchemaId(idDatasetSchema);
      ObjectMapper objectMapperIntegrity = new ObjectMapper();
      String nameFileIntegrity = String.format(FILE_PATTERN_NAME_INTEGRITY, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      outStream.reset();
      objectMapperIntegrity.writeValue(outStream, listIntegrity);
      documentControllerZuul.uploadSchemaSnapshotDocument(outStream.toByteArray(), idDataset,
              nameFileIntegrity);


      // 3. Create the data file of the snapshot, calling to recordstore-service
      // we need the partitionId. By now only consider the user root
      Long idPartition = obtainPartition(idDataset, "root").getId();
      recordStoreControllerZuul.createSnapshotData(idDataset, idSnapshot, idPartition, null, false, null);
      LOG.info("Successfully added schema snapshot with snapshotId {} and datasetId {}", idSnapshot, idDataset);
    } catch (Exception e) {
      LOG.error("Error creating schema snapshot for datasetId {}", idDataset, e);
      releaseEvent(EventType.ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT, idDataset, e.getMessage(), null);
      // Release the lock manually
      Map<String, Object> createSchemaSnapshot = new HashMap<>();
      createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
              LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      createSchemaSnapshot.put(LiteralConstants.DATASETID, idDataset);
      lockService.removeLockByCriteria(createSchemaSnapshot);
    }
  }

  /**
   * Restore schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void restoreSchemaSnapshot(Long idDataset, Long idSnapshot, String processId)
          throws EEAException, IOException {

    try {
      // Get the schema document to mapper it to DataSchema class
      String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset) + ".snap";
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] content = documentControllerZuul.getSnapshotDocument(idDataset, nameFile);
      DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
      LOG.info("Schema recovered: datasetId={}, snapshotId={}", idDataset, idSnapshot);

      // Get the rules document to mapper it to RulesSchema class
      String nameFileRules = String.format(FILE_PATTERN_NAME_RULES, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      ObjectMapper objectMapperRules = new ObjectMapper();
      objectMapperRules.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] contentRules = documentControllerZuul.getSnapshotDocument(idDataset, nameFileRules);
      RulesSchema rules = objectMapperRules.readValue(contentRules, RulesSchema.class);
      LOG.info("Rules recovered: datasetId={}, snapshotId={}", idDataset, idSnapshot);

      // Since there's the Unique property, we need to restore that file too
      // Get the unique document to mapper it into the List of UniqueConstraintSchema
      String nameFileUnique = String.format(FILE_PATTERN_NAME_UNIQUE, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      ObjectMapper objectMapperUnique = new ObjectMapper();
      objectMapperUnique.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] contentUnique = documentControllerZuul.getSnapshotDocument(idDataset, nameFileUnique);
      List<UniqueConstraintSchema> listUnique = objectMapperUnique.readValue(contentUnique,
              new TypeReference<List<UniqueConstraintSchema>>() {});
      LOG.info("Uniques recovered: datasetId={}, snapshotId={}", idDataset, idSnapshot);


      rulesControllerZuul.deleteRulesSchema(schema.getIdDataSetSchema().toString(), idDataset);
      rulesRepository.save(rules);

      uniqueConstraintRepository.deleteByDatasetSchemaId(schema.getIdDataSetSchema());
      uniqueConstraintRepository.saveAll(listUnique);


      // Since there's the Integrity rule, we need to restore that file too
      // Get the integrity document to mapper it into the List of IntegritySchema
      String nameFileIntegrity = String.format(FILE_PATTERN_NAME_INTEGRITY, idSnapshot, idDataset)
              + LiteralConstants.SNAPSHOT_EXTENSION;
      ObjectMapper objectMapperIntegrity = new ObjectMapper();
      objectMapperIntegrity.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] contentIntegrity =
              documentControllerZuul.getSnapshotDocument(idDataset, nameFileIntegrity);
      List<IntegrityVO> listIntegrityVO =
              objectMapperUnique.readValue(contentIntegrity, new TypeReference<List<IntegrityVO>>() {});
      if (listIntegrityVO != null && !listIntegrityVO.isEmpty()) {
        rulesControllerZuul.insertIntegritySchema(listIntegrityVO);
      }
      LOG.info("Integrities recovered: datasetId={}, snapshotId={}", idDataset, idSnapshot);


      // First we delete all the entries in the catalogue of the previous schema, before replacing
      // it
      // by the one of the snapshot
      schemaService.updatePkCatalogueDeletingSchema(schema.getIdDataSetSchema().toString(),
              idDataset);
      // Replace the schema: delete the older and save the new we have already recovered on step
      // Also in the service we call the recordstore to do the restore of the dataset_X data
      schemaService.replaceSchema(schema.getIdDataSetSchema().toString(), schema, idDataset,
              idSnapshot, processId);
      // fill the PK catalogue with the new schema
      // also the table foreign_relations
      schemaService.updatePKCatalogueAndForeignsAfterSnapshot(
              schema.getIdDataSetSchema().toString(), idDataset);

      // Redo the views of the dataset
      recordStoreControllerZuul.createUpdateQueryView(idDataset, false);

      LOG.info("Successfully restored snapshot with id {} for datasetId {}", idSnapshot, idDataset);
    } catch (EEAException | FeignException e) {
      LOG.error("Error restoring a schema snapshot: datasetId={}, snapshotId={}", idDataset,
              idSnapshot, e);
      Map<String, Object> value = new HashMap<>();
      ProcessVO processVO = null;
      if (processId!=null) {
        processVO = processControllerZuul.findById(processId);
        value.put(LiteralConstants.USER, processVO.getUser());
      }
      releaseEvent(EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT, idDataset,
              "Error restoring the schema snapshot", value);

      // Release the lock manually
      Map<String, Object> restoreSchemaSnapshot = new HashMap<>();
      restoreSchemaSnapshot.put(LiteralConstants.SIGNATURE,
              LockSignature.RESTORE_SCHEMA_SNAPSHOT.getValue());
      restoreSchemaSnapshot.put(LiteralConstants.DATASETID, idDataset);
      lockService.removeLockByCriteria(restoreSchemaSnapshot);
    }
  }

  /**
   * Removes the schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   *
   * @throws Exception the exception
   */
  @Override
  @Async
  public void removeSchemaSnapshot(Long idDataset, Long idSnapshot) throws Exception {
    // Remove from the metabase
    snapshotSchemaRepository.deleteSnapshotSchemaById(idSnapshot);
    metabaseRepository.deleteSnapshotDatasetByIdSnapshot(idSnapshot);
    LOG.info("Snapshot with id {} and datasetId {} was removed in metabase", idSnapshot, idDataset);
    // Delete the schema file
    String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset)
            + LiteralConstants.SNAPSHOT_EXTENSION;
    documentControllerZuul.deleteSnapshotSchemaDocument(idDataset, nameFile);

    LOG.info("Files for snapshot with id {} and datasetId {} were removed", idSnapshot, idDataset);

    // Delete the rules file
    String nameRulesFile = String.format(FILE_PATTERN_NAME_RULES, idSnapshot, idDataset)
            + LiteralConstants.SNAPSHOT_EXTENSION;
    documentControllerZuul.deleteSnapshotSchemaDocument(idDataset, nameRulesFile);

    LOG.info("Rules for snapshot with id {} and datasetId {} were removed", idSnapshot, idDataset);

    // Delete the file values
    recordStoreControllerZuul.deleteSnapshotData(idDataset, idSnapshot);
    LOG.info("Successfully removed schema snapshot with id {} for datasetId {} ", idSnapshot, idDataset);
  }

  /**
   * Delete all schema snapshots.
   *
   * @param idDesignDataset the id design dataset
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void deleteAllSchemaSnapshots(Long idDesignDataset) throws EEAException {

    LOG.info("Deleting all schema snapshots for datasetId {}", idDesignDataset);
    List<SnapshotVO> snapshots = getSchemaSnapshotsByIdDataset(idDesignDataset);
    snapshots.stream().forEach(s -> {
      try {
        removeSchemaSnapshot(idDesignDataset, s.getId());
      } catch (Exception e) {
        LOG.error("Error deleting the schema snapshot {} for datasetId {}", s.getId(), idDesignDataset, e);
      }
    });
    LOG.info("Deleted all schema snapshots for datasetId {}", idDesignDataset);
  }


  /**
   * Obtain partition.
   *
   * @param datasetId the dataset id
   * @param user the user
   *
   * @return the partition data set metabase
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
          throws EEAException {
    final PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
            .findFirstByIdDataSet_idAndUsername(datasetId, user).orElse(null);
    if (partition == null) {
      LOG.error(EEAErrorMessage.PARTITION_ID_NOTFOUND);
      throw new EEAException(EEAErrorMessage.PARTITION_ID_NOTFOUND);
    }
    return partition;
  }

  /**
   * Creates the receipt PDF.
   *
   * @param out the out
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  @Override
  public void createReceiptPDF(OutputStream out, Long dataflowId, Long dataProviderId) {

    ReleaseReceiptVO receipt = new ReleaseReceiptVO();
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId, null);
    receipt.setIdDataflow(dataflowId);
    receipt.setDataflowName(dataflow.getName());
    receipt.setObligationId(dataflow.getObligation().getObligationId());
    receipt.setObligationTitle(dataflow.getObligation().getOblTitle());
    receipt.setDatasets(dataflow.getReportingDatasets().stream()
            .filter(rd -> rd.getIsReleased() && rd.getDataProviderId().equals(dataProviderId))
            .collect(Collectors.toList()));

    if (!receipt.getDatasets().isEmpty()) {
      receipt.setProviderAssignation(receipt.getDatasets().get(0).getDataSetName());
    }

    List<RepresentativeVO> representatives =
            representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId).stream()
                    .filter(r -> r.getDataProviderId().equals(dataProviderId)).collect(Collectors.toList());

    UserRepresentationVO user = userManagementControllerZull.getUserByUserId();
    receipt.setEmail(user.getEmail());

    if (!representatives.isEmpty()) {
      RepresentativeVO representative = representatives.get(0);

      receipt.setProviderEmail(user.getEmail());

      // Check if it's needed to update the status of the button (i.e I only want to download the
      // receipt twice, but no state is changed)
      if (Boolean.FALSE.equals(representative.getReceiptDownloaded())
              || Boolean.TRUE.equals(representative.getReceiptOutdated())) {
        // update provider. Button downloaded = true && outdated = false
        representative.setReceiptDownloaded(true);
        representative.setReceiptOutdated(false);
        representativeControllerZuul.updateInternalRepresentative(representative);
        LOG.info("Receipt from the representative {} and dataflowId {} marked as downloaded", representative.getId(), dataflowId);
      }
    }

    receiptPDFGenerator.generatePDF(receipt, out);
  }

  /**
   * Removes the lock related to copy data to EU dataset.
   *
   * @param dataflowId the dataflow id
   */
  private void removeLockRelatedToCopyDataToEUDataset(Long dataflowId) {
    Map<String, Object> populateEuDataset = new HashMap<>();
    populateEuDataset.put(LiteralConstants.SIGNATURE, LockSignature.POPULATE_EU_DATASET.getValue());
    populateEuDataset.put(LiteralConstants.DATAFLOWID, dataflowId);
    lockService.removeLockByCriteria(populateEuDataset);
  }

  /**
   * Gets the snapshots released by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots released by id dataset
   */
  @Override
  public List<ReleaseVO> getSnapshotsReleasedByIdDataset(Long datasetId) {
    List<Snapshot> snapshots =
            snapshotRepository.findByReportingDatasetIdOrderByCreationDateDesc(datasetId);
    return releaseMapper.entityListToClass(snapshots.stream()
            .filter(snapshot -> snapshot.getDateReleased() != null).collect(Collectors.toList()));
  }

  /**
   * Gets the snapshots released by id data collection.
   *
   * @param dataCollectionId the data collection id
   * @return the snapshots released by id data collection
   */
  @Override
  public List<ReleaseVO> getSnapshotsReleasedByIdDataCollection(Long dataCollectionId) {
    List<Snapshot> snapshots =
            snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(dataCollectionId);
    return releaseMapper.entityListToClass(snapshots.stream()
            .filter(snapshot -> snapshot.getDateReleased() != null).collect(Collectors.toList()));
  }

  /**
   * Gets the snapshots released by id EU dataset.
   *
   * @param euDatasetId the eu dataset id
   * @return the snapshots released by id EU dataset
   * @throws EEAException the EEA exception
   */
  @Override
  public List<ReleaseVO> getSnapshotsReleasedByIdEUDataset(Long euDatasetId) throws EEAException {
    // find datacollectionid
    EUDataset eudataset = eUDatasetRepository.findById(euDatasetId).orElse(null);
    if (eudataset == null) {
      LOG.error(EEAErrorMessage.DATASET_NOTFOUND);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    DataCollection dataCollection = dataCollectionRepository
            .findFirstByDatasetSchema(eudataset.getDatasetSchema()).orElse(null);
    if (dataCollection == null) {
      LOG.error(EEAErrorMessage.DATASET_NOTFOUND);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    // find snapshots for the datacollection released in the eudataset
    List<Snapshot> snapshots =
            snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(dataCollection.getId());
    return releaseMapper
            .entityListToClass(snapshots.stream().filter(snapshot -> snapshot.getDateReleased() != null
                    && Boolean.TRUE.equals(snapshot.getEuReleased())).collect(Collectors.toList()));
  }

  /**
   * Update snapshot EU release.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void updateSnapshotEURelease(Long datasetId) {
    // We have to set for the active snapshots in this moment in the dataset, the field eu_released
    // to true, and the rest to false
    List<Snapshot> snapshots =
            snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(datasetId);
    List<Long> activeSnapshots = snapshots.stream().filter(Snapshot::getDcReleased)
            .map(Snapshot::getId).collect(Collectors.toList());
    List<Long> inactiveSnapshots = snapshots.stream().filter(snapshot -> !snapshot.getDcReleased())
            .map(Snapshot::getId).collect(Collectors.toList());
    if (!inactiveSnapshots.isEmpty()) {
      snapshotRepository.releaseEUInactiveSnapshots(inactiveSnapshots);
    }
    if (!activeSnapshots.isEmpty()) {
      snapshotRepository.releaseEUActiveSnapshots(activeSnapshots);
    }
  }

  /**
   * Gets the historic releases per each dataset type.
   *
   * @param datasetId the dataset id
   * @return the releases
   * @throws EEAException the EEA exception
   */
  @Override
  public List<ReleaseVO> getReleases(Long datasetId) throws EEAException {
    List<ReleaseVO> releases = new ArrayList<>();
    DatasetTypeEnum datasetType = datasetMetabaseService.findDatasetMetabase(datasetId).getDatasetTypeEnum();
    if (DatasetTypeEnum.REPORTING
            .equals(datasetType)) {
      // if dataset is reporting return released snapshots
      releases = getSnapshotsReleasedByIdDataset(datasetId);
    } else {
      // if the snapshot is a datacollection
      if (DatasetTypeEnum.COLLECTION
              .equals(datasetType)) {
        releases = getSnapshotsReleasedByIdDataCollection(datasetId);
      } else
        // if the snapshot is an eudataset
        if (DatasetTypeEnum.EUDATASET
                .equals(datasetType)) {
          releases = getSnapshotsReleasedByIdEUDataset(datasetId);
        }
    }
    return releases;
  }


  /**
   * Creates the release snapshots.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void createReleaseSnapshots(Long dataflowId, Long dataProviderId,
                                     boolean restrictFromPublic, boolean validate, Long jobId) throws EEAException {
    try {
      LOG.info("Releasing datasets process begins. DataflowId: {} DataProviderId: {} JobId {}", dataflowId,
              dataProviderId, jobId);
      // First dataset involved in the process
      ReportingDataset dataset = reportingDatasetRepository
              .findFirstByDataflowIdAndDataProviderIdOrderByIdAsc(dataflowId, dataProviderId);
      // List of the datasets involved
      List<Long> datasetsFilters = reportingDatasetRepository.findByDataflowId(dataflowId).stream()
              .filter(rd -> rd.getDataProviderId().equals(dataProviderId)).map(ReportingDataset::getId)
              .distinct().collect(Collectors.toList());

      // Lock all the operations related to the datasets involved
      addLocksRelatedToRelease(datasetsFilters, dataflowId);

      // Update representative visibility
      representativeControllerZuul.updateRepresentativeVisibilityRestrictions(dataflowId,
              dataProviderId, restrictFromPublic);

      JobVO valJobVo = null;
      if (jobId!=null) {
        valJobVo = jobControllerZuul.findJobById(jobId);
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        adminUserAuthorization.setAdminSecurityContextAuthenticationWithJobUserRoles(tokenVo, valJobVo);
      }
      // if the user is admin can release without validations
      if (!isAdmin() || validate) {
        validationControllerZuul.validateDataSetData(dataset.getId(), true, jobId);
      } else {
        if (jobId!=null) {
          jobControllerZuul.updateJobStatus(jobId, JobStatusEnum.FINISHED);
        }
        String processId = UUID.randomUUID().toString();
        String notificationUser = valJobVo!=null ? valJobVo.getCreatorUsername() : SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> value = new HashMap<>();
        value.put(LiteralConstants.DATASET_ID, dataset.getId());
        value.put("uuid", processId);
        value.put("user", notificationUser);
        value.put("validation_job_id", jobId);
        kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATION_RELEASE_FINISHED_EVENT, value);
      }
      LOG.info("Successfully created release snapshots for dataflowId {} and dataProviderId {} with jobId {}", dataflowId, dataProviderId, jobId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating release snapshots for dataflowId {} and dataProviderId {} with jobId {} Message: {}", dataflowId, dataProviderId, jobId, e.getMessage());
      throw e;
    }
  }


  /**
   * Release locks related to release.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @throws EEAException the EEA exception
   */
  @Override
  public void releaseLocksRelatedToRelease(Long dataflowId, Long dataProviderId)
          throws EEAException {
    try {
      List<Long> datasets = reportingDatasetRepository.findByDataflowId(dataflowId).stream()
              .filter(rd -> rd.getDataProviderId().equals(dataProviderId)).map(ReportingDataset::getId)
              .collect(Collectors.toList());

      // We have to lock all the dataset operations (insert, delete, update...)
      for (Long datasetId : datasets) {

        Map<String, Object> insertRecords = new HashMap<>();
        insertRecords.put(LiteralConstants.SIGNATURE, LockSignature.INSERT_RECORDS.getValue());
        insertRecords.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> deleteRecords = new HashMap<>();
        deleteRecords.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_RECORDS.getValue());
        deleteRecords.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> updateField = new HashMap<>();
        updateField.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_FIELD.getValue());
        updateField.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> updateRecords = new HashMap<>();
        updateRecords.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_RECORDS.getValue());
        updateRecords.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> deleteDatasetValues = new HashMap<>();
        deleteDatasetValues.put(LiteralConstants.SIGNATURE,
                LockSignature.DELETE_DATASET_VALUES.getValue());
        deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> importFileData = new HashMap<>();
        importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
        importFileData.put(LiteralConstants.DATASETID, datasetId);
        Map<String, Object> importBigFileData = new HashMap<>();
        importBigFileData.put(LiteralConstants.SIGNATURE,
                LockSignature.IMPORT_BIG_FILE_DATA.getValue());
        importBigFileData.put(LiteralConstants.DATASETID, datasetId);

        Map<String, Object> restoreSnapshots = new HashMap<>();
        restoreSnapshots.put(LiteralConstants.SIGNATURE, LockSignature.RESTORE_SNAPSHOT.getValue());
        restoreSnapshots.put(LiteralConstants.DATASETID, datasetId);


        Map<String, Object> insertRecordsMultitable = new HashMap<>();
        insertRecordsMultitable.put(LiteralConstants.SIGNATURE,
                LockSignature.INSERT_RECORDS_MULTITABLE.getValue());
        insertRecordsMultitable.put(LiteralConstants.DATASETID, datasetId);

        lockService.removeLockByCriteria(insertRecords);
        lockService.removeLockByCriteria(deleteRecords);
        lockService.removeLockByCriteria(updateField);
        lockService.removeLockByCriteria(updateRecords);
        lockService.removeLockByCriteria(deleteDatasetValues);
        lockService.removeLockByCriteria(importFileData);
        lockService.removeLockByCriteria(importBigFileData);
        lockService.removeLockByCriteria(insertRecordsMultitable);
        lockService.removeLockByCriteria(restoreSnapshots);

        // Delete tables and import tables
        DataSetSchemaVO schema = schemaService.getDataSchemaByDatasetId(false, datasetId);
        for (TableSchemaVO table : schema.getTableSchemas()) {
          Map<String, Object> deleteImportTable = new HashMap<>();
          deleteImportTable.put(LiteralConstants.SIGNATURE,
                  LockSignature.DELETE_IMPORT_TABLE.getValue());
          deleteImportTable.put(LiteralConstants.DATASETID, datasetId);
          deleteImportTable.put(LiteralConstants.TABLESCHEMAID, table.getIdTableSchema());
          lockService.removeLockByCriteria(deleteImportTable);
        }

        // Set the 'releasing' property to false in the dataset metabase
        ReportingDatasetVO reportingVO = new ReportingDatasetVO();
        reportingVO.setId(datasetId);
        reportingVO.setReleasing(false);
        reportingDatasetService.updateReportingDatasetMetabase(reportingVO);
      }
      LOG.info("All dataset operation have been locked for datasets in  dataflowId {} with dataProviderId {}", dataflowId, dataProviderId);

      Map<String, Object> populateEuDataset = new HashMap<>();
      populateEuDataset.put(LiteralConstants.SIGNATURE, LockSignature.POPULATE_EU_DATASET.getValue());
      populateEuDataset.put(LiteralConstants.DATAFLOWID, dataflowId);

      Map<String, Object> releaseSnapshots = new HashMap<>();
      releaseSnapshots.put(LiteralConstants.SIGNATURE, LockSignature.RELEASE_SNAPSHOTS.getValue());
      releaseSnapshots.put(LiteralConstants.DATAFLOWID, dataflowId);
      releaseSnapshots.put(LiteralConstants.DATAPROVIDERID, dataProviderId);


      lockService.removeLockByCriteria(populateEuDataset);
      lockService.removeLockByCriteria(releaseSnapshots);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error releasing locks related to release for dataflowId {} and dataProviderId {} Message: {}", dataflowId, dataProviderId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void updateSnapshotDisabled(Long datasetId) {
    snapshotRepository.updateSnapshotEnabledFalse(datasetId);
    LOG.info("Snapshots disabled from dataset: {}", datasetId);
  }

  /**
   * Delete snapshot by dataset id and date released is null.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void deleteSnapshotByDatasetIdAndDateReleasedIsNull(Long datasetId) {
    snapshotRepository.deleteSnapshotByDatasetIdAndDateReleasedIsNull(datasetId);
    LOG.info("Snapshots deleted from dataset: {}", datasetId);
  }

  /**
   * Checks if is admin.
   *
   * @return true, if is admin
   */
  private boolean isAdmin() {
    String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(role -> roleAdmin.equals(role.getAuthority()));
  }

  /**
   * Adds the locks related to release.
   *
   * @param datasets the datasets
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void addLocksRelatedToRelease(List<Long> datasets, Long dataflowId) throws EEAException {

    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    for (Long datasetId : datasets) {

      Map<String, Object> insertRecords = new HashMap<>();
      insertRecords.put(LiteralConstants.SIGNATURE, LockSignature.INSERT_RECORDS.getValue());
      insertRecords.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, insertRecords);

      Map<String, Object> deleteRecords = new HashMap<>();
      deleteRecords.put(LiteralConstants.SIGNATURE, LockSignature.DELETE_RECORDS.getValue());
      deleteRecords.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, deleteRecords);

      Map<String, Object> updateField = new HashMap<>();
      updateField.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_FIELD.getValue());
      updateField.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, updateField);

      Map<String, Object> updateRecords = new HashMap<>();
      updateRecords.put(LiteralConstants.SIGNATURE, LockSignature.UPDATE_RECORDS.getValue());
      updateRecords.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, updateRecords);

      Map<String, Object> deleteDatasetValues = new HashMap<>();
      deleteDatasetValues.put(LiteralConstants.SIGNATURE,
              LockSignature.DELETE_DATASET_VALUES.getValue());
      deleteDatasetValues.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, deleteDatasetValues);

      Map<String, Object> insertRecordsMultitable = new HashMap<>();
      insertRecordsMultitable.put(LiteralConstants.SIGNATURE,
              LockSignature.INSERT_RECORDS_MULTITABLE.getValue());
      insertRecordsMultitable.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, insertRecordsMultitable);

      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, importFileData);

      Map<String, Object> importBigFileData = new HashMap<>();
      importBigFileData.put(LiteralConstants.SIGNATURE,
              LockSignature.IMPORT_BIG_FILE_DATA.getValue());
      importBigFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.createLock(timestamp, userName, LockType.METHOD, importBigFileData);

      DataSetSchemaVO schema = schemaService.getDataSchemaByDatasetId(false, datasetId);
      for (TableSchemaVO table : schema.getTableSchemas()) {
        Map<String, Object> deleteImportTable = new HashMap<>();
        deleteImportTable.put(LiteralConstants.SIGNATURE,
                LockSignature.DELETE_IMPORT_TABLE.getValue());
        deleteImportTable.put(LiteralConstants.DATASETID, datasetId);
        deleteImportTable.put(LiteralConstants.TABLESCHEMAID, table.getIdTableSchema());
        lockService.createLock(timestamp, userName, LockType.METHOD, deleteImportTable);
      }

      ReportingDatasetVO reportingVO = new ReportingDatasetVO();
      reportingVO.setId(datasetId);
      reportingVO.setReleasing(true);
      reportingDatasetService.updateReportingDatasetMetabase(reportingVO);
    }

    Map<String, Object> populateEuDataset = new HashMap<>();
    populateEuDataset.put(LiteralConstants.SIGNATURE, LockSignature.POPULATE_EU_DATASET.getValue());
    populateEuDataset.put(LiteralConstants.DATAFLOWID, dataflowId);
    LockVO lockVO = lockService.findByCriteria(populateEuDataset);
    if (lockVO == null) {
      lockService.createLock(timestamp, userName, LockType.METHOD, populateEuDataset);
    }
  }

  /**
   * Removes historic release
   * @param datasetId
   */
  @Override
  public void removeHistoricRelease(Long datasetId) {
    snapshotRepository.removeHistoricRelease(datasetId);
  }

  /**
   * Gets latest release snapshot
   * @param datasetId
   * @return
   */
  @Override
  public SnapshotVO getLatestReleaseSnapshot(Long datasetId) {
    Snapshot snapshot = snapshotRepository.findFirstByReportingDatasetIdAndDateReleasedIsNotNullOrderByCreationDateDesc(datasetId);
    return snapshotMapper.entityToClass(snapshot);
  }
}
