package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.dataset.validate.commands.ValidationSchemaIntegrityCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
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

  /**
   * The table mapper.
   */
  @Mock
  private TableSchemaMapper tableSchemaMapper;

  /**
   * The field schema no rules mapper.
   */
  @Mock
  private FieldSchemaNoRulesMapper fieldSchemaNoRulesMapper;

  /**
   * The resource management controller zull.
   */
  @Mock
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /**
   * The user management controller zull.
   */
  @Mock
  private UserManagementControllerZull userManagementControllerZull;

  /**
   * The record store controller zull.
   */
  @Mock
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The design dataset repository.
   */
  @Mock
  private DesignDatasetRepository designDatasetRepository;

  /**
   * The table schema.
   */
  @Mock
  private Document tableSchema;

  /**
   * The field schema.
   */
  @Mock
  private Document fieldSchema;

  /**
   * The field schema VO.
   */
  @Mock
  private FieldSchemaVO fieldSchemaVO;

  /**
   * The table schema VO.
   */
  @Mock
  private TableSchemaVO tableSchemaVO;

  @Spy
  private List<ValidationSchemaCommand> validationCommands = new ArrayList<>();

  @Mock
  private ValidationSchemaIntegrityCommand command;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("user", "user");
    MockitoAnnotations.initMocks(this);
    validationCommands.add(command);
  }



  /**
   * Test find data schema by id.
   */
  @Test
  public void testFindDataSchemaById() {
    DataSetSchema schema = new DataSetSchema();
    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(dataSchemaMapper.entityToClass(schema)).thenReturn(schemaVO);
    assertEquals("Not equals", schemaVO,
        dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73"));
  }

  /**
   * Test find data schema by data flow.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindDataSchemaByDatasetId() throws EEAException {
    DataSetSchema dataSetSchema = new DataSetSchema();
    // dataSetSchema.setRuleDataSet(new ArrayList<>());
    DataSetMetabase metabase = new DataSetMetabase();
    metabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(metabase));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(dataSetSchema);
    DataSetSchemaVO value = new DataSetSchemaVO();
    // value.setRuleDataSet(new ArrayList<>());
    Mockito.doReturn(value).when(dataSchemaMapper).entityToClass(Mockito.any(DataSetSchema.class));
    assertEquals(value, dataSchemaServiceImpl.getDataSchemaByDatasetId(true, 1L));

  }

  /**
   * Test find data schema by data flow no rules.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testFindDataSchemaByDataFlowNoRules() throws EEAException {
    DataSetSchema dataSetSchema = new DataSetSchema();
    // dataSetSchema.setRuleDataSet(new ArrayList<>());
    DataSetMetabase metabase = new DataSetMetabase();
    metabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(metabase));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(dataSetSchema);
    DataSetSchemaVO value = new DataSetSchemaVO();
    Mockito.doReturn(value).when(noRulesDataSchemaMapper)
        .entityToClass(Mockito.any(DataSetSchema.class));
    DataSetSchemaVO result = dataSchemaServiceImpl.getDataSchemaByDatasetId(false, 1L);
    Assert.assertNotNull(result);
    // Assert.assertNull(result.getRuleDataSet());
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
    DataSetSchemaVO dataschema = new DataSetSchemaVO();
    when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(new DataSetSchema()));
    when(dataSchemaMapper.entityToClass((DataSetSchema) Mockito.any())).thenReturn(dataschema);
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(new DesignDataset()));
    assertEquals("not equals", dataschema,
        dataSchemaServiceImpl.getDataSchemaById("5ce524fad31fc52540abae73"));
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
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = new ArrayList<>();
    listaTables.add(table);
    schema.setTableSchemas(listaTables);

    DataSetSchema schema2 = new DataSetSchema();
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
    Assert.assertNotNull(dataSchemaServiceImpl.createEmptyDataSetSchema(1L));
  }

  /**
   * Creates the empty data set schema exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createEmptyDataSetSchemaException() throws EEAException {
    Mockito.when(dataFlowControllerZuul.findById(Mockito.any())).thenReturn(null);
    dataSchemaServiceImpl.createEmptyDataSetSchema(1L);
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
    Mockito.doNothing().when(userManagementControllerZull).addUserToResource(Mockito.any(),
        Mockito.any());
    dataSchemaServiceImpl.createGroupAndAddUser(1L);
    Mockito.verify(userManagementControllerZull, times(1)).addUserToResource(Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete group test.
   */
  @Test
  public void deleteGroupTest() {
    dataSchemaServiceImpl.deleteGroup(1L);
    Mockito.verify(resourceManagementControllerZull, times(1)).deleteResourceByName(Mockito.any());
  }

  /**
   * Test replace schema.
   */
  @Test
  public void testReplaceSchema() {
    DataSetSchema schema = new DataSetSchema();
    Mockito.doNothing().when(schemasRepository).deleteDatasetSchemaById(Mockito.any());
    when(schemasRepository.save(Mockito.any())).thenReturn(schema);
    doNothing().when(recordStoreControllerZull).restoreSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    dataSchemaServiceImpl.replaceSchema("1L", schema, 1L, 1L);
    verify(schemasRepository, times(1)).save(Mockito.any());
  }

  /**
   * Creates the field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest1() throws EEAException {
    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertNotNull(dataSchemaServiceImpl.createFieldSchema("<id>", new FieldSchemaVO()));
  }

  /**
   * Creates the field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest2() throws EEAException {
    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertEquals("", dataSchemaServiceImpl.createFieldSchema("<id>", new FieldSchemaVO()));
  }

  /**
   * Creates the field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createFieldSchemaTest3() throws EEAException {
    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);
    dataSchemaServiceImpl.createFieldSchema("<id>", new FieldSchemaVO());
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
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    Mockito.when(fieldSchemaVO.getType()).thenReturn(TypeData.NUMBER);
    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(TypeData.TEXT.getValue());
    Mockito.when(fieldSchemaVO.getDescription()).thenReturn("description");
    Mockito.when(fieldSchemaVO.getName()).thenReturn("name");
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    Assert.assertEquals("NUMBER", dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO));
  }

  /**
   * Update field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest2() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    Mockito.when(fieldSchemaVO.getType()).thenReturn(TypeData.TEXT);
    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(TypeData.TEXT.getValue());
    Mockito.when(fieldSchemaVO.getDescription()).thenReturn(null);
    Mockito.when(fieldSchemaVO.getName()).thenReturn(null);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertNull(dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO));
  }

  /**
   * Update field schema test 3.
   */
  @Test
  public void updateFieldSchemaTest3() {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    try {
      dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.FIELD_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update field schema test 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest4() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    try {
      dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.FIELD_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update field schema test 5.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest5() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);
    try {
      dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getCause().getClass());
    }
  }

  /**
   * Delete table schema test.
   *
   * @throws EEAException the EEA exception
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
    schema.setIdDataFlow(1L);
    List<TableSchema> listaTables = null;
    schema.setTableSchemas(listaTables);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema(id.toString());
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    dataSchemaServiceImpl.deleteTableSchema(id.toString(), id.toString());
  }

  /**
   * Update table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest1() throws EEAException {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    try {
      dataSchemaServiceImpl.updateTableSchema(new ObjectId().toString(), new TableSchemaVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.TABLE_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest2() throws EEAException {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchemaVO.getDescription()).thenReturn("description");
    Mockito.when(tableSchemaVO.getNameTableSchema()).thenReturn("nameTableSchema");
    Mockito.when(tableSchema.put(Mockito.any(), Mockito.any())).thenReturn(null);
    Mockito.when(schemasRepository.updateTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    dataSchemaServiceImpl.updateTableSchema(new ObjectId().toString(), tableSchemaVO);
    Mockito.verify(schemasRepository, times(1)).updateTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Update table schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest3() throws EEAException {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchemaVO.getDescription()).thenReturn(null);
    Mockito.when(tableSchemaVO.getNameTableSchema()).thenReturn(null);
    Mockito.when(schemasRepository.updateTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    try {
      dataSchemaServiceImpl.updateTableSchema(new ObjectId().toString(), tableSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.TABLE_NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Update table schema test 4.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest4() throws EEAException {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);
    try {
      dataSchemaServiceImpl.updateTableSchema("fail", tableSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getCause().getClass());
    }
  }

  /**
   * Creates the table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaTest() throws EEAException {
    Mockito.when(tableSchemaMapper.classToEntity(Mockito.any(TableSchemaVO.class)))
        .thenReturn(new TableSchema());
    dataSchemaServiceImpl.createTableSchema(new ObjectId().toString(), new TableSchemaVO(), 1L);
    Mockito.verify(schemasRepository, times(1)).insertTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Order table schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest1() throws EEAException {
    when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertFalse(dataSchemaServiceImpl.orderTableSchema("", "", 1));
  }

  /**
   * Order table schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest2() throws EEAException {
    doNothing().when(schemasRepository).deleteTableSchemaById(Mockito.any());
    when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    when(schemasRepository.insertTableInPosition(Mockito.anyString(), Mockito.any(),
        Mockito.anyInt())).thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(dataSchemaServiceImpl.orderTableSchema("", "", 1));
  }

  /**
   * Order table schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderTableSchemaTest3() throws EEAException {
    doNothing().when(schemasRepository).deleteTableSchemaById(Mockito.any());
    when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    when(schemasRepository.insertTableInPosition(Mockito.anyString(), Mockito.any(),
        Mockito.anyInt())).thenReturn(UpdateResult.acknowledged(2L, 0L, null));
    Assert.assertFalse(dataSchemaServiceImpl.orderTableSchema("", "", 1));
  }

  /**
   * Order field schema test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest1() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito
        .when(
            schemasRepository.insertFieldInPosition(Mockito.any(), Mockito.any(), Mockito.anyInt()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(dataSchemaServiceImpl.orderFieldSchema("", "", 1));
  }

  /**
   * Order field schema test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest2() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito
        .when(
            schemasRepository.insertFieldInPosition(Mockito.any(), Mockito.any(), Mockito.anyInt()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(dataSchemaServiceImpl.orderFieldSchema("", "", 1));
  }

  /**
   * Order field schema test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void orderFieldSchemaTest3() throws EEAException {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertFalse(dataSchemaServiceImpl.orderFieldSchema("", "", 1));
  }

  /**
   * Gets the dataset schema id test.
   *
   * @return the dataset schema id test
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getDatasetSchemaIdTest() throws EEAException {
    when(dataSetMetabaseRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    dataSchemaServiceImpl.getDatasetSchemaId(1L);
  }

  /**
   * Gets the dataset schema id success test.
   *
   * @return the dataset schema id success test
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDatasetSchemaIdSuccessTest() throws EEAException {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setDatasetSchema("schema");
    when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));
    assertEquals("schema", dataSchemaServiceImpl.getDatasetSchemaId(1L));
  }

  /**
   * Update dataset schema description test.
   */
  @Test
  public void updateDatasetSchemaDescriptionTest1() {
    Mockito.when(schemasRepository.updateDatasetSchemaDescription(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(dataSchemaServiceImpl.updateDatasetSchemaDescription("<id>", "description"));
  }

  /**
   * Update dataset schema description test 2.
   */
  @Test
  public void updateDatasetSchemaDescriptionTest2() {
    Mockito.when(schemasRepository.updateDatasetSchemaDescription(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(dataSchemaServiceImpl.updateDatasetSchemaDescription("<id>", "description"));
  }

  @Test
  public void getTableSchemaNameTest1() {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchema.get(Mockito.any())).thenReturn("nameTableSchema");
    Assert.assertEquals("nameTableSchema",
        dataSchemaServiceImpl.getTableSchemaName("datasetSchemaId", "tableSchemaId"));
  }

  @Test
  public void getTableSchemaNameTest2() {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertNull(dataSchemaServiceImpl.getTableSchemaName("datasetSchemaId", "tableSchemaId"));
  }

  @Test
  public void validateSchemaTest() {

    Assert.assertFalse(dataSchemaServiceImpl.validateSchema("5ce524fad31fc52540abae73"));
  }
}
