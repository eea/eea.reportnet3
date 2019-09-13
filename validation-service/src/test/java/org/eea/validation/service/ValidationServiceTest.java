/*
 * 
 */
package org.eea.validation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
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
import org.eea.validation.persistence.data.repository.FieldValidationRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository;
import org.eea.validation.persistence.data.repository.TableValidationRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.service.impl.ValidationServiceImpl;
import org.eea.validation.util.KieBaseManager;
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
   * The validation service impl mocke.
   */
  @Mock
  private ValidationServiceImpl validationServiceImplMocke;

  /**
   * The kie session.
   */
  @Mock
  private KieSession kieSession;

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
   * The validation table repository.
   */
  @Mock
  private TableValidationRepository validationTableRepository;

  /**
   * The record repository.
   */
  @Mock
  private RecordRepository recordRepository;

  /**
   * The validation record repository.
   */
  @Mock
  private RecordValidationRepository validationRecordRepository;

  /**
   * The validation field repository.
   */
  @Mock
  private FieldValidationRepository validationFieldRepository;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The dataset validation repository. */
  @Mock
  private ValidationDatasetRepository datasetValidationRepository;

  /** The table validation repository. */
  @Mock
  private TableValidationRepository tableValidationRepository;
  /**
   * The dataset value.
   */
  private DatasetValue datasetValue;

  /** The field value. */
  private FieldValue fieldValue;

  /** The record value. */
  private RecordValue recordValue;

  /** The record values. */
  private ArrayList<RecordValue> recordValues;

  /** The table value. */
  private TableValue tableValue;

  /** The table values. */
  private ArrayList<TableValue> tableValues;

  /** The data set VO. */
  private DataSetVO dataSetVO;

  /** The table V os. */
  private ArrayList<TableVO> tableVOs;

  /** The table VO. */
  private TableVO tableVO;

  /** The validation. */
  private Validation validation;

  /** The dataset metabase. */
  @Mock
  private DatasetMetabaseController datasetMetabase;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    validation = new Validation();
    fieldValue = new FieldValue();
    recordValues = new ArrayList<>();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setLevelError(TypeErrorEnum.ERROR);
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

    MockitoAnnotations.initMocks(this);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    DatasetValidation datasetValidation = new DatasetValidation();
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.WARNING);
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
    datasetValue.setIdDatasetSchema("1234");
    datasetValue.setId(123L);


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
        Mockito.any());
    try {
      validationServiceImpl.loadRulesKnowledgeBase(1L);
    } catch (EEAException e) {
      assertEquals("Error, cause is not FileNotFoundException", e.getCause().getClass(),
          FileNotFoundException.class);
      throw e;
    }

  }


  /**
   * Testvalidate record.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateField() throws FileNotFoundException, EEAException {

  }

  /**
   * Test validate record exception.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testValidateRecordException() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.validateRecord(1L, kieSession);
  }

  /**
   * Test validate data set data.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  // @Test
  public void testValidateRecord() throws FileNotFoundException, EEAException {

    datasetValue.getTableValues().get(0).setId(1L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    datasetValue.getTableValues().get(1).setId(2L);
    datasetValue.getTableValues().get(1).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setId(2L);
    validation2.setLevelError(TypeErrorEnum.WARNING);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    recordValue.setRecordValidations(new ArrayList<>());
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    fieldValidations.add(fieldValidation);
    FieldValidation fieldValidation2 = new FieldValidation();
    fieldValidation2.setValidation(validation2);
    fieldValidations.add(fieldValidation2);
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId(1L);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation2);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);

    when(recordRepository.findAllRecordsByTableValueId(Mockito.any())).thenReturn(records);
    validationServiceImpl.validateRecord(1L, kieSession);

  }

  // @Test
  public void testValidateRecordError() throws FileNotFoundException, EEAException {

    datasetValue.getTableValues().get(0).setId(1L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    datasetValue.getTableValues().get(1).setId(2L);
    datasetValue.getTableValues().get(1).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setId(2L);
    validation2.setLevelError(TypeErrorEnum.ERROR);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    recordValue.setRecordValidations(new ArrayList<>());
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    fieldValidations.add(fieldValidation);
    FieldValidation fieldValidation2 = new FieldValidation();
    fieldValidation2.setValidation(validation2);
    fieldValidations.add(fieldValidation2);
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId(1L);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation2);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);

    when(recordRepository.findAllRecordsByTableValueId(Mockito.any())).thenReturn(records);
    validationServiceImpl.validateRecord(1L, kieSession);

  }

  /**
   * Test validate record warning part.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  // @Test
  public void testValidateRecordWarningPart() throws FileNotFoundException, EEAException {
    datasetValue.getTableValues().remove(1);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.WARNING);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    recordValue.setRecordValidations(new ArrayList<>());
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    fieldValidations.add(fieldValidation);
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId(1L);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);
    when(recordRepository.findAllRecordsByTableValueId(Mockito.any())).thenReturn(records);
    validationServiceImpl.validateRecord(1L, kieSession);

  }

  /**
   * Test validate data set data session excep.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testValidateDataSetDataSessionExcep() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.validateFields(1L, kieSession);
  }

  /**
   * Test validate data set data dataset excep.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateFieldsSuccess() throws EEAException {
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValue.setId(1L);
    fieldValue.setLevelError(TypeErrorEnum.WARNING);
    validation.setId(1L);
    validation.setTypeEntity(TypeEntityEnum.DATASET);
    fieldValidation.setValidation(validation);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    fieldValue.setFieldValidations(fieldValidations);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);
    datasetValue.getTableValues().get(0).setId(1L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    datasetValue.getTableValues().get(1).setId(2L);
    datasetValue.getTableValues().get(1).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(recordRepository.findAllRecordsByTableValueId(Mockito.any())).thenReturn(records);
    validationServiceImpl.validateFields(1L, kieSession);

  }

  @Test
  public void testValidateFieldsSuccessData() throws EEAException {
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValue.setId(1L);
    fieldValue.setLevelError(TypeErrorEnum.WARNING);
    validation.setId(1L);
    validation.setTypeEntity(TypeEntityEnum.DATASET);
    fieldValidation.setValidation(validation);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setId(1L);
    fieldValidations.add(fieldValidation);
    fieldValue.setFieldValidations(fieldValidations);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);
    datasetValue.getTableValues().get(0).setId(1L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    datasetValue.getTableValues().get(1).setId(2L);
    datasetValue.getTableValues().get(1).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(recordRepository.findAllRecordsByTableValueId(Mockito.any())).thenReturn(records);
    validationServiceImpl.validateFields(1L, kieSession);
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
    when(kieBaseManager.reloadRules(Mockito.any(), Mockito.any())).thenReturn(kiebase);
    validationServiceImpl.loadRulesKnowledgeBase(1L);
  }

  /**
   * Test load rules knowledge base throw error exception.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testLoadRulesKnowledgeBaseThrowErrorException()
      throws FileNotFoundException, EEAException {
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(123L);
    validationServiceImpl.loadRulesKnowledgeBase(1L);
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
    KieBase kiebase = kieHelper.build();
    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(null);
    validationServiceImpl.loadRulesKnowledgeBase(1L);
  }

  /**
   * Test delete all validation.
   */
  @Test
  public void testDeleteAllValidation() {
    doNothing().when(datasetRepository).deleteValidationTable();
    validationServiceImpl.deleteAllValidation(1L);
    Mockito.verify(datasetRepository, times(1)).deleteValidationTable();
  }

  /**
   * Gets the field errors test.
   *
   * @return the field errors test
   * @throws Exception the exception
   */
  @Test
  public void getFieldErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
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
    assertNotNull("error", validationServiceImpl.getFieldErrors(1L, new ArrayList<Long>()));
  }

  /**
   * Gets the record errors test.
   *
   * @return the record errors test
   * @throws Exception the exception
   */
  @Test
  public void getRecordErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
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
    when(validationRecordRepository.findByValidationIds(Mockito.any()))
        .thenReturn(recordValidations);
    assertNotNull("error", validationServiceImpl.getRecordErrors(1L, new ArrayList<Long>()));
  }

  /**
   * Gets the table errors test.
   *
   * @return the table errors test
   * @throws Exception the exception
   */
  @Test
  public void getTableErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
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
    assertNotNull("error", validationServiceImpl.getTableErrors(1L, new ArrayList<Long>()));
  }

  /**
   * Gets the dataset errors test.
   *
   * @return the dataset errors test
   * @throws Exception the exception
   */
  @Test
  public void getDatasetErrorsTest() throws Exception {
    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
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
        validationServiceImpl.getDatasetErrors(1L, datasetValue, new ArrayList<Long>()));
  }


  /**
   * Gets the dataset valueby id test exception.
   *
   * @return the dataset valueby id test exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getDatasetValuebyIdTestException() throws EEAException {
    validationServiceImpl.getDatasetValuebyId(null);
  }

  /**
   * Gets the dataset valueby id test success.
   *
   * @return the dataset valueby id test success
   * @throws EEAException the EEA exception
   */
  @Test
  public void getDatasetValuebyIdTestSuccess() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.getDatasetValuebyId(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

  /**
   * Gets the find by id data set schema test exception.
   *
   * @return the find by id data set schema test exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(null, null);
  }

  /**
   * Gets the find by id data set schema test null exception.
   *
   * @return the find by id data set schema test null exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestNullException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(1L, null);
  }

  /**
   * Gets the find by id data set schema test success.
   *
   * @return the find by id data set schema test success
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
    datasetValue.getDatasetValidations().get(0).getValidation()
        .setTypeEntity(TypeEntityEnum.DATASET);
    datasetValue.getDatasetValidations().get(0).getValidation()
        .setValidationDate(new Date().toString());
    when(validationDatasetRepository.findByValidationIds(Mockito.any()))
        .thenReturn(datasetValue.getDatasetValidations());
    validationServiceImpl.getDatasetErrors(1L, datasetValue, new ArrayList());
  }

  /**
   * Validate data set error.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void validateDataSetError() throws EEAException {
    validationServiceImpl.validateDataSet(1L, kieSession);
  }

  /**
   * Validate data error.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataError() throws EEAException {
    datasetValue.getTableValues().get(0).getTableValidations().get(0).getValidation()
        .setLevelError(TypeErrorEnum.ERROR);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    validationServiceImpl.validateDataSet(1L, kieSession);
  }

  /**
   * Validate data warning.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataWarning() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    validationServiceImpl.validateDataSet(1L, kieSession);
  }

  /**
   * Validate table throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void validateTableThrow() throws EEAException {
    validationServiceImpl.validateTable(1L, kieSession);
  }

  /**
   * Validate table error.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateTableError() throws EEAException {
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setId(2L);
    validation2.setLevelError(TypeErrorEnum.ERROR);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    recordValue.setRecordValidations(new ArrayList<>());
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    fieldValidations.add(fieldValidation);
    FieldValidation fieldValidation2 = new FieldValidation();
    fieldValidation2.setValidation(validation2);
    fieldValidations.add(fieldValidation2);
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId(1L);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation2);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);
    datasetValue.getTableValues().remove(1);
    datasetValue.getTableValues().get(0).setId(2L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    ArrayList<TableValidation> tableVals = new ArrayList<>();
    TableValidation tableValidation = new TableValidation();
    tableValidation.setValidation(validation2);
    tableVals.add(tableValidation);
    datasetValue.getTableValues().get(0).setTableValidations(tableVals);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(datasetMetabase.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    validationServiceImpl.validateTable(1L, kieSession);
  }

  /**
   * Validate table warning.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateTableWarning() throws EEAException {
    Validation validation = new Validation();
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    Validation validation2 = new Validation();
    validation2.setId(2L);
    validation2.setLevelError(TypeErrorEnum.WARNING);
    List<RecordValue> records = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId(1L);
    recordValue.setRecordValidations(new ArrayList<>());
    List<FieldValue> fields = new ArrayList<>();
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    FieldValidation fieldValidation = new FieldValidation();
    fieldValidation.setValidation(validation);
    fieldValidations.add(fieldValidation);
    FieldValidation fieldValidation2 = new FieldValidation();
    fieldValidation2.setValidation(validation2);
    fieldValidations.add(fieldValidation2);
    fieldValue.setFieldValidations(fieldValidations);
    fieldValue.setId(1L);
    fields.add(fieldValue);
    List<RecordValidation> recordValidations = new ArrayList<>();
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setId(1l);
    recordValidation.setValidation(validation2);
    recordValidations.add(recordValidation);
    recordValue.setFields(fields);
    recordValue.setRecordValidations(recordValidations);
    records.add(recordValue);
    datasetValue.getTableValues().remove(1);
    datasetValue.getTableValues().get(0).setId(2L);
    datasetValue.getTableValues().get(0).setIdTableSchema("123123");
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(datasetMetabase.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    validationServiceImpl.validateTable(1L, kieSession);
  }
}
