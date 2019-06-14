package org.eea.validation.service;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.service.impl.ValidationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceTest {

  @InjectMocks
  private ValidationServiceImpl validationServiceImpl;
  @Mock
  private KieSession kieSession;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testRunDatasetValidations() {
    DatasetValue dataset = new DatasetValue();
    dataset.setDatasetValidations(new ArrayList<>());
    assertEquals("Good result", dataset.getDatasetValidations(),
        validationServiceImpl.runDatasetValidations(dataset));
  }


  @Test
  public void testRunTableValidations() {
    List<TableValue> tableValues = new ArrayList<>();
    TableValue tableVal = new TableValue();
    tableVal.setTableValidations(new ArrayList<TableValidation>());
    tableValues.add(tableVal);
    tableValues.add(new TableValue());
    assertEquals("Good result", tableVal.getTableValidations(),
        validationServiceImpl.runTableValidations(tableValues));

    assertEquals("Good result", new ArrayList<>(),
        validationServiceImpl.runTableValidations(new ArrayList<>()));
  }

  @Test
  public void testRunRecordValidations() {
    List<RecordValue> records = new ArrayList<RecordValue>();
    records.add(new RecordValue());
    assertEquals("Good result", records.get(0).getRecordValidations(),
        validationServiceImpl.runRecordValidations(records));
    assertEquals("Good result", new ArrayList<RecordValue>(),
        validationServiceImpl.runRecordValidations(new ArrayList<RecordValue>()));
  }

  @Test
  public void testRunFieldValidations() {
    List<FieldValue> fields = new ArrayList<FieldValue>();
    fields.add(new FieldValue());
    validationServiceImpl.runFieldValidations(fields);
    assertEquals("Good result", new ArrayList<FieldValidation>(),
        validationServiceImpl.runFieldValidations(fields));

    fields.remove(0);
    FieldValue fieldValue = new FieldValue();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(new FieldValidation());
    fieldValue.setFieldValidations(fieldValidations);
    fields.add(fieldValue);
    assertEquals("Good result", fields.get(0).getFieldValidations(),
        validationServiceImpl.runFieldValidations(fields));
  }
}
