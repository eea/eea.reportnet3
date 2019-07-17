package org.eea.validation.util;

import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleDroolsTest {

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks
  private ValidationRuleDrools validationRuleDrools;

  @Test
  public void testDataset() {
    validationRuleDrools.fillValidation(new DatasetValue(), "ERROR DATASET", "ERROR", "12312ASDA",
        "1234");
  }

  @Test
  public void testTable() {
    validationRuleDrools.fillValidation(new TableValue(), "ERROR TABLE", "ERROR", "12312ASDA",
        "1234");
  }

  @Test
  public void testRecord() {
    validationRuleDrools.fillValidation(new RecordValue(), "ERROR RECORD", "ERROR", "12312ASDA",
        "1234");
  }

  @Test
  public void testField() {
    validationRuleDrools.fillValidation(new FieldValue(), "ERROR FIELD", "ERROR", "12312ASDA",
        "1234");
  }

}
