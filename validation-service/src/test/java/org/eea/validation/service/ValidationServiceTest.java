package org.eea.validation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
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

  /**
   * The dataset value.
   */
  private DatasetValue datasetValue;
  private FieldValue fieldValue;
  private RecordValue recordValue;
  private ArrayList<RecordValue> recordValues;
  private TableValue tableValue;
  private ArrayList<TableValue> tableValues;
  private DataSetVO dataSetVO;
  private ArrayList<TableVO> tableVOs;
  private TableVO tableVO;
  private Validation validation;

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
    validation.setLevelError(TypeErrorEnum.ERROR);
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
    TableValidationList.add(tableValidation);
    tableVal.setTableValidations(TableValidationList);
    tableValues.add(tableVal);
    tableValues.add(new TableValue());
    datasetValue.setTableValues(tableValues);
    datasetValue.setDatasetValidations(datasetValidations);
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
        validationServiceImpl.runTableValidations(tableValues, kieSession));

    tableValues.remove(0);
    assertEquals("failed", new ArrayList<>(),
        validationServiceImpl.runTableValidations(tableValues, kieSession));

    assertEquals("failed", new ArrayList<>(),
        validationServiceImpl.runTableValidations(new ArrayList<>(), kieSession));
  }

  /**
   * Test run record validations.
   */
  @Test
  public void testRunRecordValidations() {
    List<RecordValue> records = new ArrayList<>();
    records.add(new RecordValue());
    assertEquals("failed", records.get(0).getRecordValidations(),
        validationServiceImpl.runRecordValidations(records, kieSession));
    assertEquals("failed", new ArrayList<RecordValue>(),
        validationServiceImpl.runRecordValidations(new ArrayList<>(), kieSession));
  }

  /**
   * Test run field validations.
   */
  @Test
  public void testRunFieldValidations() {
    List<FieldValue> fields = new ArrayList<>();
    fields.add(new FieldValue());
    validationServiceImpl.runFieldValidations(fields, kieSession);
    assertEquals("failed", new ArrayList<FieldValidation>(),
        validationServiceImpl.runFieldValidations(fields, kieSession));

    fields.remove(0);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setFieldValidations(new ArrayList<>());
    fields.add(fieldValue);
    validationServiceImpl.runFieldValidations(fields, kieSession);
    assertEquals("failed", new ArrayList<FieldValidation>(),
        validationServiceImpl.runFieldValidations(fields, kieSession));
    fields.remove(0);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(new FieldValidation());
    fieldValue.setFieldValidations(fieldValidations);
    fields.add(fieldValue);
    assertEquals("failed", fields.get(0).getFieldValidations(),
        validationServiceImpl.runFieldValidations(fields, kieSession));
  }

  /**
   * Test load rules knowledge base throw.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testLoadRulesKnowledgeBaseThrow() throws FileNotFoundException, EEAException {
    doThrow(FileNotFoundException.class).when(kieBaseManager).reloadRules(Mockito.any());
    try {
      validationServiceImpl.loadRulesKnowledgeBase(1L);
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
  public void testValidateDataSetData() throws FileNotFoundException, EEAException {

    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(kiebase);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    when(validationDatasetRepository.saveAll(Mockito.any())).thenReturn(null);
    when(validationTableRepository.saveAll(Mockito.any())).thenReturn(null);
    Validation validation = new Validation();
    validation.setId(1L);
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
    when(validationRecordRepository.saveAll(Mockito.any())).thenReturn(null);

    // when(validationFieldRepository.saveAll(Mockito.any())).thenReturn(null);

    validationServiceImpl.validateDataSetData(1L);

  }

  /**
   * Test validate data set data dataflow id excep.
   *
   * @throws EEAException the EEA exception
   * @throws FileNotFoundException the file not found exception
   */
  @Test(expected = EEAException.class)
  public void testValidateDataSetDataDataflowIdExcep() throws EEAException, FileNotFoundException {
    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(null);
    validationServiceImpl.validateDataSetData(1L);
  }

  /**
   * Test validate data set data session excep.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testValidateDataSetDataSessionExcep() throws FileNotFoundException, EEAException {
    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(null);
    validationServiceImpl.validateDataSetData(1L);


  }

  /**
   * Test validate data set data dataset excep.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void testValidateDataSetDataExcep() throws FileNotFoundException, EEAException {
    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(kiebase);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.validateDataSetData(1L);

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
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(kiebase);
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
    when(validationFieldRepository.findFieldValidationsByIdDataset(Mockito.any()))
        .thenReturn(fieldValidations);
    assertNotNull("error", validationServiceImpl.getFieldErrors(1L, new HashMap<String, String>()));
  }

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
    when(validationRecordRepository.findRecordValidationsByIdDataset(Mockito.any()))
        .thenReturn(recordValidations);
    assertNotNull("error",
        validationServiceImpl.getRecordErrors(1L, new HashMap<String, String>()));
  }

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
    when(validationTableRepository.findTableValidationsByIdDataset(Mockito.any()))
        .thenReturn(tableValidations);
    assertNotNull("error", validationServiceImpl.getTableErrors(1L, new HashMap<String, String>()));
  }

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
        validationServiceImpl.getDatasetErrors(datasetValue, new HashMap<String, String>()));
  }


  @Test(expected = EEAException.class)
  public void getDatasetValuebyIdTestException() throws EEAException {
    validationServiceImpl.getDatasetValuebyId(null);
  }

  @Test
  public void getDatasetValuebyIdTestSuccess() throws EEAException {
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    validationServiceImpl.getDatasetValuebyId(1L);
    Mockito.verify(datasetRepository, times(1)).findById(Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(null, null);
  }

  @Test(expected = EEAException.class)
  public void getfindByIdDataSetSchemaTestNullException() throws EEAException {
    validationServiceImpl.getfindByIdDataSetSchema(1L, null);
  }

  @Test
  public void getfindByIdDataSetSchemaTestSuccess() throws EEAException {
    when(schemasRepository.findByIdDataSetSchema(Mockito.any(ObjectId.class)))
        .thenReturn(new DataSetSchema());
    validationServiceImpl.getfindByIdDataSetSchema(1L, new ObjectId("5cf0e9b3b793310e9ceca190"));
    Mockito.verify(schemasRepository, times(1)).findByIdDataSetSchema(Mockito.any());
  }
}
