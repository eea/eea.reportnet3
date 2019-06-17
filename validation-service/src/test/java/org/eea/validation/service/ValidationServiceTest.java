package org.eea.validation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.eea.exception.EEAException;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
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

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

  @InjectMocks
  private ValidationServiceImpl validationServiceImpl;
  @Mock
  private KieSession kieSession;
  @Mock
  private KieBaseManager kieBaseManager;
  @Mock
  private DatasetRepository datasetRepository;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testRunDatasetValidations() {
    DatasetValue dataset = new DatasetValue();
    dataset.setDatasetValidations(new ArrayList<>());
    assertEquals("failed", dataset.getDatasetValidations(),
        validationServiceImpl.runDatasetValidations(dataset, kieSession));
  }


  @Test
  public void testRunTableValidations() {
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableVal = new TableValue();
    tableVal.setTableValidations(new ArrayList<TableValidation>());
    tableValues.add(tableVal);
    tableValues.add(new TableValue());
    assertEquals("failed", tableVal.getTableValidations(),
        validationServiceImpl.runTableValidations(tableValues, kieSession));

    assertEquals("failed", new ArrayList<>(),
        validationServiceImpl.runTableValidations(new ArrayList<>(), kieSession));
  }

  @Test
  public void testRunRecordValidations() {
    List<RecordValue> records = new ArrayList<RecordValue>();
    records.add(new RecordValue());
    assertEquals("failed", records.get(0).getRecordValidations(),
        validationServiceImpl.runRecordValidations(records, kieSession));
    assertEquals("failed", new ArrayList<RecordValue>(),
        validationServiceImpl.runRecordValidations(new ArrayList<RecordValue>(), kieSession));
  }

  @Test
  public void testRunFieldValidations() {
    List<FieldValue> fields = new ArrayList<FieldValue>();
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

  @Test
  public void testLoadRulesKnowledgeBaseThrow() throws FileNotFoundException {
    doThrow(FileNotFoundException.class).when(kieBaseManager).reloadRules(Mockito.any());
    assertNull("failed", validationServiceImpl.loadRulesKnowledgeBase(1L));
  }

  // @Test
  public void testValidateDataSetData() throws EEAException {
    validationServiceImpl.validateDataSetData(1L);

  }

  @Test
  public void testLoadRulesKnowledgeBase() throws FileNotFoundException {
    KieHelper kieHelper = new KieHelper();
    KieBase kiebase = kieHelper.build();
    when(kieBaseManager.reloadRules(Mockito.any())).thenReturn(kiebase);
    validationServiceImpl.loadRulesKnowledgeBase(1L);
  }

  @Test
  public void testDeleteAllValidation() {
    doNothing().when(datasetRepository).deleteValidationTable();
    validationServiceImpl.deleteAllValidation(1L);
  }
}
