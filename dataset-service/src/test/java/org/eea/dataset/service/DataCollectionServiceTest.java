package org.eea.dataset.service;

import static org.mockito.Mockito.times;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataCollectionMapper;
import org.eea.dataset.persistence.metabase.domain.FKDataCollection;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.service.impl.DataCollectionServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
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
 * The Class DataCollectionServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionServiceTest {

  /** The data collection service. */
  @InjectMocks
  private DataCollectionServiceImpl dataCollectionService;

  /** The data collection repository. */
  @Mock
  private DataCollectionRepository dataCollectionRepository;

  /** The data collection mapper. */
  @Mock
  private DataCollectionMapper dataCollectionMapper;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private LockService lockService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  @Mock
  private DesignDatasetService designDatasetService;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  @Mock
  private DataSource metabaseDataSource;

  @Mock
  private Connection connection;

  @Mock
  private Statement statement;

  @Mock
  private ResultSet resultSet;

  @Mock
  private DatasetSchemaService datasetSchemaService;

  @Mock
  private ForeignRelationsRepository foreignRelationsRepository;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getDataCollectionIdByDataflowIdTest() {
    dataCollectionService.getDataCollectionIdByDataflowId(Mockito.anyLong());
    Mockito.verify(dataCollectionRepository, times(1)).findByDataflowId(Mockito.any());
  }

  @Test
  public void isDesignDataflowTest1() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(true, dataCollectionService.isDesignDataflow(1L));
  }

  @Test
  public void isDesignDataflowTest2() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(false, dataCollectionService.isDesignDataflow(1L));
  }

  @Test
  public void isDesignDataflowTest3() {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(null);
    Assert.assertEquals(false, dataCollectionService.isDesignDataflow(1L));
  }

  @Test
  public void undoDataCollectionCreationTest() throws EEAException {
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.doNothing().when(resourceManagementControllerZuul)
        .deleteResourceByDatasetId(Mockito.any());
    Mockito.doNothing().when(dataCollectionRepository).deleteDatasetById(Mockito.any());
    Mockito.doNothing().when(dataflowControllerZuul).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataCollectionService.undoDataCollectionCreation(
        new ArrayList<>(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)), 1L);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void undoDataCollectionCreationTestEEAExceptionPath() throws EEAException {
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(dataCollectionRepository).deleteDatasetById(Mockito.any());
    Mockito.doNothing().when(dataflowControllerZuul).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataCollectionService.undoDataCollectionCreation(new ArrayList<>(), 1L);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void createEmptyDataCollectionTest() throws SQLException {
    List<DesignDatasetVO> designs = new ArrayList<>();
    List<RepresentativeVO> representatives = new ArrayList<>();
    List<DataProviderVO> dataProviders = new ArrayList<>();
    DesignDatasetVO design = new DesignDatasetVO();
    RepresentativeVO representative = new RepresentativeVO();
    DataProviderVO dataProvider = new DataProviderVO();
    design.setDataSetName("datasetName_");
    design.setDatasetSchema("datasetSchema_");
    representative.setId(1L);
    representative.setProviderAccount("providerAccount_");
    representative.setDataProviderId(1L);
    dataProvider.setId(1L);
    dataProvider.setLabel("label");
    designs.add(design);
    representatives.add(representative);
    dataProviders.add(dataProvider);
    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(designs);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    Mockito.when(representativeControllerZuul.findDataProvidersByIds(Mockito.any()))
        .thenReturn(dataProviders);
    Mockito.when(metabaseDataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.createStatement()).thenReturn(statement);
    Mockito.doNothing().when(connection).setAutoCommit(Mockito.anyBoolean());
    Mockito.doNothing().when(statement).addBatch(Mockito.any());
    Mockito.when(statement.executeQuery(Mockito.any())).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);
    Mockito.when(statement.executeBatch()).thenReturn(null);
    Mockito.doNothing().when(resourceManagementControllerZuul).createResources(Mockito.any());
    Mockito.doNothing().when(userManagementControllerZuul)
        .addContributorsToResources(Mockito.any());
    Mockito.doNothing().when(recordStoreControllerZull).createSchemas(Mockito.any(), Mockito.any());
    Mockito.when(datasetSchemaService.getReferencedFieldsBySchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataCollectionService.createEmptyDataCollection(1L, new Date());
    Mockito.verify(recordStoreControllerZull, times(1)).createSchemas(Mockito.any(), Mockito.any());
  }

  @Test
  public void createEmptyDataCollectionTestSQLExceptionPath() throws SQLException, EEAException {
    List<DesignDatasetVO> designs = new ArrayList<>();
    List<RepresentativeVO> representatives = new ArrayList<>();
    DesignDatasetVO design = new DesignDatasetVO();
    RepresentativeVO representative = new RepresentativeVO();
    design.setDataSetName("datasetName_");
    design.setDatasetSchema("datasetSchema_");
    representative.setId(1L);
    representative.setProviderAccount("providerAccount_");
    designs.add(design);
    representatives.add(representative);
    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(designs);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    Mockito.when(metabaseDataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.createStatement()).thenReturn(statement);
    Mockito.doThrow(SQLException.class).when(connection).setAutoCommit(Mockito.anyBoolean());
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(connection).rollback();
    dataCollectionService.createEmptyDataCollection(1L, new Date());
    Mockito.verify(connection, times(1)).rollback();
  }

  @Test
  public void createEmptyDataCollectionTestEEAExceptionPath() throws SQLException {
    List<DesignDatasetVO> designs = new ArrayList<>();
    List<RepresentativeVO> representatives = new ArrayList<>();
    DesignDatasetVO design = new DesignDatasetVO();
    RepresentativeVO representative = new RepresentativeVO();
    design.setDataSetName("datasetName_");
    design.setDatasetSchema("datasetSchema_");
    representative.setId(1L);
    representative.setProviderAccount("providerAccount_");
    designs.add(design);
    representatives.add(representative);
    Mockito.when(designDatasetService.getDesignDataSetIdByDataflowId(Mockito.any()))
        .thenReturn(designs);
    Mockito.when(representativeControllerZuul.findRepresentativesByIdDataFlow(Mockito.any()))
        .thenReturn(representatives);
    Mockito.when(metabaseDataSource.getConnection()).thenReturn(connection);
    Mockito.when(connection.createStatement()).thenReturn(statement);
    Mockito.doNothing().when(connection).setAutoCommit(Mockito.anyBoolean());
    Mockito.doNothing().when(statement).addBatch(Mockito.any());
    Mockito.when(statement.executeQuery(Mockito.any())).thenReturn(resultSet);
    Mockito.when(resultSet.next()).thenReturn(true);
    Mockito.when(statement.executeBatch()).thenReturn(null);
    Mockito.doNothing().when(resourceManagementControllerZuul).createResources(Mockito.any());
    Mockito.doThrow(NullPointerException.class).when(userManagementControllerZuul)
        .addContributorsToResources(Mockito.any());
    Mockito.doNothing().when(resourceManagementControllerZuul)
        .deleteResourceByDatasetId(Mockito.any());
    Mockito.when(datasetSchemaService.getReferencedFieldsBySchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataCollectionService.createEmptyDataCollection(1L, new Date());
    Mockito.verify(connection, times(1)).rollback();
  }

  @Test
  public void testAddForeignRelationsFromNewReportings() {
    FKDataCollection fkData = new FKDataCollection();
    fkData.setIdDatasetOrigin(1L);
    fkData.setIdDatasetSchemaOrigin("5ce524fad31fc52540abae73");
    fkData.setRepresentative("France");
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    fkData.setFks(Arrays.asList(referenced));
    dataCollectionService.addForeignRelationsFromNewReportings(Arrays.asList(fkData));
    Mockito.verify(foreignRelationsRepository, times(1)).saveAll(Mockito.any());
  }
}
