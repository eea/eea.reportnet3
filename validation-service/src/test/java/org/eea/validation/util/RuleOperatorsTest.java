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

public class RuleOperatorsTest {

  @InjectMocks
  private RuleOperators ruleOperators;

  private FieldValue fieldValue1;

  private FieldValue fieldValue2;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    List<FieldValue> fields = new ArrayList<>();
    fieldValue1 = new FieldValue();
    fieldValue1.setIdFieldSchema("1");
    fieldValue2 = new FieldValue();
    fieldValue2.setIdFieldSchema("2");
    fields.add(fieldValue1);
    fields.add(fieldValue2);
    RecordValue recordValue = new RecordValue();
    recordValue.setDataProviderCode("ES");
    recordValue.setFields(fields);
    RuleOperators.setEntity(recordValue);
  }

  @Test
  public void recordIfThenTest() {
    assertTrue(RuleOperators.recordIfThen(true, true));
  }

  @Test
  public void recordIfThenFalseTest() {
    assertTrue(RuleOperators.recordIfThen(false, true));
  }

  @Test
  public void recordAndTest() {
    assertTrue(RuleOperators.recordAnd(true, true));
  }

  @Test
  public void recordAndLeftBranchTest() {
    assertFalse(RuleOperators.recordAnd(false, true));
  }

  @Test
  public void recordAndRightBranchTest() {
    assertFalse(RuleOperators.recordAnd(true, false));
  }

  @Test
  public void recordOrLeftBranchTest() {
    assertTrue(RuleOperators.recordOr(true, true));
  }

  @Test
  public void recordOrRightBranchTest() {
    assertTrue(RuleOperators.recordOr(false, true));
  }

  @Test
  public void recordOrFalseTest() {
    assertFalse(RuleOperators.recordOr(false, false));
  }

  @Test
  public void recordNotTest() {
    assertTrue(RuleOperators.recordNot(false));
  }

  @Test
  public void recordNotFalseTest() {
    assertFalse(RuleOperators.recordNot(true));
  }

  @Test
  public void recordNullTest() {
    fieldValue1.setValue("");
    assertTrue(RuleOperators.recordNull("1"));
  }

  @Test
  public void recordNotNullTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordNotNull("1"));
  }

  @Test
  public void recordNotNullFalseTest() {
    fieldValue1.setValue("");
    assertFalse(RuleOperators.recordNotNull("1"));
  }

  @Test
  public void recordNumberEqualsTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberEquals("1", 1));
  }

  @Test
  public void recordNumberEqualsFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberEquals("1", 2));
  }

  @Test
  public void recordNumberEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberEquals("1", 1));
  }

  @Test
  public void recordNumberDistinctTest() {
    fieldValue1.setValue("2");
    assertTrue(RuleOperators.recordNumberDistinct("1", 1));
  }

  @Test
  public void recordNumberDistinctFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberDistinct("1", 1));
  }

  @Test
  public void recordNumberDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberDistinct("1", 1));
  }

  @Test
  public void recordNumberGreaterThanTest() {
    fieldValue1.setValue("5");
    assertTrue(RuleOperators.recordNumberGreaterThan("1", 1));
  }

  @Test
  public void recordNumberGreaterThanFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberGreaterThan("1", 5));
  }

  @Test
  public void recordNumberGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThan("1", 1));
  }

  @Test
  public void recordNumberLessThanTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberLessThan("1", 5));
  }

  @Test
  public void recordNumberLessThanFalseTest() {
    fieldValue1.setValue("5");
    assertFalse(RuleOperators.recordNumberLessThan("1", 1));
  }

  @Test
  public void recordNumberLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThan("1", 1));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("5");
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 5));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("1", 5));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("5");
    assertFalse(RuleOperators.recordNumberLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordNumberEqualsRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  @Test
  public void recordNumberEqualsRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  @Test
  public void recordNumberEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  @Test
  public void recordNumberDistinctRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  @Test
  public void recordNumberDistinctRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  @Test
  public void recordNumberDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanRecordTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanRecordFalseTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordNumberMatchesTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberMatches("1", "1"));
  }

  @Test
  public void recordNumberMatchesFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberMatches("1", "2"));
  }

  @Test
  public void recordNumberMatchesCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberMatches("1", "**"));
  }

  @Test
  public void recordNumberMatchesPatternTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberMatches("1", "**"));
  }

  @Test
  public void recordStringLengthTest() {
    fieldValue1.setValue("1");
    assertEquals(Integer.valueOf(1), RuleOperators.recordStringLength("1"));
  }

  @Test
  public void recordStringCatchTest() {
    fieldValue1.setValue(null);
    assertNull(RuleOperators.recordStringLength("1"));
  }

  @Test
  public void recordStringLengthEqualsTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  @Test
  public void recordStringLengthEqualsFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordStringLengthEquals("1", 2));
  }

  @Test
  public void recordStringLengthEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  @Test
  public void recordStringLengthDistinctTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordStringLengthDistinct("1", 2));
  }

  @Test
  public void recordStringLengthDistinctFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  @Test
  public void recordStringLengthDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  @Test
  public void recordStringLengthGreaterThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }

  @Test
  public void recordStringLengthGreaterThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthGreaterThan("1", 5));
  }

  @Test
  public void recordStringLengthGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }

  @Test
  public void recordStringLengthLessThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthLessThan("1", 5));
  }

  @Test
  public void recordStringLengthLessThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  @Test
  public void recordStringLengthLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 5));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 5));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordStringLengthEqualsRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("4");
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  @Test
  public void recordStringLengthEqualsRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  @Test
  public void recordStringLengthEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  @Test
  public void recordStringLengthDistinctRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  @Test
  public void recordStringLengthDistinctRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("4");
    assertFalse(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  @Test
  public void recordStringLengthDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordStringEqualsTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringEquals("1", "test"));
  }

  @Test
  public void recordStringEqualsFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringEquals("1", ""));
  }

  @Test
  public void recordStringEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringEquals("1", null));
  }

  @Test
  public void recordStringEqualsIgnoreCaseTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", "TEST"));
  }

  @Test
  public void recordStringEqualsIgnoreCaseFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringEqualsIgnoreCase("1", ""));
  }

  @Test
  public void recordStringEqualsIgnoreCaseCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", null));
  }

  @Test
  public void recordStringMatchesTest() {
    fieldValue1.setValue("test_123");
    assertTrue(RuleOperators.recordStringMatches("1", "test_(.*)"));
  }

  @Test
  public void recordStringMatchesPatternTest() {
    fieldValue1.setValue("test_123");
    assertFalse(RuleOperators.recordStringMatches("1", "test_**"));
  }

  @Test
  public void recordStringMatchesCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringMatches("1", "test_**"));
  }

  @Test
  public void recordStringEqualsRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("test");
    assertTrue(RuleOperators.recordStringEqualsRecord("1", "2"));
  }

  @Test
  public void recordStringEqualsIgnoreCaseRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("TEST");
    assertTrue(RuleOperators.recordStringEqualsIgnoreCaseRecord("1", "2"));
  }

  @Test
  public void recordStringMatchesRecordTest() {
    fieldValue1.setValue("test_123");
    fieldValue2.setValue("test_(.*)");
    assertTrue(RuleOperators.recordStringMatchesRecord("1", "2"));
  }

  @Test
  public void recordDayEqualsTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }

  @Test
  public void recordDayEqualsFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayEquals("1", 2));
  }

  @Test
  public void recordDayEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }

  @Test
  public void recordDayDistinctTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayDistinct("1", 2));
  }

  @Test
  public void recordDayDistinctFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayDistinct("1", 1));
  }

  @Test
  public void recordDayDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayDistinct("1", 1));
  }

  @Test
  public void recordDayGreaterThanTest() {
    fieldValue1.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }

  @Test
  public void recordDayGreaterThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThan("1", 3));
  }

  @Test
  public void recordDayGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }

  @Test
  public void recordDayLessThanTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayLessThan("1", 2));
  }

  @Test
  public void recordDayLessThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayLessThan("1", 1));
  }

  @Test
  public void recordDayLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThan("1", 1));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 2));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordDayLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 2));
  }

  @Test
  public void recordDayLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordDayLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordDayEqualsRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  @Test
  public void recordDayEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  @Test
  public void recordDayEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDayEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordDayEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordDayEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordDayDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }

  @Test
  public void recordMonthEqualsFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthEquals("1", 2));
  }

  @Test
  public void recordMonthEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }

  @Test
  public void recordMonthDistinctTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthDistinct("1", 2));
  }

  @Test
  public void recordMonthDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthDistinct("1", 1));
  }

  @Test
  public void recordMonthDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinct("1", 1));
  }

  @Test
  public void recordMonthGreaterThanTest() {
    fieldValue1.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }

  @Test
  public void recordMonthGreaterThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthGreaterThan("1", 2));
  }

  @Test
  public void recordMonthGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }

  @Test
  public void recordMonthLessThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthLessThan("1", 2));
  }

  @Test
  public void recordMonthLessThanFalseTest() {
    fieldValue1.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthLessThan("1", 1));
  }

  @Test
  public void recordMonthLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThan("1", 1));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 2));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 2));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }

  @Test
  public void recordMonthEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }

  @Test
  public void recordYearEqualsFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearEquals("1", 2020));
  }

  @Test
  public void recordYearEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }

  @Test
  public void recordYearDistinctTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }

  @Test
  public void recordYearDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearDistinct("1", 2020));
  }

  @Test
  public void recordYearDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }

  @Test
  public void recordYearGreaterThanTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  @Test
  public void recordYearGreaterThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  @Test
  public void recordYearGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  @Test
  public void recordYearLessThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }

  @Test
  public void recordYearLessThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThan("1", 2020));
  }

  @Test
  public void recordYearLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  @Test
  public void recordYearEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  @Test
  public void recordYearEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021-01-01");
    assertFalse(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  @Test
  public void recordYearEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordYearEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021");
    assertFalse(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordYearEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021");
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordYearDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordNumberTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordDateEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateEquals("1", "2020-01-01"));
  }

  @Test
  public void recordDateEqualsFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateEquals("1", "2021-01-01"));
  }

  @Test
  public void recordDateEqualsCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEquals("1", "2"));
  }

  @Test
  public void recordDateDistinctTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateDistinct("1", "2020-01-01"));
  }

  @Test
  public void recordDateDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateDistinct("1", "2020-01-01"));
  }

  @Test
  public void recordDateDistinctCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinct("1", "2"));
  }

  @Test
  public void recordDateGreaterThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2019-01-01"));
  }

  @Test
  public void recordDateGreaterThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThan("1", "2021-01-01"));
  }

  @Test
  public void recordDateGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2"));
  }

  @Test
  public void recordDateLessThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateLessThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateLessThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordDateLessThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateLessThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThan("1", "2"));
  }

  @Test
  public void recordDateLessThanOrEqualsThanLeftBranchTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateLessThanOrEqualsThanRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanLeftBranchTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2"));
  }

  @Test
  public void recordDateEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  @Test
  public void rrecordDateEqualsRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  @Test
  public void recordDateEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  @Test
  public void recordDateDistinctRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  @Test
  public void recordDateDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  @Test
  public void recordDateDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordDateLessThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  @Test
  public void recordDateLessThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  @Test
  public void recordDateLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanRecordLeftBranchTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanRecordRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordDateGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }
}
