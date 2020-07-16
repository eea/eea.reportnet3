package org.eea.validation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class RuleOperatorsTest {

  @InjectMocks
  private RuleOperators ruleOperators;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    List<FieldValue> fields = new ArrayList<>();
    FieldValue field = new FieldValue();
    field.setIdFieldSchema("1");
    field.setValue("1");
    FieldValue field2 = new FieldValue();
    field2.setIdFieldSchema("4");
    field2.setValue("1999-05-05");
    fields.add(field2);
    fields.add(field);
    ReflectionTestUtils.setField(ruleOperators, "fields", fields);
  }

  @Test
  public void testSetEntityRecordValue() {
    assertTrue(RuleOperators.setEntity(new RecordValue()));
  }

  @Test
  public void testSetEntityFieldValue() {
    FieldValue field = new FieldValue();
    field.setRecord(new RecordValue());
    assertTrue(RuleOperators.setEntity(field));
  }

  @Test
  public void testSetEntityObject() {
    assertTrue(RuleOperators.setEntity(new Object()));
  }

  @Test
  public void testRecordIfThenValue() {
    assertFalse(RuleOperators.recordIfThen(Boolean.TRUE, Boolean.FALSE));
  }

  @Test
  public void testRecordIfThenValueTrue() {
    assertTrue(RuleOperators.recordIfThen(Boolean.FALSE, Boolean.FALSE));
  }

  @Test
  public void testRecordAnd() {
    assertTrue(RuleOperators.recordAnd(Boolean.TRUE, Boolean.TRUE));
  }

  @Test
  public void testRecordOrFirst() {
    assertTrue(RuleOperators.recordOr(Boolean.TRUE, Boolean.FALSE));
  }

  @Test
  public void testRecordOr() {
    assertTrue(RuleOperators.recordOr(Boolean.FALSE, Boolean.TRUE));
  }


  @Test
  public void testRecordNot() {
    assertTrue(RuleOperators.recordNot(Boolean.FALSE));
  }

  @Test
  public void testRecordNumberEquals() {
    assertTrue(RuleOperators.recordNumberEquals("1", 1));
  }

  @Test
  public void testRecordNumberEqualsNot() {
    assertTrue(RuleOperators.recordNumberEquals("2", 2));
  }


  @Test
  public void testRecordNumberDistinct() {
    assertFalse(RuleOperators.recordNumberDistinct("1", 1));
  }

  @Test
  public void testRecordNumberDistinctTrue() {
    assertTrue(RuleOperators.recordNumberDistinct("2", 2));
  }

  @Test
  public void testRecordNumberGreaterThan() {
    assertFalse(RuleOperators.recordNumberGreaterThan("1", 1));
  }

  @Test
  public void testRecordNumberGreaterThanTrue() {
    assertTrue(RuleOperators.recordNumberGreaterThan("2", 2));
  }

  @Test
  public void testRecordNumberLessThan() {
    assertFalse(RuleOperators.recordNumberLessThan("1", 1));
  }

  @Test
  public void testRecordNumberLessTrue() {
    assertTrue(RuleOperators.recordNumberLessThan("2", 2));
  }

  @Test
  public void testRecordNumberGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordNumberGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("2", 1));
  }

  @Test
  public void testRecordNumberLessThanOrEqualsThan() {
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordNumberLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("2", 1));
  }

  @Test
  public void testRecordNumberEqualsRecord() {
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "1"));
  }

  @Test
  public void testRecordNumberEqualsRecordCatch() {
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  @Test
  public void testRecordNumberDistinctRecord() {
    assertFalse(RuleOperators.recordNumberDistinctRecord("1", "1"));
  }

  @Test
  public void testRecordNumberDistinctRecordCatch() {
    assertTrue(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  @Test
  public void testRecordNumberGreaterThanRecord() {
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("2", "1"));
  }

  @Test
  public void testRecordNumberGreaterThanRecordCatch() {
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("2", "2"));
  }

  @Test
  public void testRecordNumberLessThanRecord() {
    assertTrue(RuleOperators.recordNumberLessThanRecord("2", "2"));
  }

  @Test
  public void testRecordNumberLessThanRecordCatch() {
    assertTrue(RuleOperators.recordNumberLessThanRecord("2", "1"));
  }


  @Test
  public void testRecordNumberGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("2", "1"));
  }

  @Test
  public void testRecordNumberGreaterThanOrEqualsThanRecordCatch() {
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("2", "2"));
  }


  @Test
  public void testRecordNumberLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("2", "1"));
  }

  @Test
  public void testRecordNumberLessThanOrEqualsThanRecordCatch() {
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("2", "2"));
  }

  @Test
  public void testRecordNumberMatches() {
    assertTrue(RuleOperators.recordNumberMatches("1", "1"));
  }

  @Test
  public void testRecordNumberMatchesCatch() {
    assertTrue(RuleOperators.recordNumberMatches("2", null));
  }

  @Test
  public void testRecordStringLength() {
    assertEquals(Integer.valueOf(0), RuleOperators.recordStringLength(null));
  }


  @Test
  public void testRecordStringLengthCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertNull(RuleOperators.recordStringLength(null));
  }

  @Test
  public void testRecordStringLengthEquals() {
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  @Test
  public void testRecordStringLengthEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  @Test
  public void testRecordStringLengthDistinct() {
    assertFalse(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  @Test
  public void testRecordStringLengthDistinctCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  @Test
  public void testRecordStringLengthGreaterThan() {
    assertFalse(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }

  @Test
  public void testRecordStringLengthGreaterThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }


  @Test
  public void testRecordStringLengthLessThan() {
    assertFalse(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  @Test
  public void testRecordStringLengthLessThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  @Test
  public void testRecordStringLengthGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordStringLengthGreaterThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordStringLengthLessThanOrEqualsThan() {
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordStringLengthLessThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void testRecordStringLengthEqualsRecord() {
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthEqualsRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthDistinctRecord() {
    assertFalse(RuleOperators.recordStringLengthDistinctRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthDistinctRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthDistinctRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthGreaterThanRecord() {
    assertFalse(RuleOperators.recordStringLengthGreaterThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthGreaterThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthLessThanRecord() {
    assertFalse(RuleOperators.recordStringLengthLessThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthLessThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthLessThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthGreaterThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringLengthLessThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "1"));
  }

  @Test
  public void testRecordStringEquals() {
    assertTrue(RuleOperators.recordStringEquals("1", "1"));
  }

  @Test
  public void testRecordStringEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringEquals("1", "1"));
  }

  @Test
  public void testRecordStringEqualsIgnoreCase() {
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", "1"));
  }

  @Test
  public void testRecordStringEqualsIgnoreCaseCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", "1"));
  }

  @Test
  public void testRecordStringMatches() {
    assertFalse(RuleOperators.recordStringMatches("1", "$COUNTRY_CODE"));
  }

  @Test
  public void testRecordStringMatchesCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringMatches(null, null));
  }

  @Test
  public void testRecordStringEqualsRecord() {
    assertTrue(RuleOperators.recordStringEqualsRecord("1", "1"));
  }

  @Test
  public void testRecordStringEqualsRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringEqualsRecord("1", "1"));
  }

  @Test
  public void testRecordStringEqualsIgnoreCaseRecord() {
    assertTrue(RuleOperators.recordStringEqualsIgnoreCaseRecord("1", "1"));
  }

  @Test
  public void testRecordStringEqualsIgnoreCaseRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringEqualsIgnoreCaseRecord("1", "1"));
  }

  @Test
  public void testRecordStringMatchesRecord() {
    assertTrue(RuleOperators.recordStringMatchesRecord("1", "1"));
  }

  @Test
  public void testRecordStringMatchesRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordStringMatchesRecord("1", "1"));
  }

  @Test
  public void testRecordDayEquals() {
    assertFalse(RuleOperators.recordDayEquals("4", 1));
  }

  @Test
  public void testRecordDayEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }

  @Test
  public void testRecordDayDistinct() {
    assertTrue(RuleOperators.recordDayDistinct("4", 1));
  }

  @Test
  public void testRecordDayDistinctCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayDistinct("1", 1));
  }

  @Test
  public void testRecordDayGreaterThan() {
    assertTrue(RuleOperators.recordDayGreaterThan("4", 1));
  }

  @Test
  public void testRecordDayGreaterThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }

  @Test
  public void testRecordDayLessThan() {
    assertFalse(RuleOperators.recordDayLessThan("4", 1));
  }

  @Test
  public void testRecordDayLessThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThan("4", 1));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordDayLessThanOrEqualsThan() {
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordDayLessThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordDayEqualsRecord() {
    assertTrue(RuleOperators.recordDayEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordDayEqualsRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordDayDistinctRecord() {
    assertFalse(RuleOperators.recordDayDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordDayDistinctRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayDistinctRecord("4", "4"));
  }


  @Test
  public void testRecordDayGreaterThanRecord() {
    assertFalse(RuleOperators.recordDayGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayGreaterThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayLessThanRecord() {
    assertFalse(RuleOperators.recordDayLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayLessThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayLessThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDayEqualsRecordNumber() {
    assertFalse(RuleOperators.recordDayEqualsRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayEqualsRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("4", "4"));
  }

  @Test
  public void testRecordDayDistinctRecordNumber() {
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayDistinctRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("4", "4"));
  }

  @Test
  public void testRecordDayGreaterThanRecordNumber() {
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayGreaterThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayLessThanRecordNumber() {
    assertFalse(RuleOperators.recordDayLessThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayLessThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThanRecordNumber() {
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayGreaterThanOrEqualsThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("4", "1"));
  }


  @Test
  public void testRecordDayLessThanOrEqualsThanRecordNumber() {
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordDayLessThanOrEqualsThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthEquals() {
    assertFalse(RuleOperators.recordMonthEquals("4", 1));
  }

  @Test
  public void testRecordMonthEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthEquals("4", 1));
  }

  @Test
  public void testRecordMonthDistinct() {
    assertTrue(RuleOperators.recordMonthDistinct("4", 1));
  }

  @Test
  public void testRecordMonthDistinctCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthDistinct("4", 1));
  }

  @Test
  public void testRecordMonthGreaterThan() {
    assertTrue(RuleOperators.recordMonthGreaterThan("4", 1));
  }

  @Test
  public void testRecordMonthGreaterThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThan("4", 1));
  }

  @Test
  public void testRecordMonthLessThan() {
    assertFalse(RuleOperators.recordMonthLessThan("4", 1));
  }

  @Test
  public void testRecordMonthLessThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThan("4", 1));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordMonthLessThanOrEqualsThan() {
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordMonthLessThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordMonthEqualsRecord() {
    assertTrue(RuleOperators.recordMonthEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordMonthEqualsRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordMonthDistinctRecord() {
    assertFalse(RuleOperators.recordMonthDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordMonthDistinctRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordMonthGreaterThanRecord() {
    assertFalse(RuleOperators.recordMonthGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthGreaterThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthLessThanRecord() {
    assertFalse(RuleOperators.recordMonthLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthLessThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthLessThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordMonthEqualsRecordNumber() {
    assertFalse(RuleOperators.recordMonthEqualsRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthEqualsRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("4", "4"));
  }

  @Test
  public void testRecordMonthDistinctRecordNumber() {
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthDistinctRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("4", "4"));
  }

  @Test
  public void testRecordMonthGreaterThanRecordNumber() {
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthGreaterThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthLessThanRecordNumber() {
    assertFalse(RuleOperators.recordMonthLessThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthLessThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThanRecordNumber() {
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthGreaterThanOrEqualsThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("4", "1"));
  }


  @Test
  public void testRecordMonthLessThanOrEqualsThanRecordNumber() {
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordMonthLessThanOrEqualsThanRecordNumberCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearEquals() {
    assertFalse(RuleOperators.recordYearEquals("4", 1));
  }

  @Test
  public void testRecordYearEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearEquals("4", 1));
  }

  @Test
  public void testRecordYearDistinct() {
    assertTrue(RuleOperators.recordYearDistinct("4", 1));
  }

  @Test
  public void testRecordYearDistinctCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearDistinct("4", 1));
  }

  @Test
  public void testRecordYearGreaterThan() {
    assertTrue(RuleOperators.recordYearGreaterThan("4", 1));
  }

  @Test
  public void testRecordYearGreaterThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearGreaterThan("4", 1));
  }

  @Test
  public void testRecordYearLessThan() {
    assertFalse(RuleOperators.recordYearLessThan("4", 1));
  }

  @Test
  public void testRecordYearLessThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearLessThan("4", 1));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThan() {
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThanCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("4", 1));
  }

  @Test
  public void testRecordYearEqualsRecord() {
    assertTrue(RuleOperators.recordYearEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordYearEqualsRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearEqualsRecord("4", "1"));
  }

  @Test
  public void testRecordYearDistinctRecord() {
    assertFalse(RuleOperators.recordYearDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordYearDistinctRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanRecord() {
    assertFalse(RuleOperators.recordYearGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearLessThanRecord() {
    assertFalse(RuleOperators.recordYearLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearLessThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThanRecordCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordYearEqualsRecordNumber() {
    assertFalse(RuleOperators.recordYearEqualsRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearEqualsRecordNumberCatch() {
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("4", "4"));
  }

  @Test
  public void testRecordYearDistinctRecordNumber() {
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearDistinctRecordNumberCatch() {
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanRecordNumber() {
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearGreaterThanRecordNumberCatcch() {
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("4", "4"));
  }

  @Test
  public void testRecordYearLessThanRecordNumber() {
    assertFalse(RuleOperators.recordYearLessThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearLessThanRecordNumberCatch() {
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("4", "4"));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThanRecordNumber() {
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearGreaterThanOrEqualsThanRecordNumberCatch() {
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("4", "4"));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThanRecordNumber() {
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("4", "1"));
  }

  @Test
  public void testRecordYearLessThanOrEqualsThanRecordNumberCatch() {
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("4", "4"));
  }

  @Test
  public void testRecordDateEquals() {
    assertFalse(RuleOperators.recordDateEquals("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateEqualsCatch() {
    ReflectionTestUtils.setField(ruleOperators, "fields", null);
    assertTrue(RuleOperators.recordDateEquals("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateDistinct() {
    assertTrue(RuleOperators.recordDateDistinct("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateDistinctCatch() {
    assertTrue(RuleOperators.recordDateDistinct("4", "2"));
  }

  @Test
  public void testRecordDateGreaterThan() {
    assertFalse(RuleOperators.recordDateGreaterThan("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateGreaterThanCatch() {
    assertTrue(RuleOperators.recordDateGreaterThan("4", "2"));
  }

  @Test
  public void testRecordDateLessThan() {
    assertTrue(RuleOperators.recordDateLessThan("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateLessThanCatch() {
    assertTrue(RuleOperators.recordDateLessThan("4", "1"));
  }

  @Test
  public void testRecordDateLessThanOrEqualsThan() {
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("4", "9"));
  }

  @Test
  public void testRecordDateGreaterThanOrEqualsThan() {
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThan("4", "1999-09-09"));
  }

  @Test
  public void testRecordDateGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("4", "1"));
  }

  @Test
  public void testRecordDateEqualsRecord() {
    assertTrue(RuleOperators.recordDateEqualsRecord("4", "4"));
  }

  @Test
  public void testRecordDateEqualsRecordCatch() {
    assertTrue(RuleOperators.recordDateEqualsRecord("4", "1"));
  }

  @Test
  public void testRecordDateDistinctRecord() {
    assertFalse(RuleOperators.recordDateDistinctRecord("4", "4"));
  }

  @Test
  public void testRecordDateDistinctRecordCatch() {
    assertTrue(RuleOperators.recordDateDistinctRecord("4", "1"));
  }

  @Test
  public void testRecordDateGreaterThanRecord() {
    assertFalse(RuleOperators.recordDateGreaterThanRecord("4", "4"));
  }

  @Test
  public void testRecordDateGreaterThanRecordCatch() {
    assertTrue(RuleOperators.recordDateGreaterThanRecord("4", "1"));
  }

  @Test
  public void testRecordDateLessThanRecord() {
    assertFalse(RuleOperators.recordDateLessThanRecord("4", "4"));
  }

  @Test
  public void testRecordDateLessThanRecordCatch() {
    assertTrue(RuleOperators.recordDateLessThanRecord("4", "1"));
  }

  @Test
  public void testRecordDateGreaterThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDateGreaterThanOrEqualsThanRecordCatch() {
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("4", "1"));
  }

  @Test
  public void testRecordDateLessThanOrEqualsThanRecord() {
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("4", "4"));
  }

  @Test
  public void testRecordDateLessThanOrEqualsThanRecordCatch() {
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("4", "1"));
  }

  @Test
  public void testFieldAnd() {
    assertTrue(RuleOperators.fieldAnd(Boolean.TRUE, Boolean.TRUE));
  }

  @Test
  public void testFieldOr() {
    assertFalse(RuleOperators.fieldOr(Boolean.FALSE, Boolean.FALSE));
  }

  @Test
  public void testFieldNot() {
    assertTrue(RuleOperators.fieldNot(Boolean.FALSE));
  }

  @Test
  public void testFieldNumberEquals() {
    assertTrue(RuleOperators.fieldNumberEquals("1", 1));
  }

  @Test
  public void testFieldNumberEqualsCatch() {
    assertTrue(RuleOperators.fieldNumberEquals("a", 1));
  }

  @Test
  public void testFieldNumberDistinct() {
    assertFalse(RuleOperators.fieldNumberDistinct("1", 1));
  }

  @Test
  public void testFieldNumberDistinctCatch() {
    assertTrue(RuleOperators.fieldNumberDistinct("a", 1));
  }

  @Test
  public void testFieldNumberGreaterThan() {
    assertFalse(RuleOperators.fieldNumberGreaterThan("1", 1));
  }

  @Test
  public void testFieldNumberGreaterThanCatch() {
    assertTrue(RuleOperators.fieldNumberGreaterThan("a", 1));
  }

  @Test
  public void testFieldNumberLessThan() {
    assertFalse(RuleOperators.fieldNumberLessThan("1", 1));
  }

  @Test
  public void testFieldNumberLessThanCatch() {
    assertTrue(RuleOperators.fieldNumberLessThan("a", 1));
  }

  @Test
  public void testFieldNumberGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldNumberGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void testFieldNumberGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldNumberGreaterThanOrEqualsThan("a", 1));
  }

  @Test
  public void testFieldNumberLessThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldNumberLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void testFieldNumberLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldNumberLessThanOrEqualsThan("a", 1));
  }

  @Test
  public void testFieldNumberMatches() {
    assertFalse(RuleOperators.fieldNumberMatches("1", ""));
  }

  @Test
  public void testFieldNumberMatchesCatch() {
    assertTrue(RuleOperators.fieldNumberMatches(null, "a"));
  }

  @Test
  public void testFieldStringLength() {
    assertEquals("0", RuleOperators.fieldStringLength(""));
  }

  @Test
  public void testFieldStringLengthCatch() {
    assertNull(RuleOperators.fieldStringLength(null));
  }

  @Test
  public void testFieldStringEquals() {
    assertTrue(RuleOperators.fieldStringEquals("a", "a"));
  }

  @Test
  public void testFieldStringEqualsCatch() {
    assertTrue(RuleOperators.fieldStringEquals(null, "a"));
  }

  @Test
  public void testFieldStringEqualsIgnoreCase() {
    assertTrue(RuleOperators.fieldStringEqualsIgnoreCase("a", "a"));
  }

  @Test
  public void testFieldStringEqualsIgnoreCaseCatch() {
    assertTrue(RuleOperators.fieldStringEqualsIgnoreCase(null, "a"));
  }

  @Test
  public void testFieldStringMatches() {
    assertTrue(RuleOperators.fieldStringMatches("a", "a"));
  }

  @Test
  public void testFieldStringMatchesCatch() {
    assertTrue(RuleOperators.fieldStringMatches(null, "a"));
  }

  @Test
  public void testFieldDayEquals() {
    assertTrue(RuleOperators.fieldDayEquals("1999-09-09", 9));
  }

  @Test
  public void testFieldDayEqualsCatch() {
    assertTrue(RuleOperators.fieldDayEquals(null, null));
  }

  @Test
  public void testFieldDayDistinct() {
    assertFalse(RuleOperators.fieldDayDistinct("1999-09-09", 9));
  }

  @Test
  public void testFieldDayDistinctCatch() {
    assertTrue(RuleOperators.fieldDayDistinct(null, null));
  }

  @Test
  public void testFieldDayGreaterThan() {
    assertFalse(RuleOperators.fieldDayGreaterThan("1999-09-09", 9));
  }

  @Test
  public void testFieldDayGreaterThanCatch() {
    assertTrue(RuleOperators.fieldDayGreaterThan(null, null));
  }

  @Test
  public void testFieldDayLessThan() {
    assertFalse(RuleOperators.fieldDayLessThan("1999-09-09", 9));
  }

  @Test
  public void testFieldDayLessThanCatch() {
    assertTrue(RuleOperators.fieldDayLessThan(null, null));
  }

  @Test
  public void testFieldDayGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldDayGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldDayLessThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldDayLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldMonthEquals() {
    assertTrue(RuleOperators.fieldMonthEquals("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthEqualsCatch() {
    assertTrue(RuleOperators.fieldMonthEquals(null, null));
  }

  @Test
  public void testFieldMonthDistinct() {
    assertFalse(RuleOperators.fieldMonthDistinct("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthDistinctCatch() {
    assertTrue(RuleOperators.fieldMonthDistinct(null, null));
  }

  @Test
  public void testFieldMonthGreaterThan() {
    assertFalse(RuleOperators.fieldMonthGreaterThan("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthGreaterThanCatch() {
    assertTrue(RuleOperators.fieldMonthGreaterThan(null, null));
  }

  @Test
  public void testFieldMonthLessThan() {
    assertFalse(RuleOperators.fieldMonthLessThan("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthLessThanCatch() {
    assertTrue(RuleOperators.fieldMonthLessThan(null, null));
  }

  @Test
  public void testFieldMonthGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldMonthLessThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldMonthLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldYearEquals() {
    assertFalse(RuleOperators.fieldYearEquals("1999-09-09", 9));
  }

  @Test
  public void testFieldYearEqualsCatch() {
    assertTrue(RuleOperators.fieldYearEquals(null, null));
  }

  @Test
  public void testFieldYearDistinct() {
    assertTrue(RuleOperators.fieldYearDistinct("1999-09-09", 9));
  }

  @Test
  public void testFieldYearDistinctCatch() {
    assertTrue(RuleOperators.fieldYearDistinct(null, null));
  }

  @Test
  public void testFieldYearGreaterThan() {
    assertTrue(RuleOperators.fieldYearGreaterThan("1999-09-09", 9));
  }

  @Test
  public void testFieldYearGreaterThanCatch() {
    assertTrue(RuleOperators.fieldYearGreaterThan(null, null));
  }

  @Test
  public void testFieldYearLessThan() {
    assertFalse(RuleOperators.fieldYearLessThan("1999-09-09", 9));
  }

  @Test
  public void testFieldYearLessThanCatch() {
    assertTrue(RuleOperators.fieldYearLessThan(null, null));
  }

  @Test
  public void testFieldYearGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldYearGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldYearLessThanOrEqualsThan() {
    assertFalse(RuleOperators.fieldYearLessThanOrEqualsThan("1999-09-09", 9));
  }

  @Test
  public void testFieldYearLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldYearLessThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldDateEquals() {
    assertTrue(RuleOperators.fieldDateEquals("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateEqualsCatch() {
    assertTrue(RuleOperators.fieldDateEquals(null, null));
  }

  @Test
  public void testFieldDateDistinct() {
    assertFalse(RuleOperators.fieldDateDistinct("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateDistinctCatch() {
    assertTrue(RuleOperators.fieldDateDistinct(null, null));
  }

  @Test
  public void testFieldDateGreaterThan() {
    assertFalse(RuleOperators.fieldDateGreaterThan("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateGreaterThanCatch() {
    assertTrue(RuleOperators.fieldDateGreaterThan(null, null));
  }

  @Test
  public void testFieldDateLessThan() {
    assertFalse(RuleOperators.fieldDateLessThan("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateLessThanCatch() {
    assertTrue(RuleOperators.fieldDateLessThan(null, null));
  }

  @Test
  public void testFieldDateGreaterThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateGreaterThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan(null, null));
  }

  @Test
  public void testFieldDateLessThanOrEqualsThan() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan("1999-09-09", "1999-09-09"));
  }

  @Test
  public void testFieldDateLessThanOrEqualsThanCatch() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan(null, null));
  }

}
