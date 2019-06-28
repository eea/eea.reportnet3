package org.eea.validation.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
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
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.data.repository.ValidationFieldRepository;
import org.eea.validation.persistence.data.repository.ValidationRecordRepository;
import org.eea.validation.persistence.data.repository.ValidationTableRepository;
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
  private ValidationTableRepository validationTableRepository;

  /**
   * The record repository.
   */
  @Mock
  private RecordRepository recordRepository;

  /**
   * The validation record repository.
   */
  @Mock
  private ValidationRecordRepository validationRecordRepository;

  /**
   * The validation field repository.
   */
  @Mock
  private ValidationFieldRepository validationFieldRepository;

  /**
   * The dataset value.
   */
  private DatasetValue datasetValue;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    datasetValue = new DatasetValue();
    datasetValue.setId(1L);
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
   * Testvalidate record.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateField() throws FileNotFoundException, EEAException {

  }

  /**
   * Test validate data set data.
   *
   * @throws FileNotFoundException the file not found exception
   * @throws EEAException the EEA exception
   */
  @Test
  public void testValidateRecord() throws FileNotFoundException, EEAException {

    when(datasetController.getDataFlowIdById(Mockito.any())).thenReturn(1L);
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(kiebase);
    when(datasetRepository.findById(Mockito.any())).thenReturn(Optional.of(datasetValue));
    // when(validationDatasetRepository.saveAll(Mockito.any())).thenReturn(null);
    // when(validationTableRepository.saveAll(Mockito.any())).thenReturn(null);
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
    // when(validationRecordRepository.saveAll(Mockito.any())).thenReturn(null);

    // when(validationFieldRepository.saveAll(Mockito.any())).thenReturn(null);

    validationServiceImpl.validateFields(1L, kieSession);

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
    validationServiceImpl.validateFields(1L, kieSession);
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
    validationServiceImpl.validateFields(1L, kieSession);
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
}
