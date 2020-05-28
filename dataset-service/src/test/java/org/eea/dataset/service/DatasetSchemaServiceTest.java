package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.UniqueConstraintMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.dataset.validate.commands.ValidationSchemaIntegrityCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
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

  /**
   * The validation commands.
   */
  @Spy
  private List<ValidationSchemaCommand> validationCommands = new ArrayList<>();

  /**
   * The command.
   */
  @Mock
  private ValidationSchemaIntegrityCommand command;

  /**
   * The rules controller zuul.
   */
  @Mock
  private RulesControllerZuul rulesControllerZuul;

  /** The rules controller. */
  @Mock
  private RulesController rulesController;

  /** The dataset service. */
  @Mock
  private DatasetService datasetService;

  /** The pk catalogue repository. */
  @Mock
  private PkCatalogueRepository pkCatalogueRepository;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The unique constraint repository. */
  @Mock
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The unique constraint mapper. */
  @Mock
  private UniqueConstraintMapper uniqueConstraintMapper;

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
    DataSetMetabase metabase = new DataSetMetabase();
    metabase.setDatasetSchema(new ObjectId().toString());
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(metabase));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(dataSetSchema);
    DataSetSchemaVO value = new DataSetSchemaVO();
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
    String codelistItems[] = {"Avila", "Burgos"};
    FieldSchema field = new FieldSchema();
    field.setHeaderName("test");
    field.setType(DataType.CODELIST);
    field.setCodelistItems(codelistItems);
    FieldSchema field2 = new FieldSchema();
    field2.setHeaderName("test");
    field2.setType(DataType.TEXT);

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
    doNothing().when(rulesControllerZuul).createEmptyRulesSchema(Mockito.any(), Mockito.any());
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
    dataSchemaServiceImpl.deleteDatasetSchema("idTableSchema");
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
    doNothing().when(rulesControllerZuul).deleteRuleHighLevelLike(Mockito.any(), Mockito.any());
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
    doNothing().when(rulesControllerZuul).deleteRuleHighLevelLike(Mockito.any(), Mockito.any());
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
    Mockito.when(fieldSchemaVO.getType()).thenReturn(DataType.NUMBER_DECIMAL);
    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.TEXT.getValue());
    Mockito.when(fieldSchemaVO.getDescription()).thenReturn("description");
    Mockito.when(fieldSchemaVO.getName()).thenReturn("name");
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    Assert.assertEquals(DataType.NUMBER_DECIMAL,
        dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO));
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
    Mockito.when(fieldSchemaVO.getType()).thenReturn(DataType.TEXT);
    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.TEXT.getValue());
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
   * Update field schema test 6.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest6() throws EEAException {
    String items[] = {"Avila", "Burgos"};
    FieldSchemaVO fielSchemaVO = new FieldSchemaVO();
    fielSchemaVO.setCodelistItems(items);
    fielSchemaVO.setType(DataType.CODELIST);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.CODELIST.getValue());

    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    Assert.assertEquals(DataType.CODELIST,
        dataSchemaServiceImpl.updateFieldSchema("<id>", fielSchemaVO));
  }

  /**
   * Update field schema test 7.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaTest7() throws EEAException {
    String items[] = {"Avila", "Burgos"};
    FieldSchemaVO fielSchemaVO = new FieldSchemaVO();
    fielSchemaVO.setCodelistItems(items);
    fielSchemaVO.setType(DataType.MULTISELECT_CODELIST);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);

    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.MULTISELECT_CODELIST.getValue());

    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    Assert.assertEquals(DataType.MULTISELECT_CODELIST,
        dataSchemaServiceImpl.updateFieldSchema("<id>", fielSchemaVO));
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
    Document documentRecord = new Document();
    ObjectId objIdRecord = new ObjectId();
    documentRecord.put("_id", objIdRecord);
    List<Document> docFieldSchmea = new ArrayList<>();
    Document documentField = new Document();
    documentField.put("_id", objIdRecord);
    docFieldSchmea.add(documentField);
    documentRecord.put("fieldSchemas", docFieldSchmea);

    Mockito.when(schemasRepository.findRecordSchema(id.toString(), id.toString()))
        .thenReturn(documentRecord);
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
    Mockito.when(tableSchemaVO.getReadOnly()).thenReturn(true);
    Mockito.when(tableSchemaVO.getToPrefill()).thenReturn(true);
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
    Mockito.when(tableSchemaVO.getReadOnly()).thenReturn(null);
    Mockito.when(tableSchemaVO.getToPrefill()).thenReturn(null);
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

  /**
   * Gets the table schema name test 1.
   *
   * @return the table schema name test 1
   */
  @Test
  public void getTableSchemaNameTest1() {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchema.get(Mockito.any())).thenReturn("nameTableSchema");
    Assert.assertEquals("nameTableSchema",
        dataSchemaServiceImpl.getTableSchemaName("datasetSchemaId", "tableSchemaId"));
  }

  /**
   * Gets the table schema name test 2.
   *
   * @return the table schema name test 2
   */
  @Test
  public void getTableSchemaNameTest2() {
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertNull(dataSchemaServiceImpl.getTableSchemaName("datasetSchemaId", "tableSchemaId"));
  }

  /**
   * Validate schema test.
   */
  @Test
  public void validateSchemaTest() {

    Assert.assertFalse(dataSchemaServiceImpl.validateSchema("5ce524fad31fc52540abae73"));
  }


  /**
   * Propagate rules after update type null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNullTest() throws EEAException {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");

    dataSchemaServiceImpl.propagateRulesAfterUpdateSchema("datasetSchemaId", fieldSchemaVO, null,
        1L);
    Mockito.verify(rulesControllerZuul, times(1)).existsRuleRequired(Mockito.any(), Mockito.any());
  }

  /**
   * Propagate rules after update type not null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNotNullTest() throws EEAException {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.doNothing().when(datasetService).updateFieldValueType(Mockito.anyLong(), Mockito.any(),
        Mockito.any());
    dataSchemaServiceImpl.propagateRulesAfterUpdateSchema("datasetSchemaId", fieldSchemaVO,
        DataType.NUMBER_DECIMAL, 1L);
    Mockito.verify(datasetService, times(1)).updateFieldValueType(Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  /**
   * Propagate rules after update type null not required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNullNotRequiredTest() throws EEAException {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");

    dataSchemaServiceImpl.propagateRulesAfterUpdateSchema("datasetSchemaId", fieldSchemaVO, null,
        1L);
    Mockito.verify(rulesControllerZuul, times(1)).deleteRuleRequired(Mockito.any(), Mockito.any());

  }

  /**
   * Propagate rules after update type not null not required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNotNullNotRequiredTest() throws EEAException {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");
    Mockito.doNothing().when(datasetService).updateFieldValueType(Mockito.anyLong(), Mockito.any(),
        Mockito.any());
    dataSchemaServiceImpl.propagateRulesAfterUpdateSchema("datasetSchemaId", fieldSchemaVO,
        DataType.NUMBER_DECIMAL, 1L);
    Mockito.verify(datasetService, times(1)).updateFieldValueType(Mockito.any(), Mockito.any(),
        Mockito.any());

  }

  /**
   * Test check pk allow update when is pk.
   */
  @Test
  public void testCheckPkAllowUpdateWhenIsPk() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    RecordSchemaVO recordVO = new RecordSchemaVO();
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    tableVO.setRecordSchema(recordVO);
    DataSetSchemaVO dsVO = new DataSetSchemaVO();
    dsVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    dsVO.setTableSchemas(Arrays.asList(tableVO));
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));

    DataSetSchema schema = new DataSetSchema();
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(dataSchemaMapper.entityToClass(schema)).thenReturn(dsVO);

    dataSchemaServiceImpl.checkPkAllowUpdate("5ce524fad31fc52540abae73", fieldSchemaVO);

    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Test check pk allow update when not pk.
   */
  @Test
  public void testCheckPkAllowUpdateWhenNotPk() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    RecordSchemaVO recordVO = new RecordSchemaVO();
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    tableVO.setRecordSchema(recordVO);
    DataSetSchemaVO dsVO = new DataSetSchemaVO();
    dsVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    dsVO.setTableSchemas(Arrays.asList(tableVO));
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));

    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);

    dataSchemaServiceImpl.checkPkAllowUpdate("5ce524fad31fc52540abae73", fieldSchemaVO);

    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }


  /**
   * Test check existing pk referenced.
   */
  @Test
  public void testCheckExistingPkReferenced() {

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));

    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);

    dataSchemaServiceImpl.checkExistingPkReferenced(fieldSchemaVO);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }

  /**
   * Test update pk catalogue non existing PK.
   */
  @Test
  public void testUpdatePkCatalogueNonExistingPK() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);

    dataSchemaServiceImpl.addToPkCatalogue(fieldSchemaVO);
    Mockito.verify(pkCatalogueRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test update pk catalogue existing PK.
   */
  @Test
  public void testUpdatePkCatalogueExistingPK() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);

    dataSchemaServiceImpl.addToPkCatalogue(fieldSchemaVO);
    Mockito.verify(pkCatalogueRepository, times(1)).save(Mockito.any());
  }

  /**
   * Test delete from pk catalogue.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeleteFromPkCatalogue() throws EEAException {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);

    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540abae73");
    doc.put("referencedField", referencedDoc);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);

    dataSchemaServiceImpl.deleteFromPkCatalogue(fieldSchemaVO);
    Mockito.verify(pkCatalogueRepository, times(2)).findByIdPk(Mockito.any());
  }

  /**
   * Test add foreign relation.
   */
  @Test
  public void testAddForeignRelation() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    DesignDataset design = new DesignDataset();
    design.setId(1L);

    Mockito.when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(design));
    Mockito.doNothing().when(datasetMetabaseService).addForeignRelation(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    dataSchemaServiceImpl.addForeignRelation(1L, fieldSchemaVO);
    Mockito.verify(datasetMetabaseService, times(1)).addForeignRelation(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }


  /**
   * Test delete foreign relation.
   */
  @Test
  public void testDeleteForeignRelation() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    DesignDataset design = new DesignDataset();
    design.setId(1L);

    Mockito.when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(design));
    Mockito.doNothing().when(datasetMetabaseService).deleteForeignRelation(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
    dataSchemaServiceImpl.deleteForeignRelation(1L, fieldSchemaVO);
    Mockito.verify(datasetMetabaseService, times(1)).deleteForeignRelation(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());
  }


  /**
   * Test update foreign relation.
   */
  @Test
  public void testUpdateForeignRelation() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    DesignDataset design = new DesignDataset();
    design.setId(1L);

    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540abae73");
    doc.put("referencedField", referencedDoc);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);

    dataSchemaServiceImpl.updateForeignRelation(1L, fieldSchemaVO, "5ce524fad31fc52540abae73");
    Mockito.verify(schemasRepository, times(1)).findFieldSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Test get field schema.
   */
  @Test
  public void testGetFieldSchema() {

    Document doc = new Document();
    doc.put("typeData", DataType.LINK.getValue());
    Document referencedDoc = new Document();
    referencedDoc.put("idDatasetSchema", "5ce524fad31fc52540abae73");
    referencedDoc.put("idPk", "5ce524fad31fc52540abae73");
    doc.put("referencedField", referencedDoc);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);

    dataSchemaServiceImpl.getFieldSchema("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
    Mockito.verify(schemasRepository, times(1)).findFieldSchema(Mockito.any(), Mockito.any());
  }


  /**
   * Test allow delete schema.
   */
  @Test
  public void testAllowDeleteSchema() {
    DataSetSchema schema = new DataSetSchema();
    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    TableSchemaVO tableVO = new TableSchemaVO();
    RecordSchemaVO recordVO = new RecordSchemaVO();
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setPkReferenced(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    tableVO.setRecordSchema(recordVO);
    schemaVO.setTableSchemas(Arrays.asList(tableVO));
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));

    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(dataSchemaMapper.entityToClass(schema)).thenReturn(schemaVO);
    dataSchemaServiceImpl.isSchemaAllowedForDeletion("5ce524fad31fc52540abae73");
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Test update pk catalogue deleting schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdatePkCatalogueDeletingSchema() throws EEAException {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    schema.setTableSchemas(Arrays.asList(table));
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    dataSchemaServiceImpl.updatePkCatalogueDeletingSchema("5ce524fad31fc52540abae73");
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Test get referenced fields by schema.
   */
  @Test
  public void testGetReferencedFieldsBySchema() {
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    schema.setTableSchemas(Arrays.asList(table));

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    dataSchemaServiceImpl.getReferencedFieldsBySchema("5ce524fad31fc52540abae73");
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }


  /**
   * Test delete pk catalogue from table schema.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testDeletePkCatalogueFromTableSchema() throws EEAException {
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    field.setPk(false);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);

    dataSchemaServiceImpl.deleteFromPkCatalogue("5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73");
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }


  /**
   * Test update PK catalogue and foreigns after snapshot with no catalogue.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdatePKCatalogueAndForeignsAfterSnapshotWithNoCatalogue() throws EEAException {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    field.setPk(false);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);

    dataSchemaServiceImpl.updatePKCatalogueAndForeignsAfterSnapshot("5ce524fad31fc52540abae73", 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }


  /**
   * Test update PK catalogue and foreigns after snapshot with catalogue.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testUpdatePKCatalogueAndForeignsAfterSnapshotWithCatalogue() throws EEAException {

    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced = new ReferencedFieldSchema();
    referenced.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    field.setPk(false);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    dataSchemaServiceImpl.updatePKCatalogueAndForeignsAfterSnapshot("5ce524fad31fc52540abae73", 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }

  @Test
  public void createUniqueConstraintTest() {
    dataSchemaServiceImpl.createUniqueConstraint(new UniqueConstraintVO());
    Mockito.verify(uniqueConstraintRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void deleteUniqueConstraintTest() throws EEAException {
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniqueConstraint(new ObjectId().toString());
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }

  @Test
  public void updateUniqueConstraintTest() {
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    dataSchemaServiceImpl.updateUniqueConstraint(unique);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
    Mockito.verify(uniqueConstraintRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void getUniqueConstraintsTest() {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    uniques.add(new UniqueConstraintVO());
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    assertEquals(uniques, dataSchemaServiceImpl.getUniqueConstraints(new ObjectId().toString()));

  }

  @Test(expected = EEAException.class)
  public void getUniqueConstraintTest() throws EEAException {
    String id = new ObjectId().toString();
    try {
      dataSchemaServiceImpl.getUniqueConstraint(id);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.UNIQUE_NOT_FOUND, id), e.getMessage());
      throw e;
    }
  }

  @Test
  public void deleteUniquesConstraintFromField() throws EEAException {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    String id = new ObjectId().toString();
    List<String> fields = new ArrayList<>();
    fields.add(id);
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    unique.setFieldSchemaIds(fields);
    uniques.add(unique);
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniquesConstraintFromField(new ObjectId().toString(), id);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }


  @Test
  public void deleteUniquesConstraintFromTable() throws EEAException {
    List<UniqueConstraintSchema> uniques = new ArrayList<>();
    UniqueConstraintSchema unique = new UniqueConstraintSchema();
    unique.setUniqueId(new ObjectId());
    uniques.add(unique);
    Mockito.when(uniqueConstraintRepository.findByTableSchemaId(Mockito.any())).thenReturn(uniques);
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniquesConstraintFromTable(new ObjectId().toString());
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }

  @Test
  public void deleteUniquesConstraintFromDataset() throws EEAException {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    String id = new ObjectId().toString();
    List<String> fields = new ArrayList<>();
    fields.add(id);
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    unique.setFieldSchemaIds(fields);
    uniques.add(unique);
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniquesConstraintFromDataset(id);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }
}
