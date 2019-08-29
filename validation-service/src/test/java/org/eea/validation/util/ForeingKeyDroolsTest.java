/*
 * 
 */
package org.eea.validation.util;

import java.util.ArrayList;
import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ForeingKeyDroolsTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ForeingKeyDroolsTest {

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Checks if is same record true.
   */
  @Test
  public void isSameRecordTrue() {
    RecordValue recordValue = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    List<FieldValue> fieldValueList = new ArrayList<>();
    fieldValue.setValue("value");
    fieldValueList.add(fieldValue);
    recordValue.setFields(fieldValueList);
    ForeingKeyDrools.isInSameRecord("value", recordValue, 0, "value");
  }

  /**
   * Checks if is same record false.
   */
  @Test
  public void isSameRecordFalse() {
    RecordValue recordValue = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    List<FieldValue> fieldValueList = new ArrayList<>();
    fieldValue.setValue("value");
    fieldValueList.add(fieldValue);
    recordValue.setFields(fieldValueList);
    ForeingKeyDrools.isInSameRecord("value", recordValue, 0, "value not equals");
  }
}
