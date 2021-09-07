/*
 *
 */
package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.mapper.ReleaseMapper;
import org.eea.dataset.mapper.SnapshotMapper;
import org.eea.dataset.mapper.SnapshotSchemaMapper;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
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
import org.eea.dataset.service.impl.DatasetSnapshotServiceImpl;
import org.eea.dataset.service.pdf.ReceiptPDFGenerator;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.collaboration.CollaborationController.CollaborationControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.controller.document.DocumentController.DocumentControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.interfaces.vo.rod.ObligationVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class DatasetSnapshotServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSnapshotServiceTest {

  /** The dataset metabase service. */
  @InjectMocks
  private DatasetSnapshotServiceImpl datasetSnapshotService;

  /** The receipt PDF generator. */
  @Mock
  private ReceiptPDFGenerator receiptPDFGenerator;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

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

  /** The record store controller zuul. */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

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


  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The validation repository. */
  @Mock
  private ValidationRepository validationRepository;

  /** The dataset snapshot controller. */
  @Mock
  private DatasetSnapshotController datasetSnapshotController;

  /** The rules controller zuul. */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The unique constraint repository. */
  @Mock
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The release mapper. */
  @Mock
  private ReleaseMapper releaseMapper;

  /** The e U dataset repository. */
  @Mock
  private EUDatasetRepository eUDatasetRepository;

  /** The snapshots. */
  private List<Snapshot> snapshots;

  /** The reporting dataset repository. */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The validation controller zuul. */
  @Mock
  private ValidationControllerZuul validationControllerZuul;

  /** The reporting dataset service. */
  @Mock
  private ReportingDatasetService reportingDatasetService;

  /** The collaboration controller zuul. */
  @Mock
  private CollaborationControllerZuul collaborationControllerZuul;

  /**
   * The security context.
   */
  private SecurityContext securityContext;

  /**
   * The authentication.
   */
  private Authentication authentication;

  /** The lead reporters VO. */
  private List<LeadReporterVO> leadReportersVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    Snapshot snapshot = new Snapshot();
    snapshots = new ArrayList<>();
    snapshot.setId(1L);
    snapshot.setDateReleased(new Date());
    snapshot.setEuReleased(true);
    snapshot.setDcReleased(true);
    Snapshot snapshot2 = new Snapshot();
    snapshot2.setId(2L);
    snapshot2.setDateReleased(new Date());
    snapshot2.setEuReleased(true);
    snapshot2.setDcReleased(false);
    snapshots.add(snapshot2);
    snapshots.add(snapshot);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    leadReportersVO = new ArrayList<>();
    leadReportersVO.add(new LeadReporterVO());
    MockitoAnnotations.openMocks(this);
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, new CreateSnapshotVO(), null, new Date().toString());
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
    doNothing().when(recordStoreControllerZuul).createSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, new CreateSnapshotVO(), 1L, new Date().toString());
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());
  }

  /**
   * Adds the snapshot test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void addSnapshotTest3() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(Mockito.any(), Mockito.any()))
        .thenReturn(Optional.empty());
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    datasetSnapshotService.addSnapshot(1L, new CreateSnapshotVO(), 1L, new Date().toString());
    Mockito.verify(snapshotRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test delete snapshots.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeleteSnapshots() throws Exception {
    Snapshot snap = new Snapshot();
    Mockito.when(snapshotRepository.findById(1L)).thenReturn(Optional.of(snap));
    datasetSnapshotService.removeSnapshot(1L, 1L);
    Mockito.verify(snapshotRepository, times(1)).deleteById(Mockito.anyLong());


  }

  /**
   * Test delete snapshots fail.
   *
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void testDeleteSnapshotsFail() throws Exception {
    Snapshot snap = new Snapshot();
    snap.setAutomatic(Boolean.TRUE);
    Mockito.when(snapshotRepository.findById(1L)).thenReturn(Optional.of(snap));
    try {
      datasetSnapshotService.removeSnapshot(1L, 1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.ERROR_DELETING_SNAPSHOT, e.getMessage());
      throw e;
    }


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
  public void restoreSnapshotToCloneDataTest() throws Exception {

    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    datasetSnapshotService.restoreSnapshotToCloneData(1L, 1L, 1L, true, DatasetTypeEnum.EUDATASET);
    Mockito.verify(recordStoreControllerZuul, times(1)).restoreSnapshotData(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(dataCollection));
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(snapshotRepository).releaseSnaphot(Mockito.any(), Mockito.any());
    datasetSnapshotService.releaseSnapshot(1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString());
    Mockito.verify(snapshotRepository, times(1)).releaseSnaphot(Mockito.any(), Mockito.any());
  }

  /**
   * Release snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void releaseSnapshotNotNull() throws Exception {
    DataSetMetabaseVO metabase = new DataSetMetabaseVO();
    DataCollection dataCollection = new DataCollection();
    List<RepresentativeVO> representatives = new ArrayList<>();
    RepresentativeVO rep = new RepresentativeVO();
    rep.setId(1L);
    rep.setDataProviderId(1L);
    rep.setReceiptOutdated(false);
    representatives.add(rep);
    dataCollection.setId(1L);
    metabase.setDataProviderId(1L);
    DataFlowVO dataFlowVO = new DataFlowVO();
    dataFlowVO.setManualAcceptance(false);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataFlowVO);
    Mockito.when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(dataCollection));
    Mockito.when(snapshotRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new Snapshot()));
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(snapshotRepository).releaseSnaphot(Mockito.any(), Mockito.any());
    datasetSnapshotService.releaseSnapshot(1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString());
    Mockito.verify(snapshotRepository, times(1)).releaseSnaphot(Mockito.any(), Mockito.any());
  }

  /**
   * Release snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void releaseSnapshotCatch() throws Exception {
    DataSetMetabaseVO metabase = new DataSetMetabaseVO();
    DataCollection dataCollection = new DataCollection();
    List<RepresentativeVO> representatives = new ArrayList<>();
    RepresentativeVO rep = new RepresentativeVO();
    rep.setId(1L);
    rep.setReceiptOutdated(false);
    representatives.add(rep);
    dataCollection.setId(1L);
    metabase.setDataProviderId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(dataCollection));
    datasetSnapshotService.releaseSnapshot(1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString());
    Mockito.verify(lockService, times(3)).removeLockByCriteria(Mockito.any());
  }


  /**
   * Release snapshot.
   *
   * @throws Exception the exception
   */
  @Test
  public void releaseSnapshotDataCollectionNull() throws Exception {
    DataSetMetabaseVO metabase = new DataSetMetabaseVO();
    metabase.setDataProviderId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    datasetSnapshotService.releaseSnapshot(1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString());
    Mockito.verify(lockService, times(3)).removeLockByCriteria(Mockito.any());
  }

  /**
   * Release snapshot status.
   *
   * @throws Exception the exception
   */
  @Test
  public void releaseSnapshotStatus() throws Exception {
    DataSetMetabaseVO metabase = new DataSetMetabaseVO();
    DataCollection dataCollection = new DataCollection();
    dataCollection.setId(1L);
    metabase.setDataProviderId(1L);
    DataFlowVO dataFlowVO = new DataFlowVO();
    dataFlowVO.setManualAcceptance(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(metabase);
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataFlowVO);
    Mockito.when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(dataCollection));
    when(partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(Mockito.anyLong(),
        Mockito.anyString())).thenReturn(Optional.of(new PartitionDataSetMetabase()));
    doNothing().when(snapshotRepository).releaseSnaphot(Mockito.any(), Mockito.any());
    datasetSnapshotService.releaseSnapshot(1L, 1L,
        java.sql.Timestamp.valueOf(LocalDateTime.now()).toString());
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
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
    doNothing().when(recordStoreControllerZuul).createSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
    doNothing().when(documentControllerZuul).uploadSchemaSnapshotDocument(Mockito.any(),
        Mockito.any(), Mockito.any());
    when(schemaRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(new DataSetSchema());
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
    ObjectMapper objectMapper2 = new ObjectMapper();
    ObjectMapper objectMapper3 = new ObjectMapper();

    RulesSchema rule = new RulesSchema();
    rule.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));

    UniqueConstraintSchema unique = new UniqueConstraintSchema();
    unique.setUniqueId(new ObjectId("5ce524fad31fc52540abae73"));
    unique.setDatasetSchemaId(new ObjectId("5ce524fad31fc52540abae73"));
    UniqueConstraintSchema unique2 = new UniqueConstraintSchema();
    unique2.setUniqueId(new ObjectId("5db99d0bb67ca68cb8fa7053"));
    unique2.setDatasetSchemaId(new ObjectId("5db99d0bb67ca68cb8fa7053"));
    List<UniqueConstraintSchema> listUnique = new ArrayList<>();
    listUnique.add(unique);
    listUnique.add(unique2);


    when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any())).thenReturn(
        objectMapper.writeValueAsBytes(schema), objectMapper2.writeValueAsBytes(rule),
        objectMapper3.writeValueAsBytes(listUnique));

    Mockito.doNothing().when(rulesControllerZuul).deleteRulesSchema(Mockito.anyString(),
        Mockito.anyLong());
    when(rulesRepository.save(Mockito.any())).thenReturn(new RulesSchema());

    when(uniqueConstraintRepository.deleteByDatasetSchemaId(Mockito.any())).thenReturn(0L);
    when(uniqueConstraintRepository.saveAll(Mockito.any())).thenReturn(new ArrayList<>());

    datasetSnapshotService.restoreSchemaSnapshot(1L, 1L);
    Mockito.verify(schemaService, times(1)).replaceSchema(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test restore schema snapshot exception.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRestoreSchemaSnapshotException() throws Exception {
    try {
      DataSetSchema schema = new DataSetSchema();
      schema.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73"));
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectMapper objectMapper2 = new ObjectMapper();
      ObjectMapper objectMapper3 = new ObjectMapper();

      RulesSchema rule = new RulesSchema();
      rule.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));

      UniqueConstraintSchema unique = new UniqueConstraintSchema();
      unique.setUniqueId(new ObjectId("5ce524fad31fc52540abae73"));
      unique.setDatasetSchemaId(new ObjectId("5ce524fad31fc52540abae73"));
      UniqueConstraintSchema unique2 = new UniqueConstraintSchema();
      unique2.setUniqueId(new ObjectId("5db99d0bb67ca68cb8fa7053"));
      unique2.setDatasetSchemaId(new ObjectId("5db99d0bb67ca68cb8fa7053"));
      List<UniqueConstraintSchema> listUnique = new ArrayList<>();
      listUnique.add(unique);
      listUnique.add(unique2);

      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");
      when(documentControllerZuul.getSnapshotDocument(Mockito.any(), Mockito.any())).thenReturn(
          objectMapper.writeValueAsBytes(schema), objectMapper2.writeValueAsBytes(rule),
          objectMapper3.writeValueAsBytes(listUnique));

      Mockito.doNothing().when(rulesControllerZuul).deleteRulesSchema(Mockito.anyString(),
          Mockito.anyLong());
      when(rulesRepository.save(Mockito.any())).thenReturn(new RulesSchema());

      when(uniqueConstraintRepository.deleteByDatasetSchemaId(Mockito.any())).thenReturn(0L);
      when(uniqueConstraintRepository.saveAll(Mockito.any())).thenReturn(new ArrayList<>());
      doThrow(new EEAException("failed")).when(schemaService)
          .updatePKCatalogueAndForeignsAfterSnapshot(Mockito.any(), Mockito.any());
      datasetSnapshotService.restoreSchemaSnapshot(1L, 1L);
    } catch (EEAException e) {
      Mockito.verify(lockService, times(1)).removeLockByCriteria(Mockito.any());
    }
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
   * After tests.
   */
  @After
  public void afterTests() {
    File file = new File("./nullschemaSnapshot_null-DesignDataset_1.snap");
    file.delete();
  }

  /**
   * Creates the recepit PDF.
   */
  @Test
  public void createRecepitPDFTest() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
    ReportingDatasetVO dataset = new ReportingDatasetVO();
    RepresentativeVO representative = new RepresentativeVO();
    List<ReportingDatasetVO> datasets = new ArrayList<>();
    List<RepresentativeVO> representatives = new ArrayList<>();
    DataFlowVO dataflowVO = new DataFlowVO();
    ObligationVO obligationVO = new ObligationVO();
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setUsername("userName");
    userRepresentationVO.setFirstName("First Name");
    userRepresentationVO.setLastName("Last Name");
    dataset.setIsReleased(true);
    dataset.setDataProviderId(1L);
    dataset.setDataSetName("datsetName");
    representative.setDataProviderId(1L);
    representative.setLeadReporters(leadReportersVO);
    representative.setReceiptDownloaded(true);
    representative.setReceiptOutdated(true);
    datasets.add(dataset);
    representatives.add(representative);
    obligationVO.setObligationId(1);
    obligationVO.setOblTitle("Obligation title");
    dataflowVO.setName("name");
    dataflowVO.setReportingDatasets(datasets);
    dataflowVO.setObligation(obligationVO);
    Mockito.when(dataflowControllerZuul.findById(Mockito.any(), Mockito.any()))
        .thenReturn(dataflowVO);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(new HashMap<>());
    Mockito.when(userManagementControllerZull.getUserByUserId(Mockito.any()))
        .thenReturn(userRepresentationVO);
    datasetSnapshotService.createReceiptPDF(null, 1L, 1L);
    Mockito.verify(representativeControllerZuul, times(1))
        .updateInternalRepresentative(Mockito.any());
  }

  /**
   * Gets the by id exception test.
   *
   * @return the by id exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByIdExceptionTest() throws EEAException {
    when(snapshotRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    assertNull("Snapshot with id 1 Not found", datasetSnapshotService.getById(1L));
  }

  /**
   * Gets the by id schema exception test.
   *
   * @return the by id schema exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByIdSchemaExceptionTest() throws EEAException {
    when(snapshotSchemaRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    assertNull("Snapshot with id 1 Not found", datasetSnapshotService.getSchemaById(1L));
  }

  /**
   * Gets the by id test.
   *
   * @return the by id test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getByIdTest() throws EEAException {
    SnapshotVO snap = new SnapshotVO();
    snap.setId(1L);
    when(snapshotRepository.findById(Mockito.any())).thenReturn(Optional.of(new Snapshot()));
    when(snapshotMapper.entityToClass(Mockito.any())).thenReturn(snap);
    assertEquals(snap, datasetSnapshotService.getById(1L));
  }

  /**
   * Gets the schema by id test.
   *
   * @return the schema by id test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getSchemaByIdTest() throws EEAException {
    SnapshotVO snap = new SnapshotVO();
    snap.setId(1L);
    when(snapshotSchemaRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new SnapshotSchema()));
    when(snapshotSchemaMapper.entityToClass(Mockito.any())).thenReturn(snap);
    assertEquals(snap, datasetSnapshotService.getSchemaById(1L));
  }

  /**
   * Gets the snapshots released by id dataset test.
   *
   * @return the snapshots released by id dataset test
   * @throws Exception the exception
   */
  @Test
  public void getSnapshotsReleasedByIdDatasetTest() throws Exception {
    when(snapshotRepository.findByReportingDatasetIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSnapshotsReleasedByIdDataset(Mockito.anyLong()));
  }

  /**
   * Gets the snapshots released by id data collection test.
   *
   * @return the snapshots released by id data collection test
   * @throws Exception the exception
   */
  @Test
  public void getSnapshotsReleasedByIdDataCollectionTest() throws Exception {
    when(snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSnapshotsReleasedByIdDataCollection(Mockito.anyLong()));
  }

  /**
   * Gets the snapshots released by id EU dataset success test.
   *
   * @return the snapshots released by id EU dataset success test
   * @throws Exception the exception
   */
  @Test
  public void getSnapshotsReleasedByIdEUDatasetSuccessTest() throws Exception {
    when(eUDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new EUDataset()));
    when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(new DataCollection()));
    when(snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetSnapshotService.getSnapshotsReleasedByIdEUDataset(Mockito.anyLong()));
  }

  /**
   * Gets the snapshots released by id EU dataset exception E unot found test.
   *
   * @return the snapshots released by id EU dataset exception E unot found test
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void getSnapshotsReleasedByIdEUDatasetExceptionEUnotFoundTest() throws Exception {
    when(eUDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    try {
      datasetSnapshotService.getSnapshotsReleasedByIdEUDataset(1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATASET_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the snapshots released by id EU dataset exception D cnot found test.
   *
   * @return the snapshots released by id EU dataset exception D cnot found test
   * @throws Exception the exception
   */
  @Test(expected = EEAException.class)
  public void getSnapshotsReleasedByIdEUDatasetExceptionDCnotFoundTest() throws Exception {
    when(eUDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new EUDataset()));
    when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.empty());
    try {
      datasetSnapshotService.getSnapshotsReleasedByIdEUDataset(1L);
    } catch (EEAException e) {
      assertEquals(EEAErrorMessage.DATASET_NOTFOUND, e.getMessage());
      throw e;
    }
  }

  /**
   * Update snapshot EU release test.
   *
   * @throws Exception the exception
   */
  @Test
  public void updateSnapshotEUReleaseTest() throws Exception {
    when(snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    datasetSnapshotService.updateSnapshotEURelease(1L);
    Mockito.verify(snapshotRepository, times(1)).releaseEUActiveSnapshots(Mockito.any());
  }

  /**
   * Gets the releases reporting success test.
   *
   * @return the releases reporting success test
   * @throws Exception the exception
   */
  @Test
  public void getReleasesReportingSuccessTest() throws Exception {

    when(datasetService.getDatasetType(Mockito.anyLong())).thenReturn(DatasetTypeEnum.REPORTING);
    when(snapshotRepository.findByReportingDatasetIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("not equals", datasetSnapshotService.getReleases(1L), new ArrayList<>());
  }

  /**
   * Gets the releases data collection success test.
   *
   * @return the releases data collection success test
   * @throws Exception the exception
   */
  @Test
  public void getReleasesDataCollectionSuccessTest() throws Exception {
    when(datasetService.getDatasetType(Mockito.anyLong())).thenReturn(DatasetTypeEnum.COLLECTION);
    when(snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("not equals", datasetSnapshotService.getReleases(1L), new ArrayList<>());
  }

  /**
   * Gets the releases EU dataset success test.
   *
   * @return the releases EU dataset success test
   * @throws Exception the exception
   */
  @Test
  public void getReleasesEUDatasetSuccessTest() throws Exception {
    when(datasetService.getDatasetType(Mockito.anyLong())).thenReturn(DatasetTypeEnum.EUDATASET);
    when(eUDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new EUDataset()));
    when(dataCollectionRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(new DataCollection()));
    when(snapshotRepository.findByDataCollectionIdOrderByCreationDateDesc(Mockito.any()))
        .thenReturn(snapshots);
    when(releaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    assertEquals("not equals", datasetSnapshotService.getReleases(1L), new ArrayList<>());
  }


  @Test
  public void createReleaseSnapshotTest() throws EEAException {
    ReportingDataset dataset = new ReportingDataset();
    dataset.setId(1L);
    dataset.setDataProviderId(1L);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito
        .when(reportingDatasetRepository
            .findFirstByDataflowIdAndDataProviderIdOrderByIdAsc(Mockito.anyLong(), Mockito.any()))
        .thenReturn(dataset);
    Mockito.doNothing().when(validationControllerZuul).validateDataSetData(Mockito.any(),
        Mockito.anyBoolean());

    Mockito.when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(Arrays.asList(dataset));


    DataSetSchemaVO schema = new DataSetSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    table.setIdTableSchema("5ce524fad31fc52540abae73");
    schema.setTableSchemas(Arrays.asList(table));
    Mockito.when(schemaService.getDataSchemaByDatasetId(Mockito.anyBoolean(), Mockito.any()))
        .thenReturn(schema);
    Mockito.doNothing().when(reportingDatasetService).updateReportingDatasetMetabase(Mockito.any());
    Mockito.when(collaborationControllerZuul.createMessage(Mockito.anyLong(), Mockito.any()))
        .thenReturn(new MessageVO());
    datasetSnapshotService.createReleaseSnapshots(1L, 1L, true);
    Mockito.verify(validationControllerZuul, times(1)).validateDataSetData(Mockito.any(),
        Mockito.anyBoolean());
  }

  @Test
  public void releaseLocksRelatedToReleaseTest() throws EEAException {

    ReportingDataset dataset = new ReportingDataset();
    dataset.setId(1L);
    dataset.setDataProviderId(1L);
    Mockito.when(reportingDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(Arrays.asList(dataset));

    Mockito.doNothing().when(reportingDatasetService).updateReportingDatasetMetabase(Mockito.any());

    DataSetSchemaVO schema = new DataSetSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    table.setIdTableSchema("5ce524fad31fc52540abae73");
    schema.setTableSchemas(Arrays.asList(table));
    Mockito.when(schemaService.getDataSchemaByDatasetId(Mockito.anyBoolean(), Mockito.any()))
        .thenReturn(schema);

    datasetSnapshotService.releaseLocksRelatedToRelease(1L, 1L);
    Mockito.verify(lockService, times(10)).removeLockByCriteria(Mockito.any());
  }

}
