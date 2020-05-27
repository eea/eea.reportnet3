package org.eea.validation.persistence.data.domain;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecordValueTest {

  @InjectMocks
  private RecordValue recordValue;

  private final String fieldId1 = "5ec68fbecfde9c37a857d338";
  private final String fieldId2 = "5ec68fbecfde9c37a857d339";
  private final String fieldId3 = "5ec68fbecfde9c37a857d340";
  private final String fieldId4 = "5ec68fbecfde9c37a857d341";
  private final String fieldValue1 = "1";
  private final String fieldValue2 = "2.0";
  private final String fieldValue3 = "hello";
  private final String fieldValue4 = "2020-05-27";

  @Before
  public void initMocks() {
    Map<String, String> fieldsMap = new HashMap<>();
    fieldsMap.put(fieldId1, fieldValue1); // Long
    fieldsMap.put(fieldId2, fieldValue2); // Double
    fieldsMap.put(fieldId3, fieldValue3); // String
    fieldsMap.put(fieldId4, fieldValue4); // Date
    recordValue.setFieldsMap(fieldsMap);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void recordIfThenTrueTrueTest() {
    Assert.assertTrue(recordValue.recordIfThen(true, true));
  }

  @Test
  public void recordIfThenTrueFalseTest() {
    Assert.assertFalse(recordValue.recordIfThen(true, false));
  }

  @Test
  public void recordIfThenFalseFalseTest() {
    Assert.assertTrue(recordValue.recordIfThen(false, true));
  }

  @Test
  public void recordIfThenFalseTrueTest() {
    Assert.assertTrue(recordValue.recordIfThen(false, false));
  }

  @Test
  public void recordAndTrueTrueTest() {
    Assert.assertTrue(recordValue.recordAnd(true, true));
  }

  @Test
  public void recordAndTrueFalseTest() {
    Assert.assertFalse(recordValue.recordAnd(true, false));
  }

  @Test
  public void recordAndFalseTrueTest() {
    Assert.assertFalse(recordValue.recordAnd(false, true));
  }

  @Test
  public void recordAndFalseFalseTest() {
    Assert.assertFalse(recordValue.recordAnd(false, false));
  }

  @Test
  public void recordOrTrueTrueTest() {
    Assert.assertTrue(recordValue.recordOr(true, true));
  }

  @Test
  public void recordOrTrueFalseTest() {
    Assert.assertTrue(recordValue.recordOr(true, false));
  }

  @Test
  public void recordOrFalseTrueTest() {
    Assert.assertTrue(recordValue.recordOr(false, true));
  }

  @Test
  public void recordOrFalseFalseTest() {
    Assert.assertFalse(recordValue.recordOr(false, false));
  }

  @Test
  public void recordNotTrueTest() {
    Assert.assertFalse(recordValue.recordNot(true));
  }

  @Test
  public void recordNotFalseTest() {
    Assert.assertTrue(recordValue.recordNot(false));
  }

  @Test
  public void recordNumberEqualsTest() {
    Assert.assertTrue(recordValue.recordNumberEquals(fieldId1, 1));
  }
}
