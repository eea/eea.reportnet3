/*
 *
 */
package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.data.repository.FieldValidationRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.data.repository.TableValidationRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.data.repository.ValidationRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.util.KieBaseManager;
import org.eea.validation.util.RulesErrorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class ValidationServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

  /**
   * The validation service impl.
   */
  @InjectMocks
  private ValidationServiceImpl validationServiceImpl;


  /**
   * The resource management controller.
   */
  @Mock
  private ResourceManagementControllerZull resourceManagementController;
  /**
   * The kie session.
   */
  @Mock
  private KieSession kieSession;

  /**
   * The kie session.
   */
  @Mock
  private KieBase kieBase;

  /**
   * The kie base manager.
   */
  @Mock
  private KieBaseManager kieBaseManager;

  /**
   * The dataset repository.
   */
  @Mock
  private DatasetRepository datasetRepository;

  /**
   * The dataset controller.
   */
  @Mock
  private DataSetControllerZuul datasetController;

  /**
   * The validation dataset repository.
   */
  @Mock
  private ValidationDatasetRepository validationDatasetRepository;


  /**
   * The record repository.
   */
  @Mock
  private RecordRepository recordRepository;


  /**
   * The validation field repository.
   */
  @Mock
  private FieldValidationRepository validationFieldRepository;

  /**
   * The schemas repository.
   */
  @Mock
  private SchemasRepository schemasRepository;
  /**
   * /** The table validation repository.
   */
  @Mock
  private TableValidationRepository tableValidationRepository;

  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /**
   * The record validation repository.
   */
  @Mock
  private RecordValidationRepository recordValidationRepository;

  /**
   * The dataset value.
   */
  private DatasetValue datasetValue;

  /**
   * The field value.
   */
  private FieldValue fieldValue;

  /**
   * The record value.
   */
  private RecordValue recordValue;

  /**
   * The record values.
   */
  private ArrayList<RecordValue> recordValues;

  /**
   * The table value.
   */
  private TableValue tableValue;

  /**
   * The table values.
   */
  private ArrayList<TableValue> tableValues;

  /**
   * The data set VO.
   */
  private DataSetVO dataSetVO;

  /**
   * The table V os.
   */
  private ArrayList<TableVO> tableVOs;

  /**
   * The table VO.
   */
  private TableVO tableVO;

  /**
   * The validation.
   */
  private Validation validation;


  /**
   * The id list.
   */
  List<Long> idList;


  /**
   * The attributes.
   */
  private Map<String, List<String>> attributes;

  /**
   * The field repository.
   */
  @Mock
  private FieldRepository fieldRepository;

  /**
   * The table repository.
   */
  @Mock
  private TableRepository tableRepository;

  /**
   * The dataset schema controller.
   */
  @Mock
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;

  /**
   * The rules error utils.
   */
  @Mock
  private RulesErrorUtils rulesErrorUtils;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /** The validation repository. */
  @Mock
  private ValidationRepository validationRepository;

  /** The rule service impl. */
  @Mock
  private RulesServiceImpl ruleServiceImpl;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    attributes = new HashMap<>();
    List<String> data = new ArrayList<>();
    data.add("'IT'");
    attributes.put("countryCode", data);
    data.set(0, "2019");
    attributes.put("dataCallYear", data);
    validation = new Validation();
    fieldValue = new FieldValue();
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setLevelError(ErrorTypeEnum.ERROR);
    recordValue.setFields(new ArrayList<>());
    tableValue = new TableValue();
    tableValue.setId(1L);
    tableValue.setTableValidations(new ArrayList<>());
    recordValue.setTableValue(tableValue);
    recordValues.add(recordValue);
    datasetValue = new DatasetValue();
    tableValues = new ArrayList<>();
    tableValues.add(tableValue);
    datasetValue.setTableValues(tableValues);
    datasetValue.setIdDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetValue.setDatasetValidations(new ArrayList<>());
    tableVOs = new ArrayList<>();
    tableVO = new TableVO();
    tableVOs.add(tableVO);
    dataSetVO = new DataSetVO();
    dataSetVO.setTableVO(tableVOs);
    dataSetVO.setId(1L);
    tableValue.setDatasetId(datasetValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");

    MockitoAnnotations.openMocks(this);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    DatasetValidation datasetValidation = new DatasetValidation();
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(ErrorTypeEnum.WARNING);
    datasetValidation.setValidation(validation);
    datasetValidation.setId(1L);
    datasetValidation.setDatasetValue(new DatasetValue());
    datasetValidations.add(datasetValidation);

    // tableValues
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableVal = new TableValue();
    List<TableValidation> TableValidationList = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setValidation(validation);
    tableValidation.setTableValue(tableValue);
    TableValidationList.add(tableValidation);
    tableVal.setTableValidations(TableValidationList);
    tableValues.add(tableVal);
    tableValues.add(new TableValue());
    datasetValue.setTableValues(tableValues);
    datasetValue.setDatasetValidations(datasetValidations);
    datasetValue.setId(123L);

    idList = new ArrayList<>();
    idList.add(1L);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

  }

  /**
   * Test run dataset validations.
   */
  @Test
  public void testRunDatasetValidations() {
    DatasetValue dataset = new DatasetValue();
    dataset.setDatasetValidations(new ArrayList<>());
    assertEquals("failed", dataset.getDatasetValidations(),
        validationServiceImpl.runDatasetValidations(dataset, kieSession));
  }


  /**
   * Test run table validations.
   */
  @Test
  public void testRunTableValidations() {
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableVal = new TableValue();
    tableVal.setTableValidations(new ArrayList<>());
    tableValues.add(tableVal);
    tableValues.add(new TableValue());
    assertEquals("failed", tableVal.getTableValidations(),
        validationServiceImpl.runTableValidations(tableValue, kieSession));

    tableValues.remove(0);
    assertEquals("failed", new ArrayList<>(),
        validationServiceImpl.runTableValidations(tableValue, kieSession));

    assertEquals("failed", new ArrayList<>(),
        validationServiceImpl.runTableValidations(new TableValue(), kieSession));
  }

  /**
   * Test run record validations.
   */
  @Test
  public void testRunRecordValidations() {
    RecordValue record = new RecordValue();
    record.setRecordValidations(new ArrayList<>());
    assertEquals("failed", record.getRecordValidations(),
        validationServiceImpl.runRecordValidations(record, kieSession));
  }

  /**
   * Test run record validations2.
   */
  @Test
  public void testRunRecordValidations2() {
    RecordValue record = new RecordValue();
    ArrayList<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(new RecordValidation());
    record.setRecordValidations(recordValidations);
    assertEquals("failed", record.getRecordValidations(),
        validationServiceImpl.runRecordValidations(record, kieSession));
  }

  /**
   * Test run record validations.
   */
  @Test
  public void testRunRecordValidationsNotNull() {
    RecordValue record = new RecordValue();
    record.setIdRecordSchema("123");
    record.setRecordValidations(new ArrayList<>());
    assertEquals("failed", record.getRecordValidations(),
        validationServiceImpl.runRecordValidations(record, kieSession));
  }

  /**
   * Test run field validations.
   */
  @Test
  public void testRunFieldValidations() {
    FieldValue field = new FieldValue();
    assertEquals("failed", new ArrayList<FieldValidation>(),
        validationServiceImpl.runFieldValidations(field, kieSession));
  }

  /**
   * Test run record validations2.
   */
  @Test
  public void testRunFieldValidations2() {
    FieldValue field = new FieldValue();
    ArrayList<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(new FieldValidation());
    field.setFieldValidations(fieldValidations);
    assertEquals("failed", field.getFieldValidations(),
        validationServiceImpl.runFieldValidations(field, kieSession));
  }

  /**
   * Test run field validations.
   */
  @Test
  public void testRunFieldValidationsNotNull() {
    FieldValue field = new FieldValue();
    field.setIdFieldSchema("123");
    assertEquals("failed", new ArrayList<FieldValidation>(),
        validationServiceImpl.runFieldValidations(field, kieSession));
  }


  /**
   * Test load rules knowledge base throw.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testLoadRulesKnowledgeBaseThrow() throws FileNotFoundException, EEAException {
    doThrow(FileNotFoundException.class).when(kieBaseManager).reloadRules(Mockito.any(),
        Mockito.any(), Mockito.any());
    try {
      validationServiceImpl.loadRulesKnowledgeBase(1L, null);
    } catch (EEAException e) {
      assertEquals("Error, cause is not FileNotFoundException", e.getCause().getClass(),
          FileNotFoundException.class);
      throw e;
    }

  }


  /**
   * Test validate data set data.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateRecord() throws FileNotFoundException, EEAException {

    Validation validation = new Validation();
    validation.setId(2L);
    validation.setLevelError(ErrorTypeEnum.WARNING);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId("1L");
    recordValue.setRecordValidations(new ArrayList<>());

    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation);
    recordValidations.add(recordValidation);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);

    when(recordRepository.findRecordsPageable(Mockito.any(Pageable.class))).thenReturn(records);
    when(kieBase.newKieSession()).thenReturn(kieSession);
    when(kieSession.fireAllRules()).thenReturn(1);

    validationServiceImpl.validateRecord(1L, kieBase, PageRequest.of(0, 5000));

  }


  /**
   * Test validate data set data dataset excep.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateFieldsSuccess() throws EEAException {

    List<FieldValue> fields = new ArrayList<>();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    validation.setId(1L);
    validation.setTypeEntity(EntityTypeEnum.DATASET);
    fieldValidation.setValidation(validation);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId("1L");
    fieldValue.setLevelError(ErrorTypeEnum.WARNING);
    fields.add(fieldValue);
    fields.add(fieldValue);
    Page<FieldValue> page = new PageImpl<>(fields);
    when(fieldRepository.findAll(Mockito.any(Pageable.class))).thenReturn(page);
    when(kieBase.newKieSession()).thenReturn(kieSession);
    when(kieSession.fireAllRules()).thenReturn(1);
    validationServiceImpl.validateFields(1L, kieBase, PageRequest.of(0, 5000), false);

  }

  /**
   * Test load rules knowledge base.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void testLoadRulesKnowledgeBase() throws FileNotFoundException, EEAException {
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(datasetSchemaControllerZuul.getDatasetSchemaId(Mockito.any())).thenReturn("");
    when(kieBaseManager.reloadRules(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(kiebase);
    assertEquals("assertion error", kiebase,
        validationServiceImpl.loadRulesKnowledgeBase(1L, null));
  }


  /**
   * Test load rules knowledge base throw error null.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testLoadRulesKnowledgeBaseThrowErrorNull()
      throws FileNotFoundException, EEAException {
    KieHelper kieHelper = new KieHelper();
    kieHelper.build();
    Mockito.doThrow(new FileNotFoundException()).when(kieBaseManager).reloadRules(Mockito.anyLong(),
        Mockito.any(), Mockito.any());

    validationServiceImpl.loadRulesKnowledgeBase(1L, null);
  }

  /**
   * Test delete all validation.
   */
  @Test
  public void testDeleteAllValidation() {
    doNothing().when(datasetRepository).deleteValidationTable();
    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setAttributes(attributes);
    when(
        resourceManagementController.getResourceDetail(1L, ResourceGroupEnum.DATASET_LEAD_REPORTER))
            .thenReturn(resourceInfoVO);

    validationServiceImpl.deleteAllValidation(1L);
    Mockito.verify(datasetRepository, times(1)).deleteValidationTable();
  }

  /**
   * Gets the field errors test.
   *
   * @return the field errors test
   *
   * @throws Exception the exception
   */
  @Test
  public void getFieldErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(ErrorTypeEnum.ERROR);
    validation.setTypeEntity(EntityTypeEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(validation);
    datasetValidation.setDatasetValue(datasetValue);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    datasetValidations.add(datasetValidation);
    datasetValue.setDatasetValidations(datasetValidations);
    FieldValidation fieldValidation = new FieldValidation();
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setValidation(validation);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(fieldValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    when(validationFieldRepository.findByValidationIds(Mockito.any())).thenReturn(fieldValidations);
    assertNotNull("error", validationServiceImpl.getFieldErrors(1L, new ArrayList<>()));
  }

  /**
   * Gets the record errors test.
   *
   * @return the record errors test
   *
   * @throws Exception the exception
   */
  @Test
  public void getRecordErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(ErrorTypeEnum.ERROR);
    validation.setTypeEntity(EntityTypeEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    recordValidations.add(recordValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    when(recordValidationRepository.findByValidationIds(Mockito.any()))
        .thenReturn(recordValidations);
    assertNotNull("error", validationServiceImpl.getRecordErrors(1L, new ArrayList<>()));
  }

  /**
   * Gets the table errors test.
   *
   * @return the table errors test
   *
   * @throws Exception the exception
   */
  @Test
  public void getTableErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(ErrorTypeEnum.ERROR);
    validation.setTypeEntity(EntityTypeEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(validation);
    datasetValidation.setDatasetValue(datasetValue);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    datasetValidations.add(datasetValidation);
    datasetValue.setDatasetValidations(datasetValidations);
    FieldValidation fieldValidation = new FieldValidation();
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setValidation(validation);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(fieldValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));

    when(tableValidationRepository.findByValidationIds(Mockito.any())).thenReturn(tableValidations);
    assertNotNull("error", validationServiceImpl.getTableErrors(1L, new ArrayList<>()));
  }

  /**
   * Gets the dataset errors test.
   *
   * @return the dataset errors test
   *
   * @throws Exception the exception
   */
  @Test
  public void getDatasetErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(ErrorTypeEnum.ERROR);
    validation.setTypeEntity(EntityTypeEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(validation);
    datasetValidation.setDatasetValue(datasetValue);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    datasetValidations.add(datasetValidation);
    datasetValue.setDatasetValidations(datasetValidations);
    FieldValidation fieldValidation = new FieldValidation();
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setValidation(validation);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(fieldValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));

    assertNotNull("error",
        validationServiceImpl.getDatasetErrors(1L, datasetValue, new ArrayList<>()));
  }


  /**
   * Gets the dataset valueby resourceId test exception.
   *
   * @return the dataset valueby resourceId test exception
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getDatasetValuebyIdTestException() throws EEAException {
    validationServiceImpl.getDatasetValuebyId(null);
  }

  /**
   * Gets the dataset valueby resourceId test success.
   *
   * @return the dataset valueby resourceId test success
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDatasetValuebyIdTestSuccess() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.getDatasetValuebyId(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Gets the find by resourceId data set schema test exception.
   *
   * @return the find by resourceId data set schema test exception
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(null, null);
  }

  /**
   * Gets the find by resourceId data set schema test null exception.
   *
   * @return the find by resourceId data set schema test null exception
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestNullException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(1L, null);
  }

  /**
   * Gets the find by resourceId data set schema test success.
   *
   * @return the find by resourceId data set schema test success
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void getfindByIdDataSetSchemaTestSuccess() throws EEAException {
    when(schemasRepository.findByIdDataSetSchema(Mockito.any(ObjectId.class)))
        .thenReturn(new DataSetSchema());
    validationServiceImpl.getfindByIdDataSetSchema(1L, new ObjectId("5cf0e9b3b793310e9ceca190"));
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }

  /**
   * Gets the dataset errors.
   *
   * @return the dataset errors
   */
  @Test
  public void getDatasetErrors() {
    datasetValue.setId(1L);
    List<DatasetValidation> validations = new ArrayList<>();
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setId(1L);
    Validation validationAux = new Validation();
    validationAux.setId(1L);
    validationAux.setIdRule("1");
    validationAux.setLevelError(ErrorTypeEnum.ERROR);
    validationAux.setMessage("ERROR");
    validationAux.setTableName("DATASET");
    validationAux.setShortCode("SC");
    validationAux.setTypeEntity(EntityTypeEnum.DATASET);
    validationAux.setValidationDate(new Date().toString());
    datasetValidation.setValidation(validationAux);
    validations.add(datasetValidation);
    datasetValue.setDatasetValidations(validations);
    datasetValidation.setDatasetValue(datasetValue);
    when(validationDatasetRepository.findByValidationIds(Mockito.any()))
        .thenReturn(datasetValue.getDatasetValidations());
    assertNotNull("assertion error",
        validationServiceImpl.getDatasetErrors(1L, datasetValue, new ArrayList<>()));
  }

  /**
   * Validate data set error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void validateDataSetError() throws EEAException {
    validationServiceImpl.validateDataSet(1L, kieBase);
  }

  /**
   * Validate data error.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataError() throws EEAException {
    datasetValue.getTableValues().get(0).getTableValidations().get(0).getValidation()
        .setLevelError(ErrorTypeEnum.ERROR);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(kieBase.newKieSession()).thenReturn(kieSession);
    validationServiceImpl.validateDataSet(1L, kieBase);
  }

  /**
   * Validate data warning.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataWarning() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(kieBase.newKieSession()).thenReturn(kieSession);
    validationServiceImpl.validateDataSet(1L, kieBase);
    Mockito.verify(validationDatasetRepository, times(1)).saveAll(Mockito.any());
  }


  /**
   * Validate table.
   *
   * @throws EEAException the eea exception
   */
  @Test
  public void validateTable() throws EEAException {
    when(tableRepository.findById(Mockito.any())).thenReturn(Optional.of(tableValue));
    when(kieBase.newKieSession()).thenReturn(kieSession);
    validationServiceImpl.validateTable(1L, Mockito.any(), kieBase, "null");
    Mockito.verify(tableValidationRepository, times(1)).saveAll(Mockito.any());
  }


  /**
   * Force validations test.
   */
  @Test
  public void forceValidationsTest() {
    validationServiceImpl.forceValidations(1L);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

  /**
   * Count records dataset test.
   */
  @Test
  public void countRecordsDatasetTest() {
    when(recordRepository.countRecordsDataset()).thenReturn(1);
    assertEquals("not Equals", Integer.valueOf(1), validationServiceImpl.countRecordsDataset(1L));
  }

  /**
   * Count fields dataset test.
   */
  @Test
  public void countFieldsDatasetTest() {
    when(fieldRepository.countFieldsDataset()).thenReturn(1);
    assertEquals("not Equals", Integer.valueOf(1), validationServiceImpl.countFieldsDataset(1L));
  }


  /**
   * Run dataset vlaidations exception.
   */
  @Test
  public void runDatasetVlaidationsException() {
    doThrow(new RuntimeException()).when(kieSession).fireAllRules();
    validationServiceImpl.runDatasetValidations(new DatasetValue(), kieSession);
    Mockito.verify(rulesErrorUtils, times(1)).createRuleErrorException(Mockito.any(),
        Mockito.any());
  }

  /**
   * Run table validations exception.
   */
  @Test
  public void runTableValidationsException() {
    doThrow(new RuntimeException()).when(kieSession).fireAllRules();
    validationServiceImpl.runTableValidations(new TableValue(), kieSession);
    Mockito.verify(rulesErrorUtils, times(1)).createRuleErrorException(Mockito.any(),
        Mockito.any());
  }

  /**
   * Run record validations exception.
   */
  @Test
  public void runRecordValidationsException() {
    doThrow(new RuntimeException()).when(kieSession).fireAllRules();
    validationServiceImpl.runRecordValidations(new RecordValue(), kieSession);
    Mockito.verify(rulesErrorUtils, times(1)).createRuleErrorException(Mockito.any(),
        Mockito.any());
  }

  /**
   * Run field validations exception.
   */
  @Test
  public void runFieldValidationsException() {
    doThrow(new RuntimeException()).when(kieSession).fireAllRules();
    validationServiceImpl.runFieldValidations(new FieldValue(), kieSession);
    Mockito.verify(rulesErrorUtils, times(1)).createRuleErrorException(Mockito.any(),
        Mockito.any());
  }

  /**
   * Export validation data CSV.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Test
  public void exportValidationDataCSV() throws IOException, EEAException {
    DataSetMetabaseVO dataSetMetabase = new DataSetMetabaseVO();

    // We create the error list and populate it with one error

    FailedValidationsDatasetVO validations = new FailedValidationsDatasetVO();
    List<GroupValidationVO> errorList = new ArrayList<>();
    GroupValidationVO error = new GroupValidationVO();

    error.setTypeEntity(EntityTypeEnum.TABLE);
    error.setNameTableSchema("Table");
    error.setNameFieldSchema("Field");
    error.setShortCode("FML");
    error.setLevelError(ErrorTypeEnum.WARNING);
    error.setMessage("Mensaje de error");
    error.setNumberOfRecords(4);

    errorList.add(error);
    validations.setErrors(errorList);

    // We create a rule and assign it a shortcode which is going to be compared with the error to
    // check if the rule caused the error itself

    RulesSchemaVO rulesSchemaVO = new RulesSchemaVO();
    List<RuleVO> rulesVO = new ArrayList<>();
    RuleVO ruleVO = new RuleVO();

    ruleVO.setShortCode("FML");
    rulesVO.add(ruleVO);
    rulesSchemaVO.setRules(rulesVO);

    dataSetMetabase.setDataflowId(1L);
    dataSetMetabase.setDataProviderId(1L);
    dataSetMetabase.setDatasetSchema("603362319d49f04fce13b68f");
    dataSetMetabase.setDataSetName("file");
    dataSetMetabase.setId(1L);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");

    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(validationRepository.findGroupRecordsByFilter(Mockito.anyLong(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(errorList);
    when(ruleServiceImpl.getActiveRulesSchemaByDatasetId(Mockito.any())).thenReturn(rulesSchemaVO);
    validationServiceImpl.exportValidationFile(1L);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  /**
   * Load rules knowledge base exception.
   *
   * @throws EEAException the EEA exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test(expected = EEAException.class)
  public void loadRulesKnowledgeBaseException() throws EEAException, FileNotFoundException {
    doThrow(new NullPointerException()).when(datasetSchemaControllerZuul)
        .getDatasetSchemaId(Mockito.any());
    try {
      validationServiceImpl.loadRulesKnowledgeBase(1L, null);
    } catch (Exception e) {
      assertEquals(EEAErrorMessage.VALIDATION_SESSION_ERROR, e.getLocalizedMessage());
      throw e;
    }
  }

  @After
  public void afterTests() {
    File file = new File("./dataset-1-validations");
    try {
      FileUtils.deleteDirectory(file);
    } catch (IOException e) {

    }
  }

  @Test
  public void countEmptyFieldsDatasetTestSuccess() throws EEAException {
    when(fieldRepository.countEmptyFieldsDataset()).thenReturn(1);
    assertEquals((Integer) 1, validationServiceImpl.countEmptyFieldsDataset(1L));
  }

}
