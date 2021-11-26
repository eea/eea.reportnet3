package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.SimpleDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.UniqueConstraintMapper;
import org.eea.dataset.mapper.WebFormMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.ReferenceDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.ReferenceDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.DataflowReferencedSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.domain.webform.Webform;
import org.eea.dataset.persistence.schemas.repository.DataflowReferencedRepository;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.ZipUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.impl.DataschemaServiceImpl;
import org.eea.dataset.service.model.ImportSchemas;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.dataset.validate.commands.ValidationSchemaIntegrityCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleTableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
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
   * The record store controller zuul.
   */
  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

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

  @Mock
  private SimpleDataSchemaMapper simpleDataSchemaMapper;

  @Mock
  private WebFormMapper webFormMapper;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private ContributorControllerZuul contributorControllerZuul;

  @Mock
  private IntegrationControllerZuul integrationControllerZuul;

  @Mock
  private ZipUtils zipUtils;

  @Mock
  private DataflowReferencedRepository dataflowReferencedRepository;

  @Mock
  private ReferenceDatasetRepository referenceDatasetRepository;

  @Mock
  private FileCommonUtils fileCommon;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /** The lock service. */
  @Mock
  private LockService lockService;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    ThreadPropertiesManager.setVariable("user", "user");

    ReflectionTestUtils.setField(dataSchemaServiceImpl, "timeToWaitBeforeContinueCopy", 3000L);

    MockitoAnnotations.openMocks(this);
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
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());
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
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.any())).thenReturn(null);
    dataSchemaServiceImpl.createEmptyDataSetSchema(1L);
  }

  /**
   * Delete dataset schema test.
   */
  @Test
  public void deleteDatasetSchemaTest() {
    dataSchemaServiceImpl.deleteDatasetSchema("idTableSchema", 1L);
    Mockito.verify(schemasRepository, times(1)).deleteDatasetSchemaById(Mockito.any());
  }

  /**
   * Delete group test.
   */
  @Test
  public void deleteGroupTest() {
    dataSchemaServiceImpl.deleteGroup(1L, ResourceTypeEnum.DATA_SCHEMA);
    Mockito.verify(resourceManagementControllerZull, times(1)).getGroupsByIdResourceType(1L,
        ResourceTypeEnum.DATA_SCHEMA);
  }

  /**
   * Test replace schema.
   */
  @Test
  public void testReplaceSchema() {
    DataSetSchema schema = new DataSetSchema();
    Mockito.doNothing().when(schemasRepository).deleteDatasetSchemaById(Mockito.any());
    when(schemasRepository.save(Mockito.any())).thenReturn(schema);
    doNothing().when(recordStoreControllerZuul).restoreSnapshotData(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    dataSchemaServiceImpl.replaceSchema("1L", schema, 1L, 1L);
    verify(schemasRepository, times(1)).save(Mockito.any());
  }

  /**
   * Creates the field schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createFieldSchemaTest() throws EEAException {
    ReferencedFieldSchemaVO referencedField = new ReferencedFieldSchemaVO();
    referencedField.setIdDatasetSchema("5eb4269d06390651aced7c93");
    referencedField.setIdPk("5eb4269d06390651aced7c93");

    String[] codelistItems = new String[] {"item1", "item2", "item3"};

    FieldSchemaVO field = new FieldSchemaVO();
    field.setReferencedField(referencedField);
    field.setCodelistItems(codelistItems);
    field.setType(DataType.CODELIST);
    field.setValidExtensions(Arrays.asList("pdf ", "csv").toArray(new String[2]));
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    String response = dataSchemaServiceImpl.createFieldSchema("5eb4269d06390651aced7c93", field);
    Assert.assertNotEquals("", response);
  }

  @Test(expected = EEAException.class)
  public void createFieldSchemaNameExistTest() throws EEAException {
    ReferencedFieldSchemaVO referencedField = new ReferencedFieldSchemaVO();
    referencedField.setIdDatasetSchema("5eb4269d06390651aced7c93");
    referencedField.setIdPk("5eb4269d06390651aced7c93");

    String[] codelistItems = new String[] {"item1", "item2", "item3"};

    FieldSchemaVO field = new FieldSchemaVO();
    field.setReferencedField(referencedField);
    field.setCodelistItems(codelistItems);
    field.setType(DataType.CODELIST);
    field.setId("1");
    field.setName("");
    Document document = new Document();
    Document documentField = new Document();
    List<Document> documentsField = new ArrayList<>();
    documentsField.add(documentField);
    document.put("fieldSchemas", documentsField);
    documentField.put("headerName", "");
    documentField.put("_id", "");

    Mockito.when(schemasRepository.findRecordSchemaByRecordSchemaId(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    try {
      dataSchemaServiceImpl.createFieldSchema("5eb4269d06390651aced7c93", field);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED, field.getName(),
          field.getIdRecord(), "5eb4269d06390651aced7c93"), e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the field schema exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createFieldSchemaExceptionTest() throws EEAException {
    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);
    dataSchemaServiceImpl.createFieldSchema("5eb4269d06390651aced7c93", new FieldSchemaVO());
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
    Assert.assertTrue(
        dataSchemaServiceImpl.deleteFieldSchema("datasetSchemaId", "fieldSchemaId", 1L));
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
    Assert.assertFalse(
        dataSchemaServiceImpl.deleteFieldSchema("datasetSchemaId", "fieldSchemaId", 1L));
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

    Assert.assertEquals(DataType.NUMBER_DECIMAL, dataSchemaServiceImpl
        .updateFieldSchema(new ObjectId().toString(), fieldSchemaVO, 1L, true));
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
    Assert.assertNull(dataSchemaServiceImpl.updateFieldSchema(new ObjectId().toString(),
        fieldSchemaVO, 1L, true));
  }

  /**
   * Update field schema test 3.
   */
  @Test
  public void updateFieldSchemaTest3() {
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    try {
      dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO, 1L, true);
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
      dataSchemaServiceImpl.updateFieldSchema(new ObjectId().toString(), fieldSchemaVO, 1L, true);
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
      dataSchemaServiceImpl.updateFieldSchema("<id>", fieldSchemaVO, 1L, true);
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
        dataSchemaServiceImpl.updateFieldSchema("<id>", fielSchemaVO, 1L, true));
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
        dataSchemaServiceImpl.updateFieldSchema("<id>", fielSchemaVO, 1L, true));
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
    dataSchemaServiceImpl.deleteTableSchema(id.toString(), id.toString(), 1l);
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
    dataSchemaServiceImpl.deleteTableSchema(id.toString(), id.toString(), 1L);
  }

  /**
   * Update table schema table not found exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateTableSchemaTableNotFoundExceptionTest() throws EEAException {
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDatasetSchema("5eb4269d06390651aced7c93");
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any())).thenReturn(null);
    TableSchemaVO tableSchemaVOSend = new TableSchemaVO();
    tableSchemaVOSend.setIdTableSchema("idTableSchema");
    try {
      dataSchemaServiceImpl.updateTableSchema(1L, tableSchemaVOSend);
    } catch (EEAException e) {
      Assert.assertEquals(
          String.format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaVOSend.getIdTableSchema(), 1L),
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateTableSchemaTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDatasetSchema("5eb4269d06390651aced7c93");
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchemaVO.getDescription()).thenReturn("description");
    Mockito.when(tableSchemaVO.getNameTableSchema()).thenReturn("nameTableSchema");
    Mockito.when(tableSchemaVO.getReadOnly()).thenReturn(true);
    Mockito.when(tableSchemaVO.getToPrefill()).thenReturn(true);
    Mockito.when(tableSchema.put(Mockito.any(), Mockito.any())).thenReturn(null);
    Mockito.when(schemasRepository.updateTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    dataSchemaServiceImpl.updateTableSchema(1L, tableSchemaVO);
    Mockito.verify(schemasRepository, times(1)).updateTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Update table schema null values test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = NullPointerException.class)
  public void updateTableSchemaNullValuesTest() throws EEAException {
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDatasetSchema("5eb4269d06390651aced7c93");
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(tableSchema);
    Mockito.when(tableSchemaVO.getDescription()).thenReturn(null);
    Mockito.when(tableSchemaVO.getNameTableSchema()).thenReturn(null);
    Mockito.when(tableSchemaVO.getReadOnly()).thenReturn(null);
    Mockito.when(tableSchemaVO.getToPrefill()).thenReturn(null);
    Mockito.when(tableSchemaVO.getFixedNumber()).thenReturn(null);
    Mockito.when(schemasRepository.updateTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    try {
      dataSchemaServiceImpl.updateTableSchema(1L, tableSchemaVO);
    } catch (NullPointerException e) {
      throw e;
    }
  }

  /**
   * Update table schema illegal argument exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateTableSchemaIllegalArgumentExceptionTest() throws EEAException {
    DataSetMetabase dataSetMetabase = new DataSetMetabase();
    dataSetMetabase.setDatasetSchema("5eb4269d06390651aced7c93");
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(dataSetMetabase));
    Mockito.when(schemasRepository.findTableSchema(Mockito.any(), Mockito.any()))
        .thenThrow(IllegalArgumentException.class);
    try {
      dataSchemaServiceImpl.updateTableSchema(1L, tableSchemaVO);
    } catch (EEAException e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getCause().getClass());
      throw e;
    }
  }

  /**
   * Creates the table schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaRuleCreationTest() throws EEAException {
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNotEmpty(true);

    Mockito.when(tableSchemaMapper.classToEntity(Mockito.any(TableSchemaVO.class)))
        .thenReturn(new TableSchema());
    Mockito.when(datasetMetabaseService.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    Mockito.when(rulesControllerZuul.updateSequence(Mockito.any())).thenReturn(1L);
    dataSchemaServiceImpl.createTableSchema(new ObjectId().toString(), tableSchemaVO, 1L);
    Mockito.verify(schemasRepository, times(1)).insertTableSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the table schema no rule creation test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createTableSchemaNoRuleCreationTest() throws EEAException {
    TableSchemaVO tableSchemaVO = new TableSchemaVO();
    tableSchemaVO.setNotEmpty(null);

    Mockito.when(tableSchemaMapper.classToEntity(Mockito.any(TableSchemaVO.class)))
        .thenReturn(new TableSchema());
    dataSchemaServiceImpl.createTableSchema(new ObjectId().toString(), tableSchemaVO, 1L);
    Mockito.verify(schemasRepository, times(1)).insertTableSchema(Mockito.any(), Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void updateFieldSchemaNameExistTest() throws EEAException {
    ReferencedFieldSchemaVO referencedField = new ReferencedFieldSchemaVO();
    referencedField.setIdDatasetSchema("5eb4269d06390651aced7c93");
    referencedField.setIdPk("5eb4269d06390651aced7c93");

    String[] codelistItems = new String[] {"item1", "item2", "item3"};

    FieldSchemaVO field = new FieldSchemaVO();
    field.setReferencedField(referencedField);
    field.setCodelistItems(codelistItems);
    field.setType(DataType.CODELIST);
    field.setId("1");
    field.setName("");

    Document document = new Document();
    Document documentField = new Document();
    List<Document> documentsField = new ArrayList<>();
    documentsField.add(documentField);
    document.put("fieldSchemas", documentsField);
    documentField.put("headerName", "");
    documentField.put("_id", "");

    Mockito.when(schemasRepository.findRecordSchemaByRecordSchemaId(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    try {
      dataSchemaServiceImpl.updateFieldSchema("5eb4269d06390651aced7c93", field, 1L, true);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED, field.getName(),
          field.getIdRecord(), "5eb4269d06390651aced7c93"), e.getMessage());
      throw e;
    }
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
    dataSchemaServiceImpl.updateDatasetSchemaDescription("<id>", "description");
    Mockito.verify(schemasRepository, times(1)).updateDatasetSchemaDescription(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update dataset schema description test 2.
   */
  @Test
  public void updateDatasetSchemaDescriptionTest2() {
    dataSchemaServiceImpl.updateDatasetSchemaDescription("<id>", "description");
    Mockito.verify(schemasRepository, times(1)).updateDatasetSchemaDescription(Mockito.any(),
        Mockito.any());
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

    Assert.assertFalse(dataSchemaServiceImpl.validateSchema("5ce524fad31fc52540abae73",
        TypeDataflowEnum.REPORTING));
  }


  /**
   * Propagate rules after update type null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNullTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
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
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doNothing().when(kafkaSenderUtils).releaseKafkaEvent(Mockito.any(), Mockito.any());

    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(false);
    fieldSchemaVO.setId("fieldSchemaId");

    dataSchemaServiceImpl.propagateRulesAfterUpdateSchema("datasetSchemaId", fieldSchemaVO, null,
        1L);
    Mockito.verify(rulesControllerZuul, times(1)).deleteRuleRequired(Mockito.any(), Mockito.any(),
        Mockito.any());

  }

  /**
   * Propagate rules after update type not null not required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void propagateRulesAfterUpdateTypeNotNullNotRequiredTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
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
    fieldSchemaVO.setType(DataType.EXTERNAL_LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataflowId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);

    dataSchemaServiceImpl.addToPkCatalogue(fieldSchemaVO, 1L);
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
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    referenced.setDataflowId(1L);
    fieldSchemaVO.setReferencedField(referenced);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataflowId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new DataflowReferencedSchema());
    dataSchemaServiceImpl.addToPkCatalogue(fieldSchemaVO, 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).save(Mockito.any());
  }

  @Test
  public void updatePkCatalogueExistingPKReferencedTest() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    referenced.setDataflowId(1L);
    fieldSchemaVO.setReferencedField(referenced);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataflowId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.anyLong())).thenReturn(null);
    dataSchemaServiceImpl.addToPkCatalogue(fieldSchemaVO, 1L);
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
    referenced.setDataflowId(1L);
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

    dataSchemaServiceImpl.deleteFromPkCatalogue(fieldSchemaVO, 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }

  @Test
  public void deleteFromPkCatalogueReferenced() throws EEAException {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    referenced.setDataflowId(1L);
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

    dataSchemaServiceImpl.deleteFromPkCatalogue(fieldSchemaVO, 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
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
    fieldSchemaVO.setType(DataType.LINK);
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

  @Test
  public void testAddForeignRelationExtLink() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    fieldSchemaVO.setType(DataType.EXTERNAL_LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    ReferenceDataset reference = new ReferenceDataset();
    reference.setId(1L);

    Mockito.when(referenceDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(reference));
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
    fieldSchemaVO.setType(DataType.LINK);
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

  @Test
  public void testDeleteForeignRelationExtLink() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setRequired(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    fieldSchemaVO.setType(DataType.EXTERNAL_LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    ReferenceDataset reference = new ReferenceDataset();
    reference.setId(1L);

    Mockito.when(referenceDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(reference));
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
    referenced.setDataflowId(1L);
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    field.setType(DataType.EXTERNAL_LINK);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    schema.setTableSchemas(Arrays.asList(table));
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(new ArrayList<>());
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDataflowId(1L);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new Document());
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    dataSchemaServiceImpl.updatePkCatalogueDeletingSchema("5ce524fad31fc52540abae73", 1L);
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
    field.setPk(true);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);

    dataSchemaServiceImpl.deleteFromPkCatalogue("5ce524fad31fc52540abae73",
        "5ce524fad31fc52540abae73", 1L);
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
    referenced.setDataflowId(1L);
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced);
    field.setPk(false);
    field.setType(DataType.EXTERNAL_LINK);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema.setTableSchemas(Arrays.asList(table));
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(false);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDataflowId(1L);


    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);


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
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDataflowId(1L);

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldSchemaVO);
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong())).thenReturn(dataset);
    dataSchemaServiceImpl.updatePKCatalogueAndForeignsAfterSnapshot("5ce524fad31fc52540abae73", 1L);
    Mockito.verify(pkCatalogueRepository, times(1)).findByIdPk(Mockito.any());
  }

  /**
   * Creates the unique constraint test.
   */
  @Test
  public void createUniqueConstraintTest() {
    dataSchemaServiceImpl.createUniqueConstraint(new UniqueConstraintVO());
    Mockito.verify(uniqueConstraintRepository, times(1)).save(Mockito.any());
  }

  /**
   * Delete unique constraint test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteUniqueConstraintTest() throws EEAException {
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniqueConstraint(new ObjectId().toString());
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }

  /**
   * Update unique constraint test.
   */
  @Test
  public void updateUniqueConstraintTest() {
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    dataSchemaServiceImpl.updateUniqueConstraint(unique);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
    Mockito.verify(uniqueConstraintRepository, times(1)).save(Mockito.any());
  }

  /**
   * Gets the unique constraints test.
   *
   * @return the unique constraints test
   */
  @Test
  public void getUniqueConstraintsTest() {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    uniques.add(new UniqueConstraintVO());
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    assertEquals(uniques, dataSchemaServiceImpl.getUniqueConstraints(new ObjectId().toString()));

  }

  /**
   * Gets the unique constraint test.
   *
   * @return the unique constraint test
   * @throws EEAException the EEA exception
   */
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

  /**
   * Delete uniques constraint from field.
   *
   * @throws EEAException the EEA exception
   */
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
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteUniquesConstraintFromField(new ObjectId().toString(), id);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }


  /**
   * Delete uniques constraint from table.
   *
   * @throws EEAException the EEA exception
   */
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

  /**
   * Delete uniques constraint from dataset.
   *
   * @throws EEAException the EEA exception
   */
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

  /**
   * Delete only unique constraint from field one test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteOnlyUniqueConstraintFromFieldOneTest() throws EEAException {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    String id = new ObjectId().toString();
    List<String> fields = new ArrayList<>();
    fields.add(id);
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    unique.setFieldSchemaIds(fields);
    uniques.add(unique);
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    Mockito.when(uniqueConstraintRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new UniqueConstraintSchema()));
    Mockito.when(uniqueConstraintMapper.entityToClass(Mockito.any()))
        .thenReturn(new UniqueConstraintVO());
    dataSchemaServiceImpl.deleteOnlyUniqueConstraintFromField(new ObjectId().toString(), id);
    Mockito.verify(uniqueConstraintRepository, times(1)).deleteByUniqueId(Mockito.any());
  }

  /**
   * Delete only unique constraint from field not found test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteOnlyUniqueConstraintFromFieldNotFoundTest() throws EEAException {
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    String id = new ObjectId().toString();
    List<String> fields = new ArrayList<>();
    UniqueConstraintVO unique = new UniqueConstraintVO();
    unique.setUniqueId(new ObjectId().toString());
    unique.setFieldSchemaIds(fields);
    uniques.add(unique);
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    dataSchemaServiceImpl.deleteOnlyUniqueConstraintFromField(new ObjectId().toString(), id);
    Mockito.verify(uniqueConstraintRepository, times(0)).deleteByUniqueId(Mockito.any());
  }

  /**
   * Creates the unique constraint PK test.
   */
  @Test
  public void createUniqueConstraintPKTest() {
    ObjectId idRecord = new ObjectId();
    FieldSchemaVO field = new FieldSchemaVO();
    field.setIdRecord(idRecord.toString());
    field.setId(new ObjectId().toString());
    ArrayList<TableSchema> tableSchemas = new ArrayList<>();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    RecordSchema record = new RecordSchema();
    record.setIdRecordSchema(idRecord);
    table.setRecordSchema(record);
    tableSchemas.add(table);
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(tableSchemas);
    field.setPk(true);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    dataSchemaServiceImpl.createUniqueConstraintPK(new ObjectId().toString(), field);
    Mockito.verify(rulesControllerZuul, times(1)).createUniqueConstraintRule(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Test copy unique constraints catalogue.
   */
  @Test
  public void testCopyUniqueConstraintsCatalogue() {
    String datasetSchemaId = "5ce524fad31fc52540abae73";
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    UniqueConstraintSchema unique = new UniqueConstraintSchema();
    unique.setDatasetSchemaId(new ObjectId());
    unique.setUniqueId(new ObjectId());
    dictionaryOriginTargetObjectId.put("5ce524fad31fc52540abae73", "5ce524fad31fc52540abae73");
    Mockito.when(uniqueConstraintRepository.findByDatasetSchemaId(Mockito.any()))
        .thenReturn(Arrays.asList(unique));
    List<UniqueConstraintVO> uniques = new ArrayList<>();
    UniqueConstraintVO uniqueVO = new UniqueConstraintVO();
    uniqueVO.setFieldSchemaIds(Arrays.asList("5ce524fad31fc52540abae73"));
    uniques.add(uniqueVO);
    Mockito.when(uniqueConstraintMapper.entityListToClass(Mockito.any())).thenReturn(uniques);
    dataSchemaServiceImpl.copyUniqueConstraintsCatalogue(Arrays.asList(datasetSchemaId),
        dictionaryOriginTargetObjectId);
    Mockito.verify(uniqueConstraintRepository, times(1)).save(Mockito.any());
  }

  /**
   * Update field schema link test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateFieldSchemaLinkTest() throws EEAException {
    FieldSchemaVO fieldSchemaVO = Mockito.mock(FieldSchemaVO.class);
    Document document = Mockito.mock(Document.class);
    PkCatalogueSchema catalogue = Mockito.mock(PkCatalogueSchema.class);
    DataSetSchema datasetSchema = Mockito.mock(DataSetSchema.class);
    String[] codelistItems = new String[] {"item1", "item2", "item3"};
    ReferencedFieldSchemaVO referenced = Mockito.mock(ReferencedFieldSchemaVO.class);
    Mockito.when(fieldSchemaVO.getId()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(fieldSchemaVO.getPk()).thenReturn(Boolean.TRUE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    Mockito.when(document.get(LiteralConstants.TYPE_DATA)).thenReturn(DataType.LINK.getValue());
    Mockito.when(document.get(LiteralConstants.REFERENCED_FIELD)).thenReturn(document);
    Mockito.when(document.get("idPk")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(document.get("_id")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(document.get(LiteralConstants.ID_DATASET_SCHEMA))
        .thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(catalogue.getReferenced()).thenReturn(new ArrayList<>());
    Mockito.when(catalogue.getIdPk()).thenReturn(null);
    Mockito.doNothing().when(pkCatalogueRepository).deleteByIdPk(Mockito.any());
    Mockito.when(pkCatalogueRepository.save(Mockito.any())).thenReturn(null);
    Mockito.when(document.put("pkReferenced", Boolean.FALSE)).thenReturn(null);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.anyString(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito.when(document.get(LiteralConstants.PK)).thenReturn(Boolean.FALSE);
    Mockito.when(document.get("idRecord")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(datasetSchema.getTableSchemas()).thenReturn(new ArrayList<>());
    Mockito.when(fieldSchemaVO.getType()).thenReturn(DataType.LINK);
    Mockito.when(document.put(LiteralConstants.TYPE_DATA, DataType.LINK.getValue()))
        .thenReturn(DataType.TEXT.getValue());
    Mockito.when(document.containsKey(LiteralConstants.CODELIST_ITEMS)).thenReturn(true);
    Mockito.when(document.remove(Mockito.any())).thenReturn(null);
    Mockito.when(fieldSchemaVO.getDescription()).thenReturn("");
    Mockito.when(document.put("description", "")).thenReturn(null);
    // Mockito.when(document.put("headerName", "")).thenReturn(null);
    Mockito.when(fieldSchemaVO.getCodelistItems()).thenReturn(codelistItems);
    Mockito.when(fieldSchemaVO.getReferencedField()).thenReturn(referenced);
    Mockito.when(referenced.getIdDatasetSchema()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(referenced.getIdPk()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(fieldSchemaVO.getName()).thenReturn("name");
    Assert.assertEquals(DataType.LINK, dataSchemaServiceImpl
        .updateFieldSchema("5ce524fad31fc52540abae73", fieldSchemaVO, 1L, true));
  }

  @Test
  public void updateFieldSchemaLinkFullDataTest() throws EEAException {
    FieldSchemaVO fieldSchemaVO = Mockito.mock(FieldSchemaVO.class);
    Document document = Mockito.mock(Document.class);
    PkCatalogueSchema catalogue = Mockito.mock(PkCatalogueSchema.class);
    DataSetSchema datasetSchema = Mockito.mock(DataSetSchema.class);
    String[] codelistItems = new String[] {"item1", "item2", "item3"};
    ReferencedFieldSchemaVO referenced = Mockito.mock(ReferencedFieldSchemaVO.class);
    Mockito.when(fieldSchemaVO.getId()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(fieldSchemaVO.getPk()).thenReturn(Boolean.TRUE);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    Mockito.when(document.get(LiteralConstants.TYPE_DATA)).thenReturn(DataType.LINK.getValue());
    Mockito.when(document.get(LiteralConstants.REFERENCED_FIELD)).thenReturn(document);
    Mockito.when(document.get("idPk")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(document.get("_id")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(document.get("dataflowId")).thenReturn(1L);
    Mockito.when(document.get(LiteralConstants.ID_DATASET_SCHEMA))
        .thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(pkCatalogueRepository.findByIdPk(Mockito.any())).thenReturn(catalogue);
    Mockito.when(catalogue.getReferenced()).thenReturn(new ArrayList<>());
    Mockito.when(catalogue.getIdPk()).thenReturn(null);
    Mockito.doNothing().when(pkCatalogueRepository).deleteByIdPk(Mockito.any());
    Mockito.when(pkCatalogueRepository.save(Mockito.any())).thenReturn(null);
    Mockito.when(document.put("pkReferenced", Boolean.FALSE)).thenReturn(null);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.anyString(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito.when(document.get(LiteralConstants.PK)).thenReturn(Boolean.FALSE);
    Mockito.when(document.get("idRecord")).thenReturn(new ObjectId("5ce524fad31fc52540abae73"));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(datasetSchema.getTableSchemas()).thenReturn(new ArrayList<>());
    Mockito.when(fieldSchemaVO.getType()).thenReturn(DataType.LINK);
    Mockito.when(document.put(LiteralConstants.TYPE_DATA, DataType.LINK.getValue()))
        .thenReturn(DataType.TEXT.getValue());
    Mockito.when(document.containsKey(LiteralConstants.CODELIST_ITEMS)).thenReturn(true);
    Mockito.when(document.remove(Mockito.any())).thenReturn(null);
    Mockito.when(fieldSchemaVO.getDescription()).thenReturn("");
    Mockito.when(document.put("description", "")).thenReturn(null);
    // Mockito.when(document.put("headerName", "")).thenReturn(null);
    Mockito.when(fieldSchemaVO.getCodelistItems()).thenReturn(codelistItems);
    Mockito.when(fieldSchemaVO.getReferencedField()).thenReturn(referenced);
    Mockito.when(referenced.getIdDatasetSchema()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(referenced.getIdPk()).thenReturn("5ce524fad31fc52540abae73");
    Mockito.when(referenced.getLabelId()).thenReturn("5eb4269d06390651aced7c93");
    Mockito.when(referenced.getLinkedConditionalFieldId()).thenReturn("5eb4269d06390651aced7c93");
    Mockito.when(referenced.getMasterConditionalFieldId()).thenReturn("5eb4269d06390651aced7c93");
    Mockito.when(referenced.getTableSchemaName()).thenReturn("table");
    Mockito.when(referenced.getFieldSchemaName()).thenReturn("field");
    Mockito.when(referenced.getDataflowId()).thenReturn(1L);
    Mockito.when(fieldSchemaVO.getName()).thenReturn("name");
    Mockito.when(fieldSchemaVO.getValidExtensions())
        .thenReturn(Arrays.asList("pdf ", "csv").toArray(new String[2]));
    DataflowReferencedSchema dataflowReferenced = new DataflowReferencedSchema();
    List<Long> referencedIds = new ArrayList<>();
    referencedIds.add(1L);
    referencedIds.add(2L);
    dataflowReferenced.setReferencedByDataflow(referencedIds);
    Mockito.when(dataflowReferencedRepository.findByDataflowId(Mockito.any()))
        .thenReturn(dataflowReferenced);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setDataflowId(1L);
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any())).thenReturn(dataset);
    Assert.assertEquals(DataType.LINK, dataSchemaServiceImpl
        .updateFieldSchema("5ce524fad31fc52540abae73", fieldSchemaVO, 1L, false));
  }

  /**
   * Gets the simple schema test.
   *
   * @return the simple schema test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getSimpleSchemaTest() throws EEAException {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    DataSetSchema datasetSchema = new DataSetSchema();
    DesignDataset design = new DesignDataset();
    SimpleDatasetSchemaVO simpleDatasetSchemaVO = new SimpleDatasetSchemaVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(design));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    when(simpleDataSchemaMapper.entityToClass(Mockito.any(DataSetSchema.class)))
        .thenReturn(simpleDatasetSchemaVO);
    assertEquals(simpleDatasetSchemaVO, dataSchemaServiceImpl.getSimpleSchema(1L));
  }

  @Test
  public void getSimpleSchemaEUDatasetTest() throws EEAException {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    DataSetSchema datasetSchema = new DataSetSchema();
    DesignDataset design = new DesignDataset();
    SimpleDatasetSchemaVO simpleDatasetSchemaVO = new SimpleDatasetSchemaVO();
    SimpleTableSchemaVO table = new SimpleTableSchemaVO();
    ArrayList<SimpleTableSchemaVO> tables = new ArrayList<>();
    ArrayList<SimpleFieldSchemaVO> fields = new ArrayList<>();
    table.setFields(fields);
    tables.add(table);
    simpleDatasetSchemaVO.setTables(tables);
    SimpleFieldSchemaVO countryCode = new SimpleFieldSchemaVO();
    datasetMetabase.setDatasetSchema(new ObjectId().toString());
    when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(design));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    when(simpleDataSchemaMapper.entityToClass(Mockito.any(DataSetSchema.class)))
        .thenReturn(simpleDatasetSchemaVO);
    when(datasetMetabaseService.getDatasetType(Mockito.any()))
        .thenReturn(DatasetTypeEnum.EUDATASET);
    countryCode.setFieldName(LiteralConstants.COUNTRY_CODE);
    countryCode.setFieldType(DataType.TEXT);
    fields.add(countryCode);
    assertEquals(simpleDatasetSchemaVO, dataSchemaServiceImpl.getSimpleSchema(1L));
  }

  /**
   * Gets the simple schema id null test.
   *
   * @return the simple schema id null test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getSimpleSchemaIdNullTest() throws EEAException {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));
    try {
      dataSchemaServiceImpl.getSimpleSchema(1L);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, 1L), e.getMessage());
      throw e;
    }
  }

  /**
   * Gets the simple schema null test.
   *
   * @return the simple schema null test
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getSimpleSchemaNullTest() throws EEAException {
    DataSetMetabase datasetMetabase = new DataSetMetabase();
    ObjectId id = new ObjectId();
    DesignDataset design = new DesignDataset();
    new SimpleDatasetSchemaVO();
    datasetMetabase.setDatasetSchema(id.toString());
    when(dataSetMetabaseRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(datasetMetabase));
    when(designDatasetRepository.findFirstByDatasetSchema(Mockito.any()))
        .thenReturn(Optional.of(design));

    try {
      dataSchemaServiceImpl.getSimpleSchema(1L);
    } catch (EEAException e) {
      assertEquals(String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, id), e.getMessage());
      throw e;
    }
  }

  @Test
  public void testCheckDeleteAttachments() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5eb4269d06390651aced7c93");
    fieldSchemaVO.setType(DataType.ATTACHMENT);
    Document doc = new Document();
    doc.put("typeData", "ATTACHMENT");
    when(schemasRepository.findFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(doc);
    dataSchemaServiceImpl.checkClearAttachments(1L, "5eb4269d06390651aced7c93", fieldSchemaVO);
    Mockito.verify(schemasRepository, times(1)).findFieldSchema(Mockito.any(), Mockito.any());
  }

  @Test
  public void checkDeleteAttachmentsFirstHasToCleanTest() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5eb4269d06390651aced7c93");
    fieldSchemaVO.setType(DataType.ATTACHMENT);
    fieldSchemaVO.setValidExtensions(new String[0]);
    Document doc = new Document();
    doc.put("typeData", "ATTACHMENT1");
    doc.put("validExtensions", new ArrayList<>());
    when(schemasRepository.findFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(doc);
    dataSchemaServiceImpl.checkClearAttachments(1L, "5eb4269d06390651aced7c93", fieldSchemaVO);
    Mockito.verify(schemasRepository, times(1)).findFieldSchema(Mockito.any(), Mockito.any());
  }

  @Test
  public void checkDeleteAttachmentsSecondHasToCleanTest() {
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setId("5eb4269d06390651aced7c93");
    fieldSchemaVO.setType(DataType.ATTACHMENT);
    fieldSchemaVO.setValidExtensions(new String[0]);
    fieldSchemaVO.setMaxSize(1f);
    Document doc = new Document();
    doc.put("typeData", "ATTACHMENT");
    doc.put("validExtensions", new ArrayList<>());
    doc.put("maxSize", 0.0);
    when(schemasRepository.findFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(doc);
    dataSchemaServiceImpl.checkClearAttachments(1L, "5eb4269d06390651aced7c93", fieldSchemaVO);
    Mockito.verify(schemasRepository, times(1)).findFieldSchema(Mockito.any(), Mockito.any());
  }

  @Test
  public void updateWebform() {
    when(webFormMapper.classToEntity(Mockito.any())).thenReturn(new Webform());
    dataSchemaServiceImpl.updateWebform(Mockito.anyString(), new WebformVO());
    Mockito.verify(schemasRepository, times(1)).updateDatasetSchemaWebForm(Mockito.any(),
        Mockito.any());
  }


  @Test
  public void testImportSchemas() throws EEAException, IOException {

    DesignDatasetVO dataset = new DesignDatasetVO();
    dataset.setId(1L);
    dataset.setDatasetSchema("5ce524fad31fc52540abae73");
    dataset.setDataSetName("test");
    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    schemaVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    schemaVO.setNameDatasetSchema("test");
    TableSchemaVO tableVO = new TableSchemaVO();
    tableVO.setNameTableSchema("tableName");
    RecordSchemaVO recordVO = new RecordSchemaVO();
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setPkReferenced(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    fieldSchemaVO.setType(DataType.LINK);
    fieldSchemaVO.setIdRecord("5ce524fad31fc52540abae73");
    fieldSchemaVO.setName("fieldName");
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    referenced.setLabelId("5ce524fad31fc52540abae73");
    referenced.setLinkedConditionalFieldId("5ce524fad31fc52540abae73");
    referenced.setMasterConditionalFieldId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    tableVO.setRecordSchema(recordVO);
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    tableVO.setNameTableSchema("table1");
    schemaVO.setTableSchemas(Arrays.asList(tableVO));
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    table.setNameTableSchema("tableName");
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    field.setHeaderName("fieldName");
    ReferencedFieldSchema referenced2 = new ReferencedFieldSchema();
    referenced2.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setLabelId(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setLinkedConditionalFieldId(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setMasterConditionalFieldId(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setDataflowId(1L);
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced2);
    field.setType(DataType.LINK);
    record.setFieldSchema(Arrays.asList(field));
    record.setIdRecordSchema(new ObjectId("5ce524fad31fc52540abae73"));
    table.setRecordSchema(record);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    schema.setTableSchemas(tableSchemas);
    schema.setIdDataFlow(1L);
    schema.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73"));

    DataSetSchema schema2 = new DataSetSchema();
    schema2.setIdDataFlow(1L);
    schema2.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    schema2.setTableSchemas(new ArrayList<>());

    DataSetSchemaVO schema2VO = new DataSetSchemaVO();
    schema2VO.setIdDataSetSchema(new ObjectId("5ce524fad31fc52540abae73").toString());
    schema2VO.setTableSchemas(new ArrayList<>());
    schema2VO.setNameDatasetSchema("schemaName");
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(CompletableFuture.completedFuture(1L));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema2);


    Mockito.doNothing().when(recordStoreControllerZuul).createUpdateQueryView(Mockito.any(),
        Mockito.anyBoolean());

    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.any()))
        .thenReturn(new DataFlowVO());

    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(schema));
    Mockito.when(dataSchemaMapper.entityToClass(Mockito.any(DataSetSchema.class)))
        .thenReturn(schemaVO);


    Map<String, Long> schemaDatatasetId = new HashMap<>();
    Map<String, String> schemaNames = new HashMap<>();
    List<byte[]> qcRulesBytes = new ArrayList<>();

    UniqueConstraintVO uniqueVO = new UniqueConstraintVO();
    uniqueVO.setDataflowId("1");
    uniqueVO.setDatasetSchemaId("5ce524fad31fc52540abae73");
    uniqueVO.setUniqueId("5ce524fad31fc52540abae73");
    uniqueVO.setTableSchemaId("5ce524fad31fc52540abae73");
    uniqueVO.setFieldSchemaIds(Arrays.asList("5ce524fad31fc52540abae73"));
    schemaNames.put("5ce524fad31fc52540abae73", " name ");

    ImportSchemas importSchema = new ImportSchemas();
    importSchema.setSchemas(Arrays.asList(schema));
    importSchema.setExternalIntegrations(Arrays.asList(new IntegrationVO()));
    importSchema.setIntegrities(Arrays.asList(new IntegrityVO()));
    importSchema.setQcRulesBytes(qcRulesBytes);
    importSchema.setSchemaIds(schemaDatatasetId);
    importSchema.setSchemaNames(schemaNames);
    importSchema.setUniques(Arrays.asList(new UniqueConstraintSchema()));
    Mockito.when(zipUtils.unZipImportSchema(Mockito.any(), Mockito.any())).thenReturn(importSchema);

    DataSetMetabase datasetMetabase = new DataSetMetabase();
    datasetMetabase.setDatasetSchema("5ce524fad31fc52540abae73");
    Mockito.when(dataSetMetabaseRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(datasetMetabase));

    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any(FieldSchema.class)))
        .thenReturn(fieldSchemaVO);

    Document document = new Document();
    document.put(LiteralConstants.TYPE_DATA, DataType.LINK);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(document);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(new UpdateResult() {

          @Override
          public boolean wasAcknowledged() {
            return false;
          }

          @Override
          public boolean isModifiedCountAvailable() {
            return false;
          }

          @Override
          public BsonValue getUpsertedId() {
            return null;
          }

          @Override
          public long getModifiedCount() {
            return 1;
          }

          @Override
          public long getMatchedCount() {
            return 1;
          }
        });
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());
    when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    dataSchemaServiceImpl.importSchemas(1L, multipartFile.getInputStream(), "file.zip");
    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }


  @Test
  public void testImportSchemasEmpty() throws EEAException, IOException {

    when(zipUtils.unZipImportSchema(Mockito.any(), Mockito.any())).thenReturn(new ImportSchemas());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(baos);
    ZipEntry entry1 = new ZipEntry("Table.schema");
    ZipEntry entry2 = new ZipEntry("Table.qcrules");
    zip.putNextEntry(entry1);
    zip.putNextEntry(entry2);
    zip.close();
    MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
        "application/x-zip-compressed", baos.toByteArray());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    dataSchemaServiceImpl.importSchemas(1L, multipartFile.getInputStream(), "file.zip");
    Mockito.verify(designDatasetRepository, times(1)).findByDataflowId(Mockito.anyLong());
  }

  @Test
  public void testImportSchemasException() throws EEAException, IOException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zip = new ZipOutputStream(baos);
      ZipEntry entry1 = new ZipEntry("Table.schema");
      ZipEntry entry2 = new ZipEntry("Table.qcrules");
      zip.putNextEntry(entry1);
      zip.putNextEntry(entry2);
      zip.close();
      MultipartFile multipartFile = new MockMultipartFile("file", "file.zip",
          "application/x-zip-compressed", baos.toByteArray());

      Mockito.when(zipUtils.unZipImportSchema(Mockito.any(), Mockito.any()))
          .thenThrow(new EEAException("error"));
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("name");
      dataSchemaServiceImpl.importSchemas(1L, multipartFile.getInputStream(),
          multipartFile.getOriginalFilename());
    } catch (EEAException e) {
      assertEquals("error", e.getMessage());
      throw e;
    }
  }

  @Test
  public void testExportSchemas() throws IOException, EEAException {

    Mockito.when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.when(schemasRepository.findByIdDataFlow(Mockito.anyLong()))
        .thenReturn(Arrays.asList(new DataSetSchema()));

    dataSchemaServiceImpl.exportSchemas(1L);
    Mockito.verify(schemasRepository, times(1)).findByIdDataFlow(Mockito.anyLong());
  }

  @Test(expected = EEAException.class)
  public void testExportSchemasException() throws IOException, EEAException {

    Mockito.when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.when(schemasRepository.findByIdDataFlow(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());

    try {
      dataSchemaServiceImpl.exportSchemas(1L);
    } catch (EEAException e) {
      assertEquals(String.format("No schemas to export in the dataflow %s", 1L), e.getMessage());
      throw e;
    }
  }


  @Test
  public void updateDatasetSchemaExportable() {
    dataSchemaServiceImpl.updateDatasetSchemaExportable("", false);
    Mockito.verify(schemasRepository, times(1)).updateDatasetSchemaExportable(Mockito.any(),
        Mockito.anyBoolean());
  }


  @Test
  public void testExportFieldSchemas() throws IOException, EEAException {

    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setType(DataType.TEXT);
    Mockito
        .when(fileCommon.getFieldSchemas(Mockito.anyString(), Mockito.any(DataSetSchemaVO.class)))
        .thenReturn(Arrays.asList(fieldVO));

    dataSchemaServiceImpl.exportFieldsSchema(1L, new ObjectId().toString(),
        new ObjectId().toString());
    Mockito.verify(fileCommon, times(1)).getFieldSchemas(Mockito.anyString(),
        Mockito.any(DataSetSchemaVO.class));
  }

  @Test
  public void testImportFieldSchemasSave() throws EEAException, IOException {

    String csv =
        "Field name,PK,Required,ReadOnly,Field description,Field type,Extra information\r\n"
            + "countryCode,false,false,false,descripcion1,TEXT,";
    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", csv.getBytes());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("Tabla1");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setHeaderName("countryCode");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.TEXT);
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema2));
    tableSchema.setRecordSchema(recordSchema);
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    ReflectionTestUtils.setField(dataSchemaServiceImpl, "delimiter", ',');

    Mockito.when(fileCommon.isDesignDataset(Mockito.anyLong())).thenReturn(true);

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflow);

    List<FieldSchema> fields = new ArrayList<>();
    fields.add(fieldSchema2);
    Mockito.when(fileCommon.findFieldSchemas(Mockito.anyString(), Mockito.any(DataSetSchema.class)))
        .thenReturn(fields);

    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setId(new ObjectId().toString());
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldVO);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));


    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    dataSchemaServiceImpl.importFieldsSchema(new ObjectId().toString(), new ObjectId().toString(),
        1L, multipartFile.getInputStream(), true);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  @Test
  public void testImportFieldSchemasUpdate() throws EEAException, IOException {

    String csv =
        "Field name,PK,Required,ReadOnly,Field description,Field type,Extra information\r\n"
            + "countryCode,false,false,false,descripcion1,TEXT,";
    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", csv.getBytes());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("Tabla1");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setHeaderName("countryCode");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.TEXT);
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema2));
    tableSchema.setRecordSchema(recordSchema);
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    ReflectionTestUtils.setField(dataSchemaServiceImpl, "delimiter", ',');

    Mockito.when(fileCommon.isDesignDataset(Mockito.anyLong())).thenReturn(true);

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflow);

    Mockito.when(fileCommon.findFieldSchemas(Mockito.anyString(), Mockito.any(DataSetSchema.class)))
        .thenReturn(Arrays.asList(fieldSchema2));

    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchema);
    Mockito.when(schemasRepository.updateFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));

    Mockito.when(fieldSchema.put(Mockito.any(), Mockito.any()))
        .thenReturn(DataType.TEXT.getValue());

    dataSchemaServiceImpl.importFieldsSchema(new ObjectId().toString(), new ObjectId().toString(),
        1L, multipartFile.getInputStream(), false);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  @Test
  public void testImportFieldSchemasExceptionWrongStatus() throws EEAException, IOException {

    String csv =
        "Field name,PK,Required,ReadOnly,Field description,Field type,Extra information\r\n"
            + "countryCode,false,false,false,descripcion1,TEXT,";
    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", csv.getBytes());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("Tabla1");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setHeaderName("countryCode");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.TEXT);
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema2));
    tableSchema.setRecordSchema(recordSchema);
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    ReflectionTestUtils.setField(dataSchemaServiceImpl, "delimiter", ',');

    Mockito.when(fileCommon.isDesignDataset(Mockito.anyLong())).thenReturn(true);

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setStatus(TypeStatusEnum.DRAFT);
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflow);

    Mockito.when(fileCommon.findFieldSchemas(Mockito.anyString(), Mockito.any(DataSetSchema.class)))
        .thenReturn(Arrays.asList(fieldSchema2));

    dataSchemaServiceImpl.importFieldsSchema(new ObjectId().toString(), new ObjectId().toString(),
        1L, multipartFile.getInputStream(), false);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }


  @Test
  public void testImportFieldSchemasEmptyField() throws EEAException, IOException {

    String csv = "\n";
    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", csv.getBytes());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("Tabla1");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setHeaderName("countryCode");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.TEXT);
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema2));
    tableSchema.setRecordSchema(recordSchema);
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    ReflectionTestUtils.setField(dataSchemaServiceImpl, "delimiter", ',');

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setStatus(TypeStatusEnum.DESIGN);


    dataSchemaServiceImpl.importFieldsSchema(new ObjectId().toString(), new ObjectId().toString(),
        1L, multipartFile.getInputStream(), true);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Test import field schemas save code list not empty.
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testImportFieldSchemasSaveCodeListNotEmpty() throws EEAException, IOException {

    String csv =
        "Field name,PK,Required,ReadOnly,Field description,Field type,Extra information\r\n"
            + "countryCode,false,false,false,descripcion1,TEXT,field1;field2;field3";
    MultipartFile multipartFile =
        new MockMultipartFile("file", "tableSchemaName.csv", "text/csv", csv.getBytes());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setIdDataFlow(1L);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    TableSchema tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema("Tabla1");
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema2 = new FieldSchema();
    fieldSchema2.setHeaderName("countryCode");
    fieldSchema2.setIdFieldSchema(new ObjectId());
    fieldSchema2.setType(DataType.TEXT);
    recordSchema.setFieldSchema(Arrays.asList(fieldSchema2));
    tableSchema.setRecordSchema(recordSchema);
    datasetSchema.setTableSchemas(Arrays.asList(tableSchema));
    Mockito.when(schemasRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetSchema));
    ReflectionTestUtils.setField(dataSchemaServiceImpl, "delimiter", ',');

    Mockito.when(fileCommon.isDesignDataset(Mockito.anyLong())).thenReturn(true);

    DataFlowVO dataflow = new DataFlowVO();
    dataflow.setId(1L);
    dataflow.setStatus(TypeStatusEnum.DESIGN);
    Mockito.when(dataFlowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflow);

    List<FieldSchema> fields = new ArrayList<>();
    fields.add(fieldSchema2);
    Mockito.when(fileCommon.findFieldSchemas(Mockito.anyString(), Mockito.any(DataSetSchema.class)))
        .thenReturn(fields);

    FieldSchemaVO fieldVO = new FieldSchemaVO();
    fieldVO.setId(new ObjectId().toString());
    Mockito.when(fieldSchemaNoRulesMapper.entityToClass(Mockito.any())).thenReturn(fieldVO);
    PkCatalogueSchema catalogue = new PkCatalogueSchema();
    catalogue.setIdPk(new ObjectId());
    catalogue.setReferenced(Arrays.asList(new ObjectId()));


    Mockito.when(schemasRepository.createFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Mockito.when(schemasRepository.deleteFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

    dataSchemaServiceImpl.importFieldsSchema(new ObjectId().toString(), new ObjectId().toString(),
        1L, multipartFile.getInputStream(), true);
    Mockito.verify(schemasRepository, times(1)).findById(Mockito.any());
  }



  @Test
  public void testExportZipFieldSchemas() throws IOException, EEAException {

    DesignDataset design = new DesignDataset();
    design.setId(1L);
    design.setDataSetName("DS");
    design.setDatasetSchema(new ObjectId().toString());
    Mockito.when(designDatasetRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(design));

    DataSetSchema schema = new DataSetSchema();
    schema.setIdDataSetSchema(new ObjectId());
    TableSchema table = new TableSchema();
    table.setIdTableSchema(new ObjectId());
    table.setNameTableSchema("table");
    RecordSchema record = new RecordSchema();
    record.setIdRecordSchema(new ObjectId());
    FieldSchema field = new FieldSchema();
    field.setIdFieldSchema(new ObjectId());
    field.setHeaderName("field");
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    schema.setTableSchemas(Arrays.asList(table));
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);

    dataSchemaServiceImpl.exportZipFieldSchemas(1L);
    Mockito.verify(designDatasetRepository, times(1)).findById(Mockito.anyLong());
  }

  @Test(expected = EEAException.class)
  public void testExportZipFieldSchemasException() throws IOException, EEAException {

    Mockito.when(designDatasetRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
    try {
      dataSchemaServiceImpl.exportZipFieldSchemas(1L);
    } catch (EEAException e) {
      assertEquals(String.format("No field schemas to export in the dataset %s", 1L),
          e.getMessage());
      throw e;
    }
  }

}
