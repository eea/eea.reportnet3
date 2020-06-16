package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ForeignRelations;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.service.impl.DatasetMetabaseServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


/**
 * The Class DatasetMetabaseServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetMetabaseServiceTest {


  /**
   * The dataset metabase service.
   */
  @InjectMocks
  private DatasetMetabaseServiceImpl datasetMetabaseService;

  /**
   * The data set metabase repository.
   */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;


  /**
   * The data set metabase mapper.
   */
  @Mock
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /**
   * The record store controller zull.
   */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The reporting dataset repository.
   */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  /**
   * The design dataset repository.
   */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /**
   * The statistics repository.
   */
  @Mock
  private StatisticsRepository statisticsRepository;

  /**
   * The user management controller zuul.
   */
  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  /**
   * The resource management controller zuul.
   */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /**
   * The representative controller zuul.
   */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /**
   * The data collection repository.
   */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private ForeignRelationsRepository foreignRelationsRepository;

  @Mock
  private DatasetService datasetService;

  ForeignRelations foreignRelations;

  DataSetMetabase dataSetMetabase;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setId(1L);
    foreignRelations = new ForeignRelations();
    foreignRelations.setId(1L);
    foreignRelations.setIdDatasetDestination(dataSetMetabase);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the data set id by dataflow id.
   *
   * @return the data set id by dataflow id
   */
  @Test
  public void testGetDataSetIdByDataflowId() {
    when(dataSetMetabaseMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(dataSetMetabaseRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong());
    assertEquals("failed assertion", new ArrayList<>(),
        datasetMetabaseService.getDataSetIdByDataflowId(Mockito.anyLong()));
  }


  /**
   * Test create empty dataset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCreateEmptyDataset() throws Exception {
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");

    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any(), Mockito.any());
    Mockito.when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(dataprovider);
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(1L);
    Mockito.when(reportingDatasetRepository.save(Mockito.any())).thenReturn(reporting);
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    representative.setProviderAccount("test@reportnet.net");
    datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.REPORTING, "",
        "5d0c822ae1ccd34cfcd97e20", 1L, null, Arrays.asList(representative), 0);

    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any());

  }

  /**
   * Creates the empty dataset exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createEmptyDatasetExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(kafkaSenderUtils).releaseNotificableKafkaEvent(
        EventType.ADD_DATACOLLECTION_COMPLETED_EVENT, null, NotificationVO.builder()
            .user((String) ThreadPropertiesManager.getVariable("user")).dataflowId(1L).build());
    RepresentativeVO representative = new RepresentativeVO();
    representative.setDataProviderId(1L);
    representative.setProviderAccount("test@reportnet.net");
    try {
      datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.REPORTING, "datasetName",
          (new ObjectId()).toString(), 1L, null, new ArrayList<>(), 0);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the empty dataset exception null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createEmptyDatasetExceptionNullTest() throws EEAException {
    try {
      datasetMetabaseService.createEmptyDataset(null, "datasetName", (new ObjectId()).toString(),
          1L, null, new ArrayList<>(), 0);
    } catch (EEAException e) {
      assertEquals("createEmptyDataset: Bad arguments", e.getMessage());
      throw e;
    }
  }

  /**
   * Find dataset metabase.
   *
   * @throws Exception the exception
   */
  @Test
  public void findDatasetMetabase() throws Exception {
    when(dataSetMetabaseRepository.findById(Mockito.eq(1l)))
        .thenReturn(Optional.of(new DataSetMetabase()));

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    when(dataSetMetabaseMapper.entityToClass(Mockito.any(DataSetMetabase.class)))
        .thenReturn(datasetMetabaseVO);
    when(designDatasetRepository.existsById(Mockito.eq(1l))).thenReturn(true);
    DataSetMetabaseVO result = datasetMetabaseService.findDatasetMetabase(1l);
    Mockito.verify(dataSetMetabaseRepository, times(1)).findById(Mockito.anyLong());
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getDatasetTypeEnum());
    Assert.assertEquals(DatasetTypeEnum.DESIGN, result.getDatasetTypeEnum());
  }

  /**
   * Creates the empty dataset test.
   *
   * @throws EEAException the EEA exception
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test
  public void createEmptyDatasetTest()
      throws EEAException, InterruptedException, ExecutionException {
    Mockito.when(designDatasetRepository.save(Mockito.any())).thenReturn(null);
    assertNull("failed assertion", datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
        "datasetName", (new ObjectId()).toString(), 1L, null, null, 0).get());
  }

  /**
   * Update dataset name test 1.
   */
  @Test
  public void updateDatasetNameTest1() {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    Mockito.when(dataSetMetabaseRepository.save(Mockito.any())).thenReturn(null);
    Assert.assertTrue(datasetMetabaseService.updateDatasetName(1L, "datasetName"));
  }

  /**
   * Update dataset name test 2.
   */
  @Test
  public void updateDatasetNameTest2() {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Assert.assertFalse(datasetMetabaseService.updateDatasetName(1L, ""));
  }

  /**
   * Delete design dataset test.
   */
  @Test
  public void deleteDesignDatasetTest() {
    datasetMetabaseService.deleteDesignDataset(1L);
    Mockito.verify(dataSetMetabaseRepository, times(1)).deleteNativeDataset(Mockito.anyLong());
  }

  /**
   * Test get statistics success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetStatisticsSuccess() throws Exception {
    datasetMetabaseService.getStatistics(1L);
    Mockito.verify(statisticsRepository, times(1)).findStatisticsByIdDataset(Mockito.any());
  }

  /**
   * Test get statistics success 2.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetStatisticsSuccess2() throws Exception {

    List<Statistics> stats = new ArrayList<>();
    Statistics stat = new Statistics();
    ReportingDataset dataset = new ReportingDataset();
    dataset.setId(1L);
    stat.setDataset(dataset);
    stat.setStatName("test");
    stat.setValue("0");
    stat.setIdTableSchema("idTableSchema");
    stats.add(stat);
    when(statisticsRepository.findStatisticsByIdDataset(Mockito.any())).thenReturn(stats);
    datasetMetabaseService.getStatistics(1L);
    Mockito.verify(statisticsRepository, times(1)).findStatisticsByIdDataset(Mockito.any());
  }

  /**
   * Test global statistics success.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGlobalStatisticsSuccess() throws Exception {

    List<Statistics> stats = new ArrayList<>();
    Statistics stat = new Statistics();
    ReportingDataset dataset = new ReportingDataset();
    dataset.setId(1L);
    stat.setDataset(dataset);
    stat.setStatName("test");
    stat.setValue("0");
    stat.setIdTableSchema("idTableSchema");
    stats.add(stat);

    when(statisticsRepository.findStatisticsByIdDatasetSchema(Mockito.any())).thenReturn(stats);

    datasetMetabaseService.getGlobalStatistics("5ce524fad31fc52540abae73");
    Mockito.verify(statisticsRepository, times(1)).findStatisticsByIdDatasetSchema(Mockito.any());
  }

  /**
   * Test set entity property.
   *
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Test
  public void testSetEntityProperty() throws InstantiationException, IllegalAccessException {
    StatisticsVO stats = new StatisticsVO();
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    assertTrue("failed assertion",
        datasetMetabaseService.setEntityProperty(instance, "idDataSetSchema", "0sdferf"));
  }

  /**
   * Test set entity property 2.
   *
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Test
  public void testSetEntityProperty2() throws InstantiationException, IllegalAccessException {
    StatisticsVO stats = new StatisticsVO();
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    assertTrue("failed assertion",
        datasetMetabaseService.setEntityProperty(instance, "datasetErrors", "false"));
  }

  /**
   * Creates the group and add user test.
   */
  @Test
  public void createGroupAndAddUserTest() {

    RepresentativeVO representative = new RepresentativeVO();
    representative.setProviderAccount("test@reportnet.net");
    representative.setDataProviderId(1L);
    Map<Long, String> mapTest = new HashMap<>();
    mapTest.put(1L, "test@reportnet.net");
    datasetMetabaseService.createGroupProviderAndAddUser(mapTest, 1L);

    Mockito.verify(resourceManagementControllerZuul, times(1)).createResources(Mockito.any());
  }

  /**
   * Creates the group DC and add user test.
   */
  @Test
  public void createGroupDCAndAddUserTest() {
    Mockito.doNothing().when(resourceManagementControllerZuul).createResource(Mockito.any());
    Mockito.doNothing().when(userManagementControllerZuul).addUserToResource(Mockito.any(),
        Mockito.any());
    datasetMetabaseService.createGroupDcAndAddUser(1L);
    Mockito.verify(userManagementControllerZuul, times(1)).addUserToResource(Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the empty DC test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyDCTest() throws EEAException {
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");

    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any(), Mockito.any());
    datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.COLLECTION, "testName",
        "5d0c822ae1ccd34cfcd97e20", 1L, new Date(), new ArrayList<>(), 0);
    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void findDatasetSchemaIdByIdTest() {
    Mockito.when(dataSetMetabaseRepository.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5ce524fad31fc52540abae73");
    Assert.assertEquals("5ce524fad31fc52540abae73",
        datasetMetabaseService.findDatasetSchemaIdById(1L));
  }

  @Test
  public void testAddForeignRelation() {
    Mockito.when(foreignRelationsRepository.save(Mockito.any())).thenReturn(new ForeignRelations());
    datasetMetabaseService.addForeignRelation(1L, 1L, "5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void testDeleteForeignRelation() {
    Mockito.doNothing().when(foreignRelationsRepository)
        .deleteFKByOriginDestinationAndPkAndIdFkOrigin(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any());
    datasetMetabaseService.deleteForeignRelation(1L, 1L, "5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1))
        .deleteFKByOriginDestinationAndPkAndIdFkOrigin(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any());
  }

  @Test
  public void testGetDatasetDestinationForeignRelation() {
    datasetMetabaseService.getDatasetDestinationForeignRelation(1L, "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1))
        .findDatasetDestinationByOriginAndPk(Mockito.any(), Mockito.any());
  }

  /**
   * Gets the dataset type enum return design test.
   *
   * @return the dataset type enum return design test
   */
  @Test
  public void getDatasetTypeEnumReturnDesignTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.DESIGN, datasetMetabaseService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return reporting test.
   *
   * @return the dataset type enum return reporting test
   */
  @Test
  public void getDatasetTypeEnumReturnReportingTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.REPORTING, datasetMetabaseService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return collection test.
   *
   * @return the dataset type enum return collection test
   */
  @Test
  public void getDatasetTypeEnumReturnCollectionTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(true);
    Assert.assertEquals(DatasetTypeEnum.COLLECTION, datasetMetabaseService.getDatasetType(1L));
  }

  /**
   * Gets the dataset type enum return null test.
   *
   * @return the dataset type enum return null test
   */
  @Test
  public void getDatasetTypeEnumReturnNullTest() {
    Mockito.when(designDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(reportingDatasetRepository.existsById(Mockito.any())).thenReturn(false);
    Mockito.when(dataCollectionRepository.existsById(Mockito.any())).thenReturn(false);
    Assert.assertNull(datasetMetabaseService.getDatasetType(1L));
  }

  @Test
  public void getIntegrityDatasetIdNullTest() {
    Mockito.when(foreignRelationsRepository.findFirstByIdDatasetOrigin_idAndIdPkAndIdFkOrigin(
        Mockito.anyLong(), Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
    Assert.assertNull(datasetMetabaseService.getIntegrityDatasetId(1L, "1", "1"));
  }

  @Test
  public void getIntegrityDatasetIdTest() {

    Mockito.when(foreignRelationsRepository.findFirstByIdDatasetOrigin_idAndIdPkAndIdFkOrigin(
        Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Optional.of(foreignRelations));
    Assert.assertEquals((Long) 1L, datasetMetabaseService.getIntegrityDatasetId(1L, "1", "1"));
  }

  @Test
  public void createForeignRelationshipTest() {
    datasetMetabaseService.createForeignRelationship(1L, 1L, "5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updateForeignRelationshipFoundOlderTest() {
    Mockito
        .when(foreignRelationsRepository
            .findFirstByIdDatasetOrigin_idAndIdDatasetDestination_idAndIdPkAndIdFkOrigin(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any()))
        .thenReturn(Optional.of(foreignRelations));
    datasetMetabaseService.updateForeignRelationship(1L, 1L, "5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updateForeignRelationshipTest() {
    datasetMetabaseService.updateForeignRelationship(1L, 1L, "5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(foreignRelationsRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void getDatasetIdByDatasetSchemaIdAndDataProviderIdNullTest() {
    Mockito.when(dataSetMetabaseRepository.findFirstByDatasetSchemaAndDataProviderId(Mockito.any(),
        Mockito.anyLong())).thenReturn(Optional.empty());
    Assert
        .assertNull(datasetMetabaseService.getDatasetIdByDatasetSchemaIdAndDataProviderId("1", 1L));
  }

  @Test
  public void getDatasetIdByDatasetSchemaIdAndDataProviderIdTest() {
    Mockito.when(dataSetMetabaseRepository.findFirstByDatasetSchemaAndDataProviderId(Mockito.any(),
        Mockito.anyLong())).thenReturn(Optional.of(dataSetMetabase));
    Assert.assertEquals((Long) 1L,
        datasetMetabaseService.getDatasetIdByDatasetSchemaIdAndDataProviderId("1", 1L));
  }
}
