package org.eea.dataset.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.mapper.SnapshotSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.domain.SnapshotSchema;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotSchemaRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class DatasetSnapshotServiceImpl.
 */
@Service("datasetSnapshotService")
public class DatasetSnapshotServiceImpl implements DatasetSnapshotService {

  /**
   * The partition data set metabase repository.
   */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * The snapshot repository.
   */
  @Autowired
  private SnapshotRepository snapshotRepository;

  /**
   * The snapshot mapper.
   */
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

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The document controller zuul. */
  @Autowired
  private DocumentControllerZuul documentControllerZuul;

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


  /** The Constant FILE_PATTERN_NAME. */
  private static final String FILE_PATTERN_NAME = "schemaSnapshot_%s-DesignDataset_%s";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMetabaseServiceImpl.class);


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void addSnapshot(Long idDataset, String description) throws EEAException {

    // 1. Create the snapshot in the metabase
    Snapshot snap = new Snapshot();
    snap.setCreationDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
    snap.setDescription(description);
    ReportingDataset reportingDataset = new ReportingDataset();
    reportingDataset.setId(idDataset);
    snap.setReportingDataset(reportingDataset);
    snap.setDataSetName("snapshot from dataset_" + idDataset);
    snap.setRelease(false);

    snapshotRepository.save(snap);
    LOG.info("Snapshot {} created into the metabase", snap.getId());

    // 2. Create the data file of the snapshot, calling to recordstore-service
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZull.createSnapshotData(idDataset, snap.getId(), idPartition);

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.CREATE_SNAPSHOT.getValue());
    criteria.add(idDataset);
    lockService.removeLockByCriteria(criteria);

    LOG.info("Snapshot {} data files created", snap.getId());
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
  @Async
  public void removeSnapshot(Long idDataset, Long idSnapshot) throws EEAException {
    // Remove from the metabase
    snapshotRepository.deleteById(idSnapshot);
    // Delete the file
    recordStoreControllerZull.deleteSnapshotData(idDataset, idSnapshot);

    LOG.info("Snapshot {} removed", idSnapshot);
  }

  /**
   * Restore snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Async
  public void restoreSnapshot(Long idDataset, Long idSnapshot, String user) throws EEAException {

    // 1. Delete the dataset values implied
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZull.restoreSnapshotData(idDataset, idSnapshot, idPartition,
        TypeDatasetEnum.REPORTING, user);

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.RESTORE_SNAPSHOT.getValue());
    criteria.add(idDataset);
    lockService.removeLockByCriteria(criteria);

    LOG.info("Snapshot {} restored", idSnapshot);

  }



  /**
   * Release snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void releaseSnapshot(Long idDataset, Long idSnapshot) throws EEAException {

    snapshotRepository.releaseSnaphot(idDataset, idSnapshot);
    LOG.info("Snapshot {} released", idSnapshot);
  }


  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the schema snapshots by id dataset
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
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void addSchemaSnapshot(Long idDataset, String idDatasetSchema, String description)
      throws EEAException, IOException {

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
    String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset) + ".snap";
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    objectMapper.writeValue(outStream, schema);
    documentControllerZuul.uploadSchemaSnapshotDocument(outStream.toByteArray(), idDataset,
        nameFile);


    // 3. Create the data file of the snapshot, calling to recordstore-service
    // we need the partitionId. By now only consider the user root
    Long idPartition = obtainPartition(idDataset, "root").getId();
    recordStoreControllerZull.createSnapshotData(idDataset, idSnapshot, idPartition);

    // Release the lock manually
    List<Object> criteria = new ArrayList<>();
    criteria.add(LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
    criteria.add(idDataset);
    lockService.removeLockByCriteria(criteria);

    LOG.info("Snapshot schema {} data files created", idSnapshot);
  }

  /**
   * Restore schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @param user the user
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Async
  public void restoreSchemaSnapshot(Long idDataset, Long idSnapshot, String user)
      throws EEAException, IOException {

    try {
      // Get the schema document to mapper it to DataSchema class
      String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset) + ".snap";

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      byte[] content = documentControllerZuul.getSnapshotDocument(idDataset, nameFile);
      DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
      LOG.info("Schema class recovered");

      // Replace the schema: delete the older and save the new we have already recovered on step
      // Also in the service we call the recordstore to do the restore of the dataset_X data
      schemaService.replaceSchema(schema.getIdDataSetSchema().toString(), schema, idDataset,
          idSnapshot, user);

      LOG.info("Schema Snapshot {} totally restored", idSnapshot);
    } finally {
      // Remove the lock
      List<Object> criteria = new ArrayList<>();
      criteria.add(LockSignature.RESTORE_SCHEMA_SNAPSHOT.getValue());
      criteria.add(idDataset);
      lockService.removeLockByCriteria(criteria);
    }



  }

  /**
   * Removes the schema snapshot.
   *
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   * @throws Exception the exception
   */
  @Override
  @Async
  public void removeSchemaSnapshot(Long idDataset, Long idSnapshot) throws Exception {
    // Remove from the metabase
    snapshotSchemaRepository.deleteSnapshotSchemaById(idSnapshot);
    metabaseRepository.deleteSnapshotDatasetByIdSnapshot(idSnapshot);
    // Delete the file
    String nameFile = String.format(FILE_PATTERN_NAME, idSnapshot, idDataset) + ".snap";
    documentControllerZuul.deleteSnapshotSchemaDocument(idDataset, nameFile);

    // Delete the file values
    recordStoreControllerZull.deleteSnapshotData(idDataset, idSnapshot);

    LOG.info("Schema Snapshot {} removed", idSnapshot);
  }



  /**
   * Delete all schema snapshots.
   *
   * @param idDesignDataset the id design dataset
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


}
