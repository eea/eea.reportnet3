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
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ForeignRelationsRepository;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.service.impl.DataCollectionServiceImpl;
import org.eea.dataset.service.model.FKDataCollection;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
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

/** The Class DataCollectionServiceTest. */
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

  /** The dataflow controller zuul. */
  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The lock service. */
  @Mock
  private LockService lockService;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The resource management controller zuul. */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZuul;

  /** The design dataset service. */
  @Mock
  private DesignDatasetService designDatasetService;

  /** The representative controller zuul. */
  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The user management controller zuul. */
  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  /** The record store controller zull. */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The metabase data source. */
  @Mock
  private DataSource metabaseDataSource;

  /** The connection. */
  @Mock
  private Connection connection;

  /** The statement. */
  @Mock
  private Statement statement;

  /** The result set. */
  @Mock
  private ResultSet resultSet;

  /** The dataset schema service. */
  @Mock
  private DatasetSchemaService datasetSchemaService;

  /** The foreign relations repository. */
  @Mock
  private ForeignRelationsRepository foreignRelationsRepository;

  /** The rules controller zuul. */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /** The design dataset repository. */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Gets the data collection id by dataflow id test.
   *
   * @return the data collection id by dataflow id test
   */
  @Test
  public void getDataCollectionIdByDataflowIdTest() {
    dataCollectionService.getDataCollectionIdByDataflowId(Mockito.anyLong());
    Mockito.verify(dataCollectionRepository, times(1)).findByDataflowId(Mockito.any());
  }

  /**
   * Gets the dataflow status design test.
   *
   * @return the dataflow status design test
   */
  @Test
  public void getDataflowStatusDesignTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(TypeStatusEnum.DESIGN, dataCollectionService.getDataflowStatus(1L));
  }

  /**
   * Gets the dataflow status draft test.
   *
   * @return the dataflow status draft test
   */
  @Test
  public void getDataflowStatusDraftTest() {
    DataFlowVO dataflowVO = new DataFlowVO();
    dataflowVO.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(dataflowVO);
    Assert.assertEquals(TypeStatusEnum.DRAFT, dataCollectionService.getDataflowStatus(1L));
  }

  /**
   * Gets the dataflow status null test.
   *
   * @return the dataflow status null test
   */
  @Test
  public void getDataflowStatusNullTest() {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(null);
    Assert.assertNull(dataCollectionService.getDataflowStatus(1L));
  }

  /**
   * Gets the dataflow status exception test.
   *
   * @return the dataflow status exception test
   */
  @Test
  public void getDataflowStatusExceptionTest() {
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.any()))
        .thenThrow(new RuntimeException());
    Assert.assertNull(dataCollectionService.getDataflowStatus(1L));
  }

  /**
   * Undo data collection creation test.
   *
   * @throws EEAException the EEA exception
   */
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
        new ArrayList<>(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)), 1L, true);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Undo data collection creation test EEA exception path.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void undoDataCollectionCreationTestEEAExceptionPath() throws EEAException {
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.doThrow(EEAException.class).when(kafkaSenderUtils)
        .releaseNotificableKafkaEvent(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.doNothing().when(dataCollectionRepository).deleteDatasetById(Mockito.any());
    Mockito.doNothing().when(dataflowControllerZuul).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
    dataCollectionService.undoDataCollectionCreation(new ArrayList<>(), 1L, false);
    Mockito.verify(dataflowControllerZuul, times(1)).updateDataFlowStatus(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Update data collection test EEA exception path.
   *
   * @throws SQLException the SQL exception
   */
  @Test
  public void updateDataCollectionTestEEAExceptionPath() throws SQLException {
    List<DesignDatasetVO> designs = new ArrayList<>();
    List<RepresentativeVO> representatives = new ArrayList<>();
    DesignDatasetVO design = new DesignDatasetVO();
    RepresentativeVO representative = new RepresentativeVO();
    design.setDataSetName("datasetName_");
    design.setDatasetSchema("datasetSchema_");
    representative.setId(1L);
    representative.setProviderAccount("providerAccount_");
    representative.setHasDatasets(false);
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
    dataCollectionService.updateDataCollection(1L);
    Mockito.verify(connection, times(1)).rollback();
  }

  /**
   * Creates the empty data collection test.
   *
   * @throws SQLException the SQL exception
   */
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
    representative.setHasDatasets(false);
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
    Mockito.when(designDatasetRepository.findByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(rulesControllerZuul.findRuleSchemaByDatasetId(Mockito.any()))
        .thenReturn(new RulesSchemaVO());
    Mockito.doNothing().when(recordStoreControllerZull).createSchemas(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
    Mockito.when(datasetSchemaService.getReferencedFieldsBySchema(Mockito.any()))
        .thenReturn(new ArrayList<>());
    dataCollectionService.createEmptyDataCollection(1L, new Date());
    Mockito.verify(recordStoreControllerZull, times(1)).createSchemas(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean());
  }

  /**
   * Creates the empty data collection test SQL exception path.
   *
   * @throws SQLException the SQL exception
   * @throws EEAException the EEA exception
   */
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
    representative.setHasDatasets(false);
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

  /**
   * Creates the empty data collection test EEA exception path.
   *
   * @throws SQLException the SQL exception
   */
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
    representative.setHasDatasets(false);
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

  /**
   * Test add foreign relations from new reportings.
   */
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
