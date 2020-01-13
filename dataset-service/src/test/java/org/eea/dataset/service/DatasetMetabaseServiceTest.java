package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMetabaseMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
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
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
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


/**
 * The Class DatasetMetabaseServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetMetabaseServiceTest {


  /** The dataset metabase service. */
  @InjectMocks
  private DatasetMetabaseServiceImpl datasetMetabaseService;

  /** The data set metabase repository. */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;


  /** The data set metabase mapper. */
  @Mock
  private DataSetMetabaseMapper dataSetMetabaseMapper;

  /** The record store controller zull. */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The reporting dataset repository. */
  @Mock
  private ReportingDatasetRepository reportingDatasetRepository;

  @Mock
  private DesignDatasetRepository designDatasetRepository;

  @Mock
  private StatisticsRepository statisticsRepository;

  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DataCollectionRepository dataCollectionRepository;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
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
    datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.REPORTING, "",
        "5d0c822ae1ccd34cfcd97e20", 1L, null, new RepresentativeVO(), 0);
    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void findDatasetMetabase() throws Exception {
    when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    datasetMetabaseService.findDatasetMetabase(Mockito.anyLong());
    Mockito.verify(dataSetMetabaseRepository, times(1)).findById(Mockito.anyLong());
  }

  @Test
  public void createEmptyDatasetTest() throws EEAException {
    Mockito.when(designDatasetRepository.save(Mockito.any())).thenReturn(null);
    datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.DESIGN, "datasetName",
        (new ObjectId()).toString(), 1L, null, null, 0);
  }

  @Test
  public void updateDatasetNameTest1() {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DataSetMetabase()));
    Mockito.when(dataSetMetabaseRepository.save(Mockito.any())).thenReturn(null);
    Assert.assertTrue(datasetMetabaseService.updateDatasetName(1L, "datasetName"));
  }

  @Test
  public void updateDatasetNameTest2() {
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    Assert.assertFalse(datasetMetabaseService.updateDatasetName(1L, ""));
  }

  @Test
  public void deleteDesignDatasetTest() {
    datasetMetabaseService.deleteDesignDataset(1L);
    Mockito.verify(dataSetMetabaseRepository, times(1)).deleteById(Mockito.anyLong());
  }


  @Test
  public void testGetStatisticsSuccess() throws Exception {

    datasetMetabaseService.getStatistics(1L);
    Mockito.verify(statisticsRepository, times(1)).findStatisticsByIdDataset(Mockito.any());
  }



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

  @Test
  public void testSetEntityProperty() throws InstantiationException, IllegalAccessException {
    StatisticsVO stats = new StatisticsVO();
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    datasetMetabaseService.setEntityProperty(instance, "idDataSetSchema", "0sdferf");
  }

  @Test
  public void testSetEntityProperty2() throws InstantiationException, IllegalAccessException {
    StatisticsVO stats = new StatisticsVO();
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    datasetMetabaseService.setEntityProperty(instance, "datasetErrors", "false");
  }

  @Test
  public void createGroupAndAddUserTest() {
    Mockito.doNothing().when(resourceManagementControllerZuul).createResource(Mockito.any());
    Mockito.doNothing().when(userManagementControllerZuul).addUserToResource(Mockito.any(),
        Mockito.any());
    datasetMetabaseService.createGroupProviderAndAddUser(1L, "test@reportnet.net", 1L);
    Mockito.verify(userManagementControllerZuul, times(1)).addUserToResource(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void createGroupDCAndAddUserTest() {
    Mockito.doNothing().when(resourceManagementControllerZuul).createResource(Mockito.any());
    Mockito.doNothing().when(userManagementControllerZuul).addUserToResource(Mockito.any(),
        Mockito.any());
    datasetMetabaseService.createGroupDcAndAddUser(1L);
    Mockito.verify(userManagementControllerZuul, times(1)).addUserToResource(Mockito.any(),
        Mockito.any());
  }


  @Test
  public void createEmptyDCTest() throws EEAException {
    DataProviderVO dataprovider = new DataProviderVO();
    dataprovider.setLabel("test");

    doNothing().when(recordStoreControllerZull).createEmptyDataset(Mockito.any(), Mockito.any());
    datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.COLLECTION, "testName",
        "5d0c822ae1ccd34cfcd97e20", 1L, new Date(), new RepresentativeVO(), 0);
    Mockito.verify(recordStoreControllerZull, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any());

  }

}
