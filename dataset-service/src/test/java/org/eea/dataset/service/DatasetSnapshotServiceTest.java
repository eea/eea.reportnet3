/*
 * 
 */
package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.mapper.SnapshotSchemaMapper;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotSchemaRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.impl.DatasetSnapshotServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class DatasetSnapshotServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSnapshotServiceTest {

  /** The dataset metabase service. */
  @InjectMocks
  private DatasetSnapshotServiceImpl datasetSnapshotService;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The snapshot repository. */
  @Mock
  private SnapshotRepository snapshotRepository;

  /** The data set metabase mapper. */
  @Mock
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The snapshot mapper. */
  @Mock
  private SnapshotMapper snapshotMapper;

  /** The snapshot schema mapper. */
  @Mock
  private SnapshotSchemaMapper snapshotSchemaMapper;

  /** The record store controller zull. */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The partition data set metabase repository. */
  @Mock
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The record repository. */
  @Mock
  private RecordRepository recordRepository;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The snapshot schema repository. */
  @Mock
  private SnapshotSchemaRepository snapshotSchemaRepository;

  /** The schema repository. */
  @Mock
  private SchemasRepository schemaRepository;

  /** The document controller zuul. */
  @Mock
  private DocumentControllerZuul documentControllerZuul;

  /** The schema service. */
  @Mock
  private DatasetSchemaService schemaService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
  }



  /**
   * Test get snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSnapshots() throws Exception {

    when(snapshotMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetSnapshotService.getSnapshotsByIdDataset(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSnapshotsByIdDataset(Mockito.anyLong()));

  }

  /**
   * Test add snapshots.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addSnapshotTest1() throws EEAException {
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, "test", false);
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());
  }

  /**
   * Adds the snapshot test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addSnapshotTest2() throws EEAException {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(recordStoreControllerZull).createSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, "test", false);
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());
  }

  /**
   * Adds the snapshot test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addSnapshotTest3() throws EEAException {
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, "test", false);
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {

    datasetSnapshotService.removeSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).deleteById(Mockito.anyLong());


  }

  /**
   * Test restore snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testRestoreSnapshotsException() throws Exception {

    datasetSnapshotService.restoreSnapshot(1L, 1L, true);

  }

  /**
   * Test restore snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSnapshots() throws Exception {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    datasetSnapshotService.restoreSnapshot(1L, 1L, true);
    Mockito.verify(partitionDataSetMetabaseRepository, times(1))
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any());
  }


  /**
   * Release snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void releaseSnapshot() throws Exception {
    DataSetMetabaseVO metabase = new DataSetMetabaseVO();
    DataCollection dataCollection = new DataCollection();
    dataCollection.setId(1L);
    metabase.setDataProviderId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(dataSetMetabaseRepository.findDatasetSchemaIdById(Mockito.any())).thenReturn("");
    Mockito.when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(dataCollection));
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    datasetSnapshotService.restoreSnapshot(1L, 1L, true);
    doNothing().when(snapshotRepository).releaseSnaphot(Mockito.any(), Mockito.any());
    datasetSnapshotService.releaseSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).releaseSnaphot(Mockito.any(), Mockito.any());
  }


  /**
   * Test get schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSchemaSnapshots() throws Exception {

    when(snapshotSchemaMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    datasetSnapshotService.getSchemaSnapshotsByIdDataset(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSchemaSnapshotsByIdDataset(Mockito.anyLong()));

  }

  /**
   * Adds the schema snapshot test 1.
   *
   * @throws Exception the exception
   */
  @Test
  public void addSchemaSnapshotTest1() throws Exception {

    doNothing().when(documentControllerZuul).uploadSchemaSnapshotDocument(Mockito.any(),
        Mockito.any(), Mockito.any());
    when(schemaRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(new DataSetSchema());
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSchemaSnapshot(1L, "5db99d0bb67ca68cb8fa7053", "test");
    Mockito.verify(snapshotSchemaRepository, times(1)).save(Mockito.any());
  }

  /**
   * Adds the schema snapshot test 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void addSchemaSnapshotTest2() throws Exception {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(recordStoreControllerZull).createSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any());
    doNothing().when(documentControllerZuul).uploadSchemaSnapshotDocument(Mockito.any(),
        Mockito.any(), Mockito.any());
    when(schemaRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(new DataSetSchema());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSchemaSnapshot(1L, "5db99d0bb67ca68cb8fa7053", "test");
    Mockito.verify(snapshotSchemaRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test delete schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSchemaSnapshots() throws Exception {

    doNothing().when(documentControllerZuul).deleteSnapshotSchemaDocument(Mockito.any(),
        Mockito.any());
    datasetSnapshotService.removeSchemaSnapshot(1L, 1L);
    Mockito.verify(snapshotSchemaRepository, times(1)).deleteSnapshotSchemaById(Mockito.anyLong());

  }

  /**
   * Test restore schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSchemaSnapshots() throws Exception {

    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    ObjectMapper objectMapper = new ObjectMapper();

    when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any()))
        .thenReturn(objectMapper.writeValueAsBytes(schema));

    datasetSnapshotService.restoreSchemaSnapshot(1L, 1L);
    Mockito.verify(schemaService, times(1)).replaceSchema(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  /**
   * Test delete all schema snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteAllSchemaSnapshots() throws Exception {

    SnapshotVO snap = new SnapshotVO();
    snap.setId(1L);
    List<SnapshotVO> snapshots = new ArrayList<>();
    snapshots.add(snap);
    when(snapshotSchemaMapper.entityListToClass(Mockito.any())).thenReturn(snapshots);
    datasetSnapshotService.deleteAllSchemaSnapshots(1L);

    Mockito.verify(snapshotSchemaMapper, times(1)).entityListToClass(Mockito.any());
  }

  /**
   * Test delete all schema snapshots exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteAllSchemaSnapshotsException() throws Exception {

    SnapshotVO snap = new SnapshotVO();
    snap.setId(null);
    List<SnapshotVO> snapshots = new ArrayList<>();
    snapshots.add(snap);
    when(snapshotSchemaMapper.entityListToClass(Mockito.any())).thenReturn(snapshots);
    doThrow(new Exception()).when(documentControllerZuul)
        .deleteSnapshotSchemaDocument(Mockito.any(), Mockito.any());
    datasetSnapshotService.deleteAllSchemaSnapshots(1L);
    Mockito.verify(snapshotSchemaRepository, times(1))
        .findByDesignDatasetIdOrderByCreationDateDesc(Mockito.any());
  }


  /**
   * Test delete all snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteAllSnapshots() throws Exception {

    SnapshotVO snap = new SnapshotVO();
    snap.setId(1L);
    List<SnapshotVO> snapshots = new ArrayList<>();
    snapshots.add(snap);
    when(snapshotMapper.entityListToClass(Mockito.any())).thenReturn(snapshots);
    datasetSnapshotService.deleteAllSnapshots(1L);
    Mockito.verify(snapshotMapper, times(1)).entityListToClass(Mockito.any());
  }



  /**
   * After tests.
   */
  @After
  public void afterTests() {
    File file = new File("./nullschemaSnapshot_null-DesignDataset_1.snap");
    file.delete();
  }


}
