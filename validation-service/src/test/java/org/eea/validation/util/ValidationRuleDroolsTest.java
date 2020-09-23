package org.eea.validation.util;

import static org.junit.Assert.assertNotNull;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.junit.Test;

public class ValidationRuleDroolsTest {


  @Test
  public void testFillValidationDatasetValue() {
    DatasetValue dataset = new DatasetValue();
    ValidationRuleDrools.fillValidation(dataset, "", "ERROR", "", "");
    assertNotNull(dataset.getDatasetValidations());
  }

  @Test
  public void testFillValidationTableValue() {
    TableValue table = new TableValue();
    ValidationRuleDrools.fillValidation(table, "", "ERROR", "", "");
    assertNotNull(table.getTableValidations());
  }

  @Test
  public void testFillValidationFieldValue() {
    FieldValue field = new FieldValue();
    ValidationRuleDrools.fillValidation(field, "", "ERROR", "", "");
    assertNotNull(field.getFieldValidations());
  }

  @Test
  public void testFillValidationRecordValueStringStringStringString() {
    RecordValue record = new RecordValue();
    ValidationRuleDrools.fillValidation(record, "", "ERROR", "", "");
    assertNotNull(record.getRecordValidations());

  }

}
