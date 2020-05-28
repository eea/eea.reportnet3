package org.eea.validation.persistence.data.domain;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
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
  public void recordNumberEqualsLongTrueTest() {
    Assert.assertTrue(recordValue.recordNumberEquals(fieldId1, 1));
  }

  @Test
  public void recordNumberEqualsLongFalseTest() {
    Assert.assertFalse(recordValue.recordNumberEquals(fieldId1, 1.1));
  }

  @Test
  public void recordNumberEqualsDoubleTrueTest() {
    Assert.assertTrue(recordValue.recordNumberEquals(fieldId2, 2.0));
  }

  @Test
  public void recordNumberEqualsFieldNotFoundTest() {
    Assert.assertTrue(recordValue.recordNumberEquals(new ObjectId().toString(), 1));
  }

  @Test
  public void recordNumberDistinctLongTrueTest() {
    Assert.assertTrue(recordValue.recordNumberDistinct(fieldId1, 2));
  }

  @Test
  public void recordNumberDistinctLongFalseTest() {
    Assert.assertFalse(recordValue.recordNumberDistinct(fieldId1, 1.0));
  }

  @Test
  public void recordNumberDistinctDoubleTrueTest() {
    Assert.assertTrue(recordValue.recordNumberDistinct(fieldId2, 2.1));
  }

  @Test
  public void recordNumberDistinctFieldNotFoundTest() {
    Assert.assertTrue(recordValue.recordNumberDistinct(new ObjectId().toString(), 1));
  }

  @Test
  public void recordNumberGreaterThanLongTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberGreaterThan(fieldId1, 0.5));
  }

  @Test
  public void recordNumberGreaterThanLongFalseThanTest() {
    Assert.assertFalse(recordValue.recordNumberGreaterThan(fieldId1, 1.0));
  }

  @Test
  public void recordNumberGreaterThanFieldNotFoundTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberGreaterThan(new ObjectId().toString(), 1.0));
  }

  @Test
  public void recordNumberLessThanLongTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberLessThan(fieldId1, 1.5));
  }

  @Test
  public void recordNumberLessThanLongFalseThanTest() {
    Assert.assertFalse(recordValue.recordNumberLessThan(fieldId1, 0.5));
  }

  @Test
  public void recordNumberLessThanFieldNotFoundTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberLessThan(new ObjectId().toString(), 1.0));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanLongTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberGreaterThanOrEqualsThan(fieldId1, 0.5));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanDoubleTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberGreaterThanOrEqualsThan(fieldId1, 1.0));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanLongFalseThanTest() {
    Assert.assertFalse(recordValue.recordNumberGreaterThanOrEqualsThan(fieldId1, 1.5));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanFieldNotFoundTrueThanTest() {
    Assert.assertTrue(
        recordValue.recordNumberGreaterThanOrEqualsThan(new ObjectId().toString(), 1.0));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanLongTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberLessThanOrEqualsThan(fieldId1, 1.5));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanDoubleTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberLessThanOrEqualsThan(fieldId1, 1.0));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanLongFalseThanTest() {
    Assert.assertFalse(recordValue.recordNumberLessThanOrEqualsThan(fieldId1, 0.5));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanFieldNotFoundTrueThanTest() {
    Assert.assertTrue(recordValue.recordNumberLessThanOrEqualsThan(new ObjectId().toString(), 1.0));
  }
}
