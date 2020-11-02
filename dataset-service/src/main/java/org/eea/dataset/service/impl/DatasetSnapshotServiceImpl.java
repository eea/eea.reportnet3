package org.eea.dataset.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.ReleaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.mapper.SnapshotSchemaMapper;
import org.eea.dataset.persistence.data.domain.Validation;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.EUDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.EUDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotSchemaRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.RulesRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.pdf.ReceiptPDFGenerator;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.interfaces.vo.metabase.ReleaseReceiptVO;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

/**
 * The Class DatasetSnapshotServiceImpl.
 */
@Service("datasetSnapshotService")
public class DatasetSnapshotServiceImpl implements DatasetSnapshotService {

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

  /** The validation repository. */
  @Autowired
  private ValidationRepository validationRepository;

  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

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

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;


  /** The Constant FILE_PATTERN_NAME. */
  private static final String FILE_PATTERN_NAME = "schemaSnapshot_%s-DesignDataset_%s";

  /** The Constant FILE_PATTERN_NAME_RULES. */
  private static final String FILE_PATTERN_NAME_RULES = "rulesSnapshot_%s-DesignDataset_%s";

  /** The Constant FILE_PATTERN_NAME_UNIQUE. */
  private static final String FILE_PATTERN_NAME_UNIQUE = "uniqueSnapshot_%s-DesignDataset_%s";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetSnapshotServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
   * Adds the snapshot.
   *
   * @param idDataset the id dataset
   * @param description the description
   * @param released the released
   * @param partitionIdDestination the partition id destination
   */
  @Override
  @Async
  public void addSnapshot(Long idDataset, CreateSnapshotVO createSnapshotVO,
      Long partitionIdDestination) {


    List<Validation> isBlocked = null;
    try {
      setTenant(idDataset);
      isBlocked = validationRepository.findByLevelError(ErrorTypeEnum.BLOCKER);

      // 1. Create the snapshot in the metabase
      Snapshot snap = new Snapshot();
      snap.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
      snap.setDescription(createSnapshotVO.getDescription());
      DataSetMetabase dataset = new DataSetMetabase();
      dataset.setId(idDataset);
      if (DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(idDataset))) {
        dataset = metabaseRepository.findById(idDataset).orElse(new DataSetMetabase());
        if (dataset.getDatasetSchema() != null) {
          DataCollection dataCollection = dataCollectionRepository
              .findFirstByDatasetSchema(dataset.getDatasetSchema()).orElse(new DataCollection());
          snap.setDataCollectionId(dataCollection.getId());
        }
      }
      snap.setReportingDataset(dataset);
      snap.setDataSetName("snapshot from dataset_" + idDataset);
      snap.setDcReleased(createSnapshotVO.getReleased());
      snap.setEuReleased(false);
      snap.setBlocked(isBlocked != null && !isBlocked.isEmpty());

      snap.setAutomatic(
          Boolean.TRUE.equals(createSnapshotVO.getAutomatic()) ? Boolean.TRUE : Boolean.FALSE);

      snapshotRepository.save(snap);
      LOG.info("Snapshot {} created into the metabase", snap.getId());
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
      recordStoreControllerZuul.createSnapshotData(idDataset, snap.getId(), idPartition);

    } catch (Exception e) {
      LOG_ERROR.error("Error creating snapshot for dataset {}", idDataset, e);
      releaseEvent(EventType.ADD_DATASET_SNAPSHOT_FAILED_EVENT, idDataset, e.getMessage());
      // Release the lock manually
      List<Object> criteria = new ArrayList<>();
      criteria.add(LockSignature.CREATE_SNAPSHOT.getValue());
      criteria.add(idDataset);
      criteria.add(createSnapshotVO.getReleased());
      lockService.removeLockByCriteria(criteria);
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
  private void releaseEvent(EventType eventType, Long datasetId, String error) {
    try {
      if (error == null) {
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .datasetId(datasetId).build());
      } else {
        kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null,
            NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
                .datasetId(datasetId).error(error).build());
      }
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing notification", e);
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

    if (Boolean.TRUE.equals(snapshotRepository.findById(idSnapshot).get().getAutomatic())) {
      LOG_ERROR.error("Error deleting snapshot, the snapshot is automatic");
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
  public void restoreSnapshot(Long idDataset, Long idSnapshot, Boolean deleteData)
      throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZuul.restoreSnapshotData(idDataset, idSnapshot, idPartition,
        DatasetTypeEnum.REPORTING, (String) ThreadPropertiesManager.getVariable("user"), false,
        deleteData);
  }

  /**
   * Restore snapshot to clone data.
   *
   * @param datasetOrigin the dataset origin
   * @param idDatasetDestination the id dataset destination
   * @param idSnapshot the id snapshot
   * @param deleteData the delete data
   * @param datasetType the dataset type
   * @param user the user
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void restoreSnapshotToCloneData(Long datasetOrigin, Long idDatasetDestination,
      Long idSnapshot, Boolean deleteData, DatasetTypeEnum datasetType, String user)
      throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(datasetOrigin, "root").getId();
    recordStoreControllerZuul.restoreSnapshotData(idDatasetDestination, idSnapshot, idPartition,
        datasetType, user, false, deleteData);
  }

  /**
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws EEAException
   */
  @Override
  @Async
  public void releaseSnapshot(Long idDataset, Long idSnapshot) throws EEAException {

    // First of all, we create a lock to prevent copy data from DC to EU dataset while this process
    // is releasign to DC
    Long dataflowId = datasetService.getDataFlowIdById(idDataset);
    addLockRelatedToCopyDataToEUDataset(dataflowId);

    Long providerId = 0L;
    DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(idDataset);
    if (metabase.getDataProviderId() != null) {
      providerId = metabase.getDataProviderId();
    }

    final Long idDataProvider = providerId;

    // Get the provider
    DataProviderVO provider = representativeControllerZuul.findDataProviderById(idDataProvider);

    // Get the dataCollection
    Optional<DataSetMetabase> designDataset = metabaseRepository.findById(idDataset);
    String datasetSchema = designDataset.isPresent() ? designDataset.get().getDatasetSchema() : "";
    Optional<DataCollection> dataCollection =
        dataCollectionRepository.findFirstByDatasetSchema(datasetSchema);
    Long idDataCollection = dataCollection.isPresent() ? dataCollection.get().getId() : null;

    // Delete data of the same provider
    deleteDataProvider(idDataset, idSnapshot, idDataProvider, provider, idDataCollection);

    removeLockRelatedToCopyDataToEUDataset(dataflowId);
    removeLock(idDataset, LockSignature.RELEASE_SNAPSHOT);
    removeLockRelatedToCopyDataToEUDataset(dataflowId);

    Map<String, Object> value = new HashMap<>();
    value.put(LiteralConstants.DATASET_ID, idDataset);
    kafkaSenderUtils.releaseKafkaEvent(EventType.RELEASE_ONEBYONE_COMPLETED_EVENT, value);
  }

  /**
   * Delete data provider.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param idDataProvider the id data provider
   * @param provider the provider
   * @param idDataCollection the id data collection
   */
  private void deleteDataProvider(Long idDataset, Long idSnapshot, final Long idDataProvider,
      DataProviderVO provider, Long idDataCollection) {
    Long idDataflow = datasetService.getDataFlowIdById(idDataset);
    if (provider != null && idDataCollection != null) {
      datasetService.deleteRecordValuesByProvider(idDataCollection, provider.getCode());

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
            representativeControllerZuul.updateRepresentative(representative);
            LOG.info("Receipt from the representative {} marked as outdated",
                representative.getId());
          }
        }

        // This method will release the lock and the notification
        restoreSnapshot(idDataCollection, idSnapshot, false);
        // Check if the snapshot is released
        snapshotRepository.releaseSnaphot(idDataset, idSnapshot);
        // Add the date of the release
        Optional<Snapshot> snapshot = snapshotRepository.findById(idSnapshot);
        if (snapshot.isPresent()) {
          snapshot.get().setDateReleased(java.sql.Timestamp.valueOf(LocalDateTime.now()));
          snapshotRepository.save(snapshot.get());
        }

        LOG.info("Snapshot {} released", idSnapshot);
      } catch (EEAException e) {
        LOG_ERROR.error(e.getMessage());
        releaseEvent(EventType.RELEASE_FAILED_EVENT, idSnapshot, e.getMessage());
        removeLock(idDataset, LockSignature.RELEASE_SNAPSHOT);
        removeLockRelatedToCopyDataToEUDataset(idDataflow);
      }
    } else {
      LOG_ERROR.error("Error in release snapshot");
      releaseEvent(EventType.RELEASE_FAILED_EVENT, idSnapshot, "Error in release snapshot");
      removeLock(idDataset, LockSignature.RELEASE_SNAPSHOT);
      removeLockRelatedToCopyDataToEUDataset(idDataflow);
    }
  }

  /**
   * Sets the tenant.
   *
   * @param idDataset the new tenant
   */
  private void setTenant(Long idDataset) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
  }

  /**
   * Removes the lock.
   *
   * @param idLock the id lock
   * @param lock the lock
   */
  private void removeLock(Long idLock, LockSignature lock) {
    List<Object> criteria = new ArrayList<>();
    criteria.add(lock.getValue());
    criteria.add(idLock);
    lockService.removeLockByCriteria(criteria);
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
      snap.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
      snap.setDescription(description);
      DesignDataset designDataset = new DesignDataset();
      designDataset.setId(idDataset);
      snap.setDesignDataset(designDataset);
      snap.setDataSetName("snapshot schema from design dataset_" + idDataset);
      snapshotSchemaRepository.save(snap);
      LOG.info("Snapshot schema {} created into the metabase", snap.getId());

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

      // 3. Create the data file of the snapshot, calling to recordstore-service
      // we need the partitionId. By now only consider the user root
      Long idPartition = obtainPartition(idDataset, "root").getId();
      recordStoreControllerZuul.createSnapshotData(idDataset, idSnapshot, idPartition);
      LOG.info("Snapshot schema {} data files created", idSnapshot);
    } catch (Exception e) {
      LOG_ERROR.error("Error creating snapshot for dataset schema {}", idDataset, e);
      releaseEvent(EventType.ADD_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT, idDataset, e.getMessage());
      // Release the lock manually
      List<Object> criteria = new ArrayList<>();
      criteria.add(LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      criteria.add(idDataset);
      lockService.removeLockByCriteria(criteria);
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
  public void restoreSchemaSnapshot(Long idDataset, Long idSnapshot)
      throws EEAException, IOException {

    try {
      // Get the schema document to mapper it to DataSchema class
      String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset) + ".snap";
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] content = documentControllerZuul.getSnapshotDocument(idDataset, nameFile);
      DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
      LOG.info("Schema class recovered");

      // Get the rules document to mapper it to RulesSchema class
      String nameFileRules = String.format(FILE_PATTERN_NAME_RULES, idSnapshot, idDataset)
          + LiteralConstants.SNAPSHOT_EXTENSION;
      ObjectMapper objectMapperRules = new ObjectMapper();
      objectMapperRules.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] contentRules = documentControllerZuul.getSnapshotDocument(idDataset, nameFileRules);
      RulesSchema rules = objectMapperRules.readValue(contentRules, RulesSchema.class);
      LOG.info("Schema rules class recovered");

      // Since there's the Unique property, we need to restore that file too
      // Get the unique document to mapper it into the List of UniqueConstraintSchema
      String nameFileUnique = String.format(FILE_PATTERN_NAME_UNIQUE, idSnapshot, idDataset)
          + LiteralConstants.SNAPSHOT_EXTENSION;
      ObjectMapper objectMapperUnique = new ObjectMapper();
      objectMapperUnique.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] contentUnique = documentControllerZuul.getSnapshotDocument(idDataset, nameFileUnique);
      List<UniqueConstraintSchema> listUnique = objectMapperUnique.readValue(contentUnique,
          new TypeReference<List<UniqueConstraintSchema>>() {});
      LOG.info("Schema Unique class recovered");


      rulesControllerZuul.deleteRulesSchema(schema.getIdDataSetSchema().toString());
      rulesRepository.save(rules);

      uniqueConstraintRepository.deleteByDatasetSchemaId(schema.getIdDataSetSchema());
      uniqueConstraintRepository.saveAll(listUnique);


      // First we delete all the entries in the catalogue of the previous schema, before replacing
      // it
      // by the one of the snapshot
      schemaService.updatePkCatalogueDeletingSchema(schema.getIdDataSetSchema().toString());
      // Replace the schema: delete the older and save the new we have already recovered on step
      // Also in the service we call the recordstore to do the restore of the dataset_X data
      schemaService.replaceSchema(schema.getIdDataSetSchema().toString(), schema, idDataset,
          idSnapshot);
      // fill the PK catalogue with the new schema
      // also the table foreign_relations
      schemaService.updatePKCatalogueAndForeignsAfterSnapshot(
          schema.getIdDataSetSchema().toString(), idDataset);

      LOG.info("Schema Snapshot {} totally restored", idSnapshot);
    } catch (EEAException | FeignException e) {
      LOG_ERROR.error("Error restoring a schema snapshot. IdDataset {}, IdSnapshot {}.Message: ",
          idDataset, idSnapshot, e.getMessage(), e);
      releaseEvent(EventType.RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT, idDataset,
          "Error restoring the schema snapshot");
      // Release the lock manually
      removeLock(idDataset, LockSignature.RESTORE_SCHEMA_SNAPSHOT);
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
    // Delete the schema file
    String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset)
        + LiteralConstants.SNAPSHOT_EXTENSION;
    documentControllerZuul.deleteSnapshotSchemaDocument(idDataset, nameFile);

    // Delete the rules file
    String nameRulesFile = String.format(FILE_PATTERN_NAME_RULES, idSnapshot, idDataset)
        + LiteralConstants.SNAPSHOT_EXTENSION;
    documentControllerZuul.deleteSnapshotSchemaDocument(idDataset, nameRulesFile);

    // Delete the file values
    recordStoreControllerZuul.deleteSnapshotData(idDataset, idSnapshot);

    LOG.info("Schema Snapshot {} removed", idSnapshot);
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

    LOG.info("Deleting all schema snapshots when the design dataset it's going to be deleted");
    List<SnapshotVO> snapshots = getSchemaSnapshotsByIdDataset(idDesignDataset);
    snapshots.stream().forEach(s -> {
      try {
        removeSchemaSnapshot(idDesignDataset, s.getId());
      } catch (Exception e) {
        LOG_ERROR.error("Error deleting the schema snapshot " + s.getId(), e.getMessage(), e);
      }
    });
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
  private PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
      throws EEAException {
    final PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(datasetId, user).orElse(null);
    if (partition == null) {
      LOG_ERROR.error(EEAErrorMessage.PARTITION_ID_NOTFOUND);
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
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);
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

    UserRepresentationVO user = userManagementControllerZull.getUserByUserId(
        ((Map<String, String>) SecurityContextHolder.getContext().getAuthentication().getDetails())
            .get(AuthenticationDetails.USER_ID));
    receipt.setUserName(user.getUsername());
    receipt.setFullUserName((null != user.getFirstName() ? user.getFirstName() : "") + " "
        + (null != user.getLastName() ? user.getLastName() : ""));

    if (!representatives.isEmpty()) {
      RepresentativeVO representative = representatives.get(0);

      receipt.setProviderEmail(representative.getProviderAccount());

      // Check if it's needed to update the status of the button (i.e I only want to download the
      // receipt twice, but no state is changed)
      if (!representative.getReceiptDownloaded() || representative.getReceiptOutdated()) {
        // update provider. Button downloaded = true && outdated = false
        representative.setReceiptDownloaded(true);
        representative.setReceiptOutdated(false);
        representativeControllerZuul.updateRepresentative(representative);
        LOG.info("Receipt from the representative {} marked as downloaded", representative.getId());
      }
    }

    receiptPDFGenerator.generatePDF(receipt, out);
  }


  /**
   * Adds the lock related to copy data to EU dataset.
   *
   * @param dataflowId the dataflow id
   * @throws EEAException the EEA exception
   */
  private void addLockRelatedToCopyDataToEUDataset(Long dataflowId) throws EEAException {
    Map<String, Object> mapCriteria = new HashMap<>();
    mapCriteria.put("signature", LockSignature.POPULATE_EU_DATASET.getValue());
    mapCriteria.put("dataflowId", dataflowId);
    lockService.createLock(new Timestamp(System.currentTimeMillis()),
        (String) ThreadPropertiesManager.getVariable("user"), LockType.METHOD, mapCriteria);
  }

  /**
   * Removes the lock related to copy data to EU dataset.
   *
   * @param dataflowId the dataflow id
   */
  private void removeLockRelatedToCopyDataToEUDataset(Long dataflowId) {
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.POPULATE_EU_DATASET.getValue());
    criteria.add(dataflowId);
    lockService.removeLockByCriteria(criteria);
  }

  /**
   * Gets the snapshots released by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots released by id dataset
   * @throws EEAException the EEA exception
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
   * @throws EEAException the EEA exception
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
      LOG_ERROR.error(EEAErrorMessage.DATASET_NOTFOUND);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    DataCollection dataCollection = dataCollectionRepository
        .findFirstByDatasetSchema(eudataset.getDatasetSchema()).orElse(null);
    if (dataCollection == null) {
      LOG_ERROR.error(EEAErrorMessage.DATASET_NOTFOUND);
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
   * @throws EEAException the EEA exception
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
    if (DatasetTypeEnum.REPORTING.equals(datasetService.getDatasetType(datasetId))) {
      // if dataset is reporting return released snapshots
      releases = getSnapshotsReleasedByIdDataset(datasetId);
    } else {
      // if the snapshot is a datacollection
      if (DatasetTypeEnum.COLLECTION.equals(datasetService.getDatasetType(datasetId))) {
        releases = getSnapshotsReleasedByIdDataCollection(datasetId);
      } else
      // if the snapshot is an eudataset
      if (DatasetTypeEnum.EUDATASET.equals(datasetService.getDatasetType(datasetId))) {
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
   * @throws EEAException
   */
  @Override
  @Async
  public void createReleaseSnapshots(Long dataflowId, Long dataProviderId) throws EEAException {
    // List of the datasets involved
    ReportingDataset dataset = reportingDatasetRepository
        .findFirstByDataflowIdAndDataProviderIdOrderByIdAsc(dataflowId, dataProviderId);
    validationControllerZuul.validateDataSetData(dataset.getId(), true);

  }


  /**
   * Adds the locks related to release.
   *
   * @param datasets the datasets
   * @param representatives the representatives
   * @throws EEAException the EEA exception
   */
  private void addLocksRelatedToRelease(List<Long> datasets, List<RepresentativeVO> representatives)
      throws EEAException {
    // We have to lock all the dataset operations (insert, delete, update...)
    for (Long datasetId : datasets) {
      // Insert
      Map<String, Object> mapCriteriaInsert = new HashMap<>();
      mapCriteriaInsert.put("signature", LockSignature.INSERT_RECORDS.getValue());
      mapCriteriaInsert.put("datasetId", datasetId);
      lockService.createLock(new Timestamp(System.currentTimeMillis()),
          SecurityContextHolder.getContext().getAuthentication().getName(), LockType.METHOD,
          mapCriteriaInsert);
    }
    // Also, we have to update the representative 'releasing' property to true
    if (!representatives.isEmpty()) {
      RepresentativeVO representative = representatives.get(0);
      representative.setReleasing(true);
      representativeControllerZuul.updateRepresentative(representative);
    }
  }


  /**
   * Release locks related to release.
   *
   * @param datasets the datasets
   * @param representatives the representatives
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  private void releaseLocksRelatedToRelease(List<Long> datasets,
      List<RepresentativeVO> representatives, Long dataflowId, Long dataProviderId) {
    // We have to lock all the dataset operations (insert, delete, update...)
    for (Long datasetId : datasets) {
      List<Object> criteriaInsert = new ArrayList<>();
      criteriaInsert.add(LockSignature.INSERT_RECORDS.getValue());
      criteriaInsert.add(datasetId);
      lockService.removeLockByCriteria(criteriaInsert);
    }
    // Also, we have to update the representative 'releasing' property to false
    if (!representatives.isEmpty()) {
      RepresentativeVO representative = representatives.get(0);
      representative.setReleasing(false);
      representativeControllerZuul.updateRepresentative(representative);
    }
    // Finally, unlock the release operation itself
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.RELEASE_SNAPSHOTS.getValue());
    criteria.add(dataflowId);
    criteria.add(dataProviderId);
    lockService.removeLockByCriteria(criteria);
  }


}
