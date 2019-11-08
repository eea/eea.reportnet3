package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class DatasetSchemaServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasetSchemaServiceTest {

  /**
   * The schemas repository.
   */
  @Mock
  private SchemasRepository schemasRepository;

  /**
   * The data set metabase table collection.
   */
  @Mock
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /**
   * The data flow controller zuul.
   */
  @Mock
  private DataFlowControllerZuul dataFlowControllerZuul;

  /**
   * The data set metabase repository.
   */
  @Mock
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /**
   * The data schema service impl.
   */
  @InjectMocks
  private DataschemaServiceImpl dataSchemaServiceImpl;

  /**
   * The data schema mapper.
   */
  @Mock
  private DataSchemaMapper dataSchemaMapper;

  /**
   * The data schema mapper.
   */
  @Mock
  private NoRulesDataSchemaMapper noRulesDataSchemaMapper;

  /** The table mapper. */
  @Mock
  private TableSchemaMapper tableMapper;

  /** The field schema no rules mapper. */
  @Mock
  private FieldSchemaNoRulesMapper fieldSchemaNoRulesMapper;

  /** The resource management controller zull. */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The user management controller zull. */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * The data set schema.
   */

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test create data schema.
   */
  @Test
  public void testCreateDataSchema() {
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    // header.setTableId(1L);
    header.setHeaderType(TypeData.TEXT);
    headers.add(header);
    List<TableCollection> tables = new ArrayList<>();
    TableCollection table = new TableCollection();
    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables.add(table);
    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }


  /**
   * Test create data schema integer.
   */
  @Test
  public void testCreateDataSchemaInteger() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.NUMBER);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }


  /**
   * Test create data schema boolean.
   */
  @Test
  public void testCreateDataSchemaBoolean() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.BOOLEAN);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }


  /**
   * Test create data schema coordinate lat.
   */
  @Test
  public void testCreateDataSchemaCoordinateLat() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.COORDINATE_LAT);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test create data schema coordinate long.
   */
  @Test
  public void testCreateDataSchemaCoordinateLong() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.COORDINATE_LONG);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test create data schema coordinate date.
   */
  @Test
  public void testCreateDataSchemaCoordinateDate() {

    List<TableCollection> tables2 = new ArrayList<>();

    TableCollection table = new TableCollection();
    TableHeadersCollection header = new TableHeadersCollection();
    List<TableHeadersCollection> headers = new ArrayList<>();

    header.setHeaderName("test");
    header.setId(1L);
    header.setHeaderType(TypeData.DATE);
    headers.add(header);

    table.setId(1L);
    table.setDataFlowId(1L);
    table.setDataSetId(1L);
    table.setTableName("test");
    table.setTableHeadersCollections(headers);
    tables2.add(table);

    when(dataSetMetabaseTableCollection.findAllByDataSetId(Mockito.any())).thenReturn(tables2);

    dataSchemaServiceImpl.createDataSchema(1L, 1L);

    Mockito.verify(schemasRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test find data schema by id.
   */
  @Test
  public void testFindDataSchemaById() {

    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertNull("fail", schemasRepository.findSchemaByIdFlow(1L));

  }

  /**
   * Test find data schema by data flow.
   */
  @Test
  public void testFindDataSchemaByDataFlow() {
    DataSetSchema dataSetSchema = new DataSetSchema();
    dataSetSchema.setRuleDataSet(new ArrayList<>());
    Mockito.when(schemasRepository.findSchemaByIdFlow(Mockito.any())).thenReturn(dataSetSchema);
    DataSetSchemaVO value = new DataSetSchemaVO();
    value.setRuleDataSet(new ArrayList<>());
    Mockito.doReturn(value).when(dataSchemaMapper).entityToClass(Mockito.any(DataSetSchema.class));
    DataSetSchemaVO result = dataSchemaServiceImpl.getDataSchemaByIdFlow(1L, true);
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getRuleDataSet());
    Mockito.verify(dataSchemaMapper, Mockito.times(1))
        .entityToClass(Mockito.any(DataSetSchema.class));
    Mockito.verify(noRulesDataSchemaMapper, Mockito.times(0))
        .entityToClass(Mockito.any(DataSetSchema.class));

  }

  /**
   * Test find data schema by data flow no rules.
   */
  @Test
  public void testFindDataSchemaByDataFlowNoRules() {
    DataSetSchema dataSetSchema = new DataSetSchema();
    dataSetSchema.setRuleDataSet(new ArrayList<>());
    Mockito.when(schemasRepository.findSchemaByIdFlow(Mockito.any())).thenReturn(dataSetSchema);
    DataSetSchemaVO value = new DataSetSchemaVO();
    Mockito.doReturn(value).when(noRulesDataSchemaMapper)
        .entityToClass(Mockito.any(DataSetSchema.class));
    DataSetSchemaVO result = dataSchemaServiceImpl.getDataSchemaByIdFlow(1L, false);
    Assert.assertNotNull(result);
    Assert.assertNull(result.getRuleDataSet());
    Mockito.verify(dataSchemaMapper, Mockito.times(0))
        .entityToClass(Mockito.any(DataSetSchema.class));
    Mockito.verify(noRulesDataSchemaMapper, Mockito.times(1))
        .entityToClass(Mockito.any(DataSetSchema.class));

  }

  /**
   * Test find data schema not null by id.
   */
  @Test
  public void testFindDataSchemaNotNullById() {
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(new DataSetSchema()));
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any()))
        .thenReturn(new DataSetSchemaVO());
    dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73");

    assertNull("fail", schemasRepository.findSchemaByIdFlow(1L));

  }


  /**
   * Test schema models.
   */
  @Test
  public void testSchemaModels() {

    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType(TypeData.TEXT);

    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType(TypeData.TEXT);

    assertEquals("Not equals", field, field2);

    RecordSchema record = new RecordSchema();
    record.setNameSchema("test");
    List<FieldSchema> listaFields = new ArrayList<>();
    listaFields.add(field);
    record.setFieldSchema(listaFields);

    RecordSchema record2 = new RecordSchema();
    record2.setNameSchema("test");
    record2.setFieldSchema(listaFields);

    assertEquals("Not equals", record, record2);

    TableSchema table = new TableSchema();
    table.setNameTableSchema("test");
    table.setRecordSchema(record);

    TableSchema table2 = new TableSchema();
    table2.setNameTableSchema("test");
    table2.setRecordSchema(record2);

    assertEquals("Not equals", table, table2);

    DataSetSchema schema = new DataSetSchema();
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);

    DataSetSchema schema2 = new DataSetSchema();
    schema2.setNameDataSetSchema("test");
    schema2.setIdDataFlow(1L);
    schema2.setTableSchemas(listaTables);

    assertEquals("Not equals", schema, schema2);
  }

  /**
   * Creates the empty data set schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyDataSetSchemaTest() throws EEAException {
    Mockito.when(dataFlowControllerZuul.findById(Mockito.any())).thenReturn(new DataFlowVO());
    Mockito.when(schemasRepository.save(Mockito.any())).thenReturn(null);
    Assert.assertNotNull(dataSchemaServiceImpl.createEmptyDataSetSchema(1L, "nameDataSetSchema"));
  }

  /**
   * Creates the empty data set schema exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
    Mockito.when(dataFlowControllerZuul.findById(Mockito.any())).thenReturn(null);
    dataSchemaServiceImpl.createEmptyDataSetSchema(1L, "nameDataSetSchema");
  }

  /**
   * Delete table schema test.
   *
   * @throws EEAException
   */
  @Test
  public void deleteTableSchemaTest() throws EEAException {
    ObjectId id = new ObjectId();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(id);
    table.setNameTableSchema("test");
    TableSchema table2 = new TableSchema();
    table2.setIdTableSchema(new ObjectId());
    table2.setNameTableSchema("test");
    DataSetSchema schema = new DataSetSchema();
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    listaTables.add(table2);
    schema.setTableSchemas(listaTables);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema(id.toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    dataSchemaServiceImpl.deleteTableSchema(id.toString(), id.toString());
    Mockito.verify(schemasRepository, times(1)).deleteTableSchemaById(Mockito.any());
  }

  /**
   * Delete table schema exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteTableSchemaExceptionTest() throws EEAException {
    ObjectId id = new ObjectId();
    DataSetSchema schema = new DataSetSchema();
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = null;
    schema.setTableSchemas(listaTables);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema(id.toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    dataSchemaServiceImpl.deleteTableSchema(id.toString(), id.toString());
  }

  /**
   * Update table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest() throws EEAException {
    ObjectId id = new ObjectId();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(id);
    table.setNameTableSchema("test");
    DataSetSchema schema = new DataSetSchema();
    schema.setNameDataSetSchema("test");
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema(id.toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    dataSchemaServiceImpl.updateTableSchema(id.toString(), tableVO);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
    Mockito.verify(schemasRepository, times(1)).deleteTableSchemaById(Mockito.any());
    Mockito.verify(schemasRepository, times(1)).insertTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Update table schema dataset null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateTableSchemaDatasetNullTest() throws EEAException {
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(null));
    dataSchemaServiceImpl.updateTableSchema(new ObjectId().toString(), null);
  }

  /**
   * Update table schema table null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateTableSchemaTableNullTest() throws EEAException {
    Mockito.when(schemasRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new DataSetSchema()));
    dataSchemaServiceImpl.updateTableSchema(new ObjectId().toString(), new TableSchemaVO());
  }

  /**
   * Creates the table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaTest() throws EEAException {
    Mockito.when(tableMapper.classToEntity(Mockito.any())).thenReturn(new TableSchema());
    dataSchemaServiceImpl.createTableSchema(new ObjectId().toString(), new TableSchemaVO(), 1L);
  }

  /**
   * Delete dataset schema test.
   */
  @Test
  public void deleteDatasetSchemaTest() {
    dataSchemaServiceImpl.deleteDatasetSchema(1L, "idTableSchema");
    Mockito.verify(schemasRepository, times(1)).deleteDatasetSchemaById(Mockito.any());
  }

  /**
   * Creates the group and add user test.
   */
  @Test
  public void createGroupAndAddUserTest() {
    Mockito.doNothing().when(resourceManagementControllerZull).createResource(Mockito.any());
    Mockito.doNothing().when(userManagementControllerZull).addContributorToResource(Mockito.any(),
        Mockito.any());
    dataSchemaServiceImpl.createGroupAndAddUser(1L);
    Mockito.verify(userManagementControllerZull, times(1)).addContributorToResource(Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest1() throws EEAException {
    UpdateResult updateResult = UpdateResult.acknowledged(1L, 1L, null);
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(updateResult);
    Assert.assertTrue(dataSchemaServiceImpl.deleteFieldSchema("datasetSchemaId", "fieldSchemaId"));
  }

  /**
   * Delete field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteFieldSchemaTest2() throws EEAException {
    UpdateResult updateResult = UpdateResult.acknowledged(1L, 0L, null);
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(updateResult);
    Assert.assertFalse(dataSchemaServiceImpl.deleteFieldSchema("datasetSchemaId", "fieldSchemaId"));
  }

  /**
   * Update field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest1() throws EEAException {
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setType(TypeData.TEXT);

    Assert.assertEquals("TEXT", dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO));
  }

  @Test
  public void updateFieldSchemaTest2() throws EEAException {
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));

    Assert.assertNull(dataSchemaServiceImpl.updateFieldSchema("<id>", new FieldSchemaVO()));
  }

  @Test(expected = EEAException.class)
  public void updateFieldSchemaTest3() throws EEAException {
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);

    Assert.assertNull(dataSchemaServiceImpl.updateFieldSchema("<id>", new FieldSchemaVO()));
  }

  /**
   * Delete group test.
   */
  @Test
  public void deleteGroupTest() {
    dataSchemaServiceImpl.deleteGroup(1L);
    Mockito.verify(resourceManagementControllerZull, times(1)).deleteResourceByName(Mockito.any());
  }

}
