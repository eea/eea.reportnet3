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

/**
 * The Class RuleOperatorsTest.
 */
public class RuleOperatorsTest {

  /** The rule operators. */
  @InjectMocks
  private RuleOperators ruleOperators;

  /** The field value 1. */
  private FieldValue fieldValue1;

  /** The field value 2. */
  private FieldValue fieldValue2;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
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
    recordValue.setFields(fields);
    FieldValue fieldValue = new FieldValue();
    RuleOperators.setEntity(fieldValue);
    fieldValue.setRecord(recordValue);
    RuleOperators.setEntity("");
    recordValue.setDataProviderCode(null);
    RuleOperators.setEntity(fieldValue);
    recordValue.setDataProviderCode("ES");
    RuleOperators.setEntity(fieldValue);
    recordValue.setDataProviderCode(null);
    RuleOperators.setEntity(recordValue);
    recordValue.setDataProviderCode("ES");
    RuleOperators.setEntity(recordValue);
  }

  /**
   * Gets the value test.
   *
   * @return the value test
   */
  @Test
  public void getValueTest() {
    assertTrue(RuleOperators.recordNull("3"));
  }

  /**
   * Record if then test.
   */
  @Test
  public void recordIfThenLeftBranchTest() {
    assertTrue(RuleOperators.recordIfThen(false, false));
  }

  /**
   * Record if then right branch test.
   */
  @Test
  public void recordIfThenRightBranchTest() {
    assertTrue(RuleOperators.recordIfThen(true, true));
  }

  /**
   * Record if then false test.
   */
  @Test
  public void recordIfThenFalseTest() {
    assertFalse(RuleOperators.recordIfThen(true, false));
  }

  /**
   * Record and test.
   */
  @Test
  public void recordAndTest() {
    assertTrue(RuleOperators.recordAnd(true, true));
  }

  /**
   * Record and left branch test.
   */
  @Test
  public void recordAndLeftBranchTest() {
    assertFalse(RuleOperators.recordAnd(false, true));
  }

  /**
   * Record and right branch test.
   */
  @Test
  public void recordAndRightBranchTest() {
    assertFalse(RuleOperators.recordAnd(true, false));
  }

  /**
   * Record or left branch test.
   */
  @Test
  public void recordOrLeftBranchTest() {
    assertTrue(RuleOperators.recordOr(true, true));
  }

  /**
   * Record or right branch test.
   */
  @Test
  public void recordOrRightBranchTest() {
    assertTrue(RuleOperators.recordOr(false, true));
  }

  /**
   * Record or false test.
   */
  @Test
  public void recordOrFalseTest() {
    assertFalse(RuleOperators.recordOr(false, false));
  }

  /**
   * Record not test.
   */
  @Test
  public void recordNotTest() {
    assertTrue(RuleOperators.recordNot(false));
  }

  /**
   * Record not false test.
   */
  @Test
  public void recordNotFalseTest() {
    assertFalse(RuleOperators.recordNot(true));
  }

  /**
   * Record null test.
   */
  @Test
  public void recordNullTest() {
    fieldValue1.setValue("");
    assertTrue(RuleOperators.recordNull("1"));
  }

  /**
   * Record not null test.
   */
  @Test
  public void recordNotNullTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordNotNull("1"));
  }

  /**
   * Record not null false test.
   */
  @Test
  public void recordNotNullFalseTest() {
    fieldValue1.setValue("");
    assertFalse(RuleOperators.recordNotNull("1"));
  }

  /**
   * Record number equals test.
   */
  @Test
  public void recordNumberEqualsTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberEquals("1", 1));
  }

  /**
   * Record number equals false test.
   */
  @Test
  public void recordNumberEqualsFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberEquals("1", 2));
  }

  /**
   * Record number equals catch test.
   */
  @Test
  public void recordNumberEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberEquals("1", 1));
  }

  /**
   * Record number distinct test.
   */
  @Test
  public void recordNumberDistinctTest() {
    fieldValue1.setValue("2");
    assertTrue(RuleOperators.recordNumberDistinct("1", 1));
  }

  /**
   * Record number distinct false test.
   */
  @Test
  public void recordNumberDistinctFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberDistinct("1", 1));
  }

  /**
   * Record number distinct catch test.
   */
  @Test
  public void recordNumberDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberDistinct("1", 1));
  }

  /**
   * Record number greater than test.
   */
  @Test
  public void recordNumberGreaterThanTest() {
    fieldValue1.setValue("5");
    assertTrue(RuleOperators.recordNumberGreaterThan("1", 1));
  }

  /**
   * Record number greater than false test.
   */
  @Test
  public void recordNumberGreaterThanFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberGreaterThan("1", 5));
  }

  /**
   * Record number greater than catch test.
   */
  @Test
  public void recordNumberGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThan("1", 1));
  }

  /**
   * Record number less than test.
   */
  @Test
  public void recordNumberLessThanTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberLessThan("1", 5));
  }

  /**
   * Record number less than false test.
   */
  @Test
  public void recordNumberLessThanFalseTest() {
    fieldValue1.setValue("5");
    assertFalse(RuleOperators.recordNumberLessThan("1", 1));
  }

  /**
   * Record number less than catch test.
   */
  @Test
  public void recordNumberLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThan("1", 1));
  }

  /**
   * Record number greater than or equals than test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("5");
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record number greater than or equals than false test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 5));
  }

  /**
   * Record number greater than or equals than catch test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record number less than or equals than test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("1", 5));
  }

  /**
   * Record number less than or equals than false test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("5");
    assertFalse(RuleOperators.recordNumberLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record number less than or equals than catch test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record number equals record test.
   */
  @Test
  public void recordNumberEqualsRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  /**
   * Record number equals record false test.
   */
  @Test
  public void recordNumberEqualsRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  /**
   * Record number equals record catch test.
   */
  @Test
  public void recordNumberEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberEqualsRecord("1", "2"));
  }

  /**
   * Record number distinct record test.
   */
  @Test
  public void recordNumberDistinctRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  /**
   * Record number distinct record false test.
   */
  @Test
  public void recordNumberDistinctRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  /**
   * Record number distinct record catch test.
   */
  @Test
  public void recordNumberDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberDistinctRecord("1", "2"));
  }

  /**
   * Record number greater than record test.
   */
  @Test
  public void recordNumberGreaterThanRecordTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  /**
   * Record number greater than record false test.
   */
  @Test
  public void recordNumberGreaterThanRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  /**
   * Record number greater than record catch test.
   */
  @Test
  public void recordNumberGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanRecord("1", "2"));
  }

  /**
   * Record number less than record test.
   */
  @Test
  public void recordNumberLessThanRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  /**
   * Record number less than record false test.
   */
  @Test
  public void recordNumberLessThanRecordFalseTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  /**
   * Record number less than record catch test.
   */
  @Test
  public void recordNumberLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanRecord("1", "2"));
  }

  /**
   * Record number greater than or equals than record test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number greater than or equals than record false test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number greater than or equals than record catch test.
   */
  @Test
  public void recordNumberGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number less than or equals than record test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("1");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number less than or equals than record false test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("5");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number less than or equals than record catch test.
   */
  @Test
  public void recordNumberLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordNumberLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record number matches test.
   */
  @Test
  public void recordNumberMatchesTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordNumberMatches("1", "1"));
  }

  /**
   * Record number matches empty test.
   */
  @Test
  public void recordNumberMatchesEmptyTest() {
    fieldValue1.setValue("");
    assertTrue(RuleOperators.recordNumberMatches("1", "1"));
  }

  /**
   * Record number matches false test.
   */
  @Test
  public void recordNumberMatchesFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberMatches("1", "2"));
  }

  /**
   * Record number matches catch test.
   */
  @Test
  public void recordNumberMatchesCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordNumberMatches("1", "**"));
  }

  /**
   * Record number matches pattern test.
   */
  @Test
  public void recordNumberMatchesPatternTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordNumberMatches("1", "**"));
  }

  /**
   * Record string length test.
   */
  @Test
  public void recordStringLengthTest() {
    fieldValue1.setValue("1");
    assertEquals(Integer.valueOf(1), RuleOperators.recordStringLength("1"));
  }

  /**
   * Record string catch test.
   */
  @Test
  public void recordStringCatchTest() {
    fieldValue1.setValue(null);
    assertNull(RuleOperators.recordStringLength("1"));
  }

  /**
   * Record string length equals test.
   */
  @Test
  public void recordStringLengthEqualsTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  /**
   * Record string length equals false test.
   */
  @Test
  public void recordStringLengthEqualsFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordStringLengthEquals("1", 2));
  }

  /**
   * Record string length equals catch test.
   */
  @Test
  public void recordStringLengthEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthEquals("1", 1));
  }

  /**
   * Record string length distinct test.
   */
  @Test
  public void recordStringLengthDistinctTest() {
    fieldValue1.setValue("1");
    assertTrue(RuleOperators.recordStringLengthDistinct("1", 2));
  }

  /**
   * Record string length distinct false test.
   */
  @Test
  public void recordStringLengthDistinctFalseTest() {
    fieldValue1.setValue("1");
    assertFalse(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  /**
   * Record string length distinct catch test.
   */
  @Test
  public void recordStringLengthDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthDistinct("1", 1));
  }

  /**
   * Record string length greater than test.
   */
  @Test
  public void recordStringLengthGreaterThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }

  /**
   * Record string length greater than false test.
   */
  @Test
  public void recordStringLengthGreaterThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthGreaterThan("1", 5));
  }

  /**
   * Record string length greater than catch test.
   */
  @Test
  public void recordStringLengthGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThan("1", 1));
  }

  /**
   * Record string length less than test.
   */
  @Test
  public void recordStringLengthLessThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthLessThan("1", 5));
  }

  /**
   * Record string length less than false test.
   */
  @Test
  public void recordStringLengthLessThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  /**
   * Record string length less than catch test.
   */
  @Test
  public void recordStringLengthLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThan("1", 1));
  }

  /**
   * Record string length greater than or equals than test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record string length greater than or equals than false test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 5));
  }

  /**
   * Record string length greater than or equals than catch test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record string length less than or equals than test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 5));
  }

  /**
   * Record string length less than or equals than false test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record string length less than or equals than catch test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record string length equals record test.
   */
  @Test
  public void recordStringLengthEqualsRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("4");
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  /**
   * Record string length equals record false test.
   */
  @Test
  public void recordStringLengthEqualsRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  /**
   * Record string length equals record catch test.
   */
  @Test
  public void recordStringLengthEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthEqualsRecord("1", "2"));
  }

  /**
   * Record string length distinct record test.
   */
  @Test
  public void recordStringLengthDistinctRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  /**
   * Record string length distinct record false test.
   */
  @Test
  public void recordStringLengthDistinctRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("4");
    assertFalse(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  /**
   * Record string length distinct record catch test.
   */
  @Test
  public void recordStringLengthDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthDistinctRecord("1", "2"));
  }

  /**
   * Record string length greater than record test.
   */
  @Test
  public void recordStringLengthGreaterThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  /**
   * Record string length greater than record false test.
   */
  @Test
  public void recordStringLengthGreaterThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  /**
   * Record string length greater than record catch test.
   */
  @Test
  public void recordStringLengthGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanRecord("1", "2"));
  }

  /**
   * Record string length less than record test.
   */
  @Test
  public void recordStringLengthLessThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  /**
   * Record string length less than record false test.
   */
  @Test
  public void recordStringLengthLessThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  /**
   * Record string length less than record catch test.
   */
  @Test
  public void recordStringLengthLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanRecord("1", "2"));
  }

  /**
   * Record string length greater than or equals than record test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string length greater than or equals than record false test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertFalse(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string length greater than or equals than record catch test.
   */
  @Test
  public void recordStringLengthGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string length less than or equals than record test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("5");
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string length less than or equals than record false test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string length less than or equals than record catch test.
   */
  @Test
  public void recordStringLengthLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordStringLengthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record string equals test.
   */
  @Test
  public void recordStringEqualsTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringEquals("1", "test"));
  }

  /**
   * Record string equals false test.
   */
  @Test
  public void recordStringEqualsFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringEquals("1", ""));
  }

  /**
   * Record string equals catch test.
   */
  @Test
  public void recordStringEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringEquals("1", null));
  }

  /**
   * Record string equals ignore case test.
   */
  @Test
  public void recordStringEqualsIgnoreCaseTest() {
    fieldValue1.setValue("test");
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", "TEST"));
  }

  /**
   * Record string equals ignore case false test.
   */
  @Test
  public void recordStringEqualsIgnoreCaseFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringEqualsIgnoreCase("1", ""));
  }

  /**
   * Record string equals ignore case catch test.
   */
  @Test
  public void recordStringEqualsIgnoreCaseCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringEqualsIgnoreCase("1", null));
  }

  /**
   * Record string matches test.
   */
  @Test
  public void recordStringMatchesTest() {
    fieldValue1.setValue("test_123");
    assertTrue(RuleOperators.recordStringMatches("1", "test_(.*)"));
  }

  /**
   * Record string matches empty test.
   */
  @Test
  public void recordStringMatchesEmptyTest() {
    fieldValue1.setValue("");
    assertTrue(RuleOperators.recordStringMatches("1", "test_(.*)"));
  }

  /**
   * Record string matches false test.
   */
  @Test
  public void recordStringMatchesFalseTest() {
    fieldValue1.setValue("test");
    assertFalse(RuleOperators.recordStringMatches("1", "test_(.*)"));
  }

  /**
   * Record string matches pattern test.
   */
  @Test
  public void recordStringMatchesPatternTest() {
    fieldValue1.setValue("test_123");
    assertFalse(RuleOperators.recordStringMatches("1", "test_**"));
  }

  /**
   * Record string matches catch test.
   */
  @Test
  public void recordStringMatchesCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordStringMatches("1", "test_**"));
  }

  /**
   * Record string equals record test.
   */
  @Test
  public void recordStringEqualsRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("test");
    assertTrue(RuleOperators.recordStringEqualsRecord("1", "2"));
  }

  /**
   * Record string equals ignore case record test.
   */
  @Test
  public void recordStringEqualsIgnoreCaseRecordTest() {
    fieldValue1.setValue("test");
    fieldValue2.setValue("TEST");
    assertTrue(RuleOperators.recordStringEqualsIgnoreCaseRecord("1", "2"));
  }

  /**
   * Record string matches record test.
   */
  @Test
  public void recordStringMatchesRecordTest() {
    fieldValue1.setValue("test_123");
    fieldValue2.setValue("test_(.*)");
    assertTrue(RuleOperators.recordStringMatchesRecord("1", "2"));
  }

  /**
   * Record day equals test.
   */
  @Test
  public void recordDayEqualsTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }

  /**
   * Record day equals false test.
   */
  @Test
  public void recordDayEqualsFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayEquals("1", 2));
  }

  /**
   * Record day equals catch test.
   */
  @Test
  public void recordDayEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }

  /**
   * Record day distinct test.
   */
  @Test
  public void recordDayDistinctTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayDistinct("1", 2));
  }

  /**
   * Record day distinct false test.
   */
  @Test
  public void recordDayDistinctFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayDistinct("1", 1));
  }

  /**
   * Record day distinct catch test.
   */
  @Test
  public void recordDayDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayDistinct("1", 1));
  }

  /**
   * Record day greater than test.
   */
  @Test
  public void recordDayGreaterThanTest() {
    fieldValue1.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }

  /**
   * Record day greater than false test.
   */
  @Test
  public void recordDayGreaterThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThan("1", 3));
  }

  /**
   * Record day greater than catch test.
   */
  @Test
  public void recordDayGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }

  /**
   * Record day less than test.
   */
  @Test
  public void recordDayLessThanTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayLessThan("1", 2));
  }

  /**
   * Record day less than false test.
   */
  @Test
  public void recordDayLessThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayLessThan("1", 1));
  }

  /**
   * Record day less than catch test.
   */
  @Test
  public void recordDayLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThan("1", 1));
  }

  /**
   * Record day greater than or equals than test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record day greater than or equals than false test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 2));
  }

  /**
   * Record day greater than or equals than catch test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record day less than or equals than test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 2));
  }

  /**
   * Record day less than or equals than false test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record day less than or equals than catch test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record day equals record test.
   */
  @Test
  public void recordDayEqualsRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  /**
   * Record day equals record false test.
   */
  @Test
  public void recordDayEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  /**
   * Record day equals record catch test.
   */
  @Test
  public void recordDayEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }

  /**
   * Record day distinct record test.
   */
  @Test
  public void recordDayDistinctRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  /**
   * Record day distinct record false test.
   */
  @Test
  public void recordDayDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  /**
   * Record day distinct record catch test.
   */
  @Test
  public void recordDayDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }

  /**
   * Record day greater than record test.
   */
  @Test
  public void recordDayGreaterThanRecordTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  /**
   * Record day greater than record false test.
   */
  @Test
  public void recordDayGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  /**
   * Record day greater than record catch test.
   */
  @Test
  public void recordDayGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }

  /**
   * Record day less than record test.
   */
  @Test
  public void recordDayLessThanRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  /**
   * Record day less than record false test.
   */
  @Test
  public void recordDayLessThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  /**
   * Record day less than record catch test.
   */
  @Test
  public void recordDayLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }

  /**
   * Record day greater than or equals than record test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day greater than or equals than record false test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day greater than or equals than record catch test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day less than or equals than record test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2020-12-02");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day less than or equals than record false test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("2020-12-01");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day less than or equals than record catch test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record day equals record number test.
   */
  @Test
  public void recordDayEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  /**
   * Record day equals record number false test.
   */
  @Test
  public void recordDayEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  /**
   * Record day equals record number catch test.
   */
  @Test
  public void recordDayEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }

  /**
   * Record day distinct record number test.
   */
  @Test
  public void recordDayDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  /**
   * Record day distinct record number false test.
   */
  @Test
  public void recordDayDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  /**
   * Record day distinct record number catch test.
   */
  @Test
  public void recordDayDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }

  /**
   * Record day greater than record number test.
   */
  @Test
  public void recordDayGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record day greater than record number false test.
   */
  @Test
  public void recordDayGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record day greater than record number catch test.
   */
  @Test
  public void recordDayGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than record number test.
   */
  @Test
  public void recordDayLessThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than record number false test.
   */
  @Test
  public void recordDayLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than record number catch test.
   */
  @Test
  public void recordDayLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }

  /**
   * Record day greater than or equals than record number test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record day greater than or equals than record number false test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record day greater than or equals than record number catch test.
   */
  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than or equals than record number test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-12-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than or equals than record number false test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-12-02");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record day less than or equals than record number catch test.
   */
  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month equals test.
   */
  @Test
  public void recordMonthEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }

  /**
   * Record month equals false test.
   */
  @Test
  public void recordMonthEqualsFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthEquals("1", 2));
  }

  /**
   * Record month equals catch test.
   */
  @Test
  public void recordMonthEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }

  /**
   * Record month distinct test.
   */
  @Test
  public void recordMonthDistinctTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthDistinct("1", 2));
  }

  /**
   * Record month distinct false test.
   */
  @Test
  public void recordMonthDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthDistinct("1", 1));
  }

  /**
   * Record month distinct catch test.
   */
  @Test
  public void recordMonthDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinct("1", 1));
  }

  /**
   * Record month greater than test.
   */
  @Test
  public void recordMonthGreaterThanTest() {
    fieldValue1.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }

  /**
   * Record month greater than false test.
   */
  @Test
  public void recordMonthGreaterThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthGreaterThan("1", 2));
  }

  /**
   * Record month greater than catch test.
   */
  @Test
  public void recordMonthGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }

  /**
   * Record month less than test.
   */
  @Test
  public void recordMonthLessThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthLessThan("1", 2));
  }

  /**
   * Record month less than false test.
   */
  @Test
  public void recordMonthLessThanFalseTest() {
    fieldValue1.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthLessThan("1", 1));
  }

  /**
   * Record month less than catch test.
   */
  @Test
  public void recordMonthLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThan("1", 1));
  }

  /**
   * Record month greater than or equals than test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record month greater than or equals than false test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 2));
  }

  /**
   * Record month greater than or equals than catch test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }

  /**
   * Record month less than or equals than test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 2));
  }

  /**
   * Record month less than or equals than false test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record month less than or equals than catch test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }

  /**
   * Record month equals record test.
   */
  @Test
  public void recordMonthEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  /**
   * Record month equals record false test.
   */
  @Test
  public void recordMonthEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  /**
   * Record month equals record catch test.
   */
  @Test
  public void recordMonthEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }

  /**
   * Record month distinct record test.
   */
  @Test
  public void recordMonthDistinctRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  /**
   * Record month distinct record false test.
   */
  @Test
  public void recordMonthDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  /**
   * Record month distinct record catch test.
   */
  @Test
  public void recordMonthDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }

  /**
   * Record month greater than record test.
   */
  @Test
  public void recordMonthGreaterThanRecordTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  /**
   * Record month greater than record false test.
   */
  @Test
  public void recordMonthGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  /**
   * Record month greater than record catch test.
   */
  @Test
  public void recordMonthGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  /**
   * Record month less than record test.
   */
  @Test
  public void recordMonthLessThanRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  /**
   * Record month less than record false test.
   */
  @Test
  public void recordMonthLessThanRecordFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  /**
   * Record month less than record catch test.
   */
  @Test
  public void recordMonthLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }

  /**
   * Record month greater than or equals than record test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month greater than or equals than record false test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month greater than or equals than record catch test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month less than or equals than record test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-02-01");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month less than or equals than record false test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month less than or equals than record catch test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record month equals record number test.
   */
  @Test
  public void recordMonthEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  /**
   * Record month equals record number false test.
   */
  @Test
  public void recordMonthEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  /**
   * Record month equals record number catch test.
   */
  @Test
  public void recordMonthEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }

  /**
   * Record month distinct record number test.
   */
  @Test
  public void recordMonthDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  /**
   * Record month distinct record number false test.
   */
  @Test
  public void recordMonthDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  /**
   * Record month distinct record number catch test.
   */
  @Test
  public void recordMonthDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }

  /**
   * Record month greater than record number test.
   */
  @Test
  public void recordMonthGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record month greater than record number false test.
   */
  @Test
  public void recordMonthGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record month greater than record number catch test.
   */
  @Test
  public void recordMonthGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than record number test.
   */
  @Test
  public void recordMonthLessThanRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than record number false test.
   */
  @Test
  public void recordMonthLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than record number catch test.
   */
  @Test
  public void recordMonthLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }

  /**
   * Record month greater than or equals than record number test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month greater than or equals than record number false test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month greater than or equals than record number catch test.
   */
  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than or equals than record number test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than or equals than record number false test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2020-02-01");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record month less than or equals than record number catch test.
   */
  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year equals test.
   */
  @Test
  public void recordYearEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }

  /**
   * Record year equals false test.
   */
  @Test
  public void recordYearEqualsFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearEquals("1", 2020));
  }

  /**
   * Record year equals catch test.
   */
  @Test
  public void recordYearEqualsCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }

  /**
   * Record year distinct test.
   */
  @Test
  public void recordYearDistinctTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }

  /**
   * Record year distinct false test.
   */
  @Test
  public void recordYearDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearDistinct("1", 2020));
  }

  /**
   * Record year distinct catch test.
   */
  @Test
  public void recordYearDistinctCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }

  /**
   * Record year greater than test.
   */
  @Test
  public void recordYearGreaterThanTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  /**
   * Record year greater than false test.
   */
  @Test
  public void recordYearGreaterThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  /**
   * Record year greater than catch test.
   */
  @Test
  public void recordYearGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  /**
   * Record year less than test.
   */
  @Test
  public void recordYearLessThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }

  /**
   * Record year less than false test.
   */
  @Test
  public void recordYearLessThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThan("1", 2020));
  }

  /**
   * Record year less than catch test.
   */
  @Test
  public void recordYearLessThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }

  /**
   * Record year greater than or equals than test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year greater than or equals than false test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year greater than or equals than catch test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year less than or equals than test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year less than or equals than false test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year less than or equals than catch test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }

  /**
   * Record year equals record test.
   */
  @Test
  public void recordYearEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  /**
   * Record year equals record false test.
   */
  @Test
  public void recordYearEqualsRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021-01-01");
    assertFalse(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  /**
   * Record year equals record catch test.
   */
  @Test
  public void recordYearEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }

  /**
   * Record year distinct record test.
   */
  @Test
  public void recordYearDistinctRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  /**
   * Record year distinct record false test.
   */
  @Test
  public void recordYearDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  /**
   * Record year distinct record catch test.
   */
  @Test
  public void recordYearDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }

  /**
   * Record year greater than record test.
   */
  @Test
  public void recordYearGreaterThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  /**
   * Record year greater than record false test.
   */
  @Test
  public void recordYearGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  /**
   * Record year greater than record catch test.
   */
  @Test
  public void recordYearGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }

  /**
   * Record year less than record test.
   */
  @Test
  public void recordYearLessThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  /**
   * Record year less than record false test.
   */
  @Test
  public void recordYearLessThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  /**
   * Record year less than record catch test.
   */
  @Test
  public void recordYearLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }

  /**
   * Record year greater than or equals than record test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year greater than or equals than record false test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year greater than or equals than record catch test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year less than or equals than record test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year less than or equals than record false test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year less than or equals than record catch test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record year equals record number test.
   */
  @Test
  public void recordYearEqualsRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  /**
   * Record year equals record number false test.
   */
  @Test
  public void recordYearEqualsRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021");
    assertFalse(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  /**
   * Record year equals record number catch test.
   */
  @Test
  public void recordYearEqualsRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }

  /**
   * Record year distinct record number test.
   */
  @Test
  public void recordYearDistinctRecordNumberTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2021");
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  /**
   * Record year distinct record number false test.
   */
  @Test
  public void recordYearDistinctRecordNumberFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  /**
   * Record year distinct record number catch test.
   */
  @Test
  public void recordYearDistinctRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }

  /**
   * Record year greater than record number test.
   */
  @Test
  public void recordYearGreaterThanRecordNumberTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record year greater than record number false test.
   */
  @Test
  public void recordYearGreaterThanRecordNumberFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record year greater than record number catch test.
   */
  @Test
  public void recordYearGreaterThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than record number test.
   */
  @Test
  public void recordYearLessThanRecordNumberTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than record number false test.
   */
  @Test
  public void recordYearLessThanRecordNumberFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than record number catch test.
   */
  @Test
  public void recordYearLessThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }

  /**
   * Record year greater than or equals than record number test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year greater than or equals than record number false test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year greater than or equals than record number catch test.
   */
  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than or equals than record number test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than or equals than record number false test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record year less than or equals than record number catch test.
   */
  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  /**
   * Record date equals test.
   */
  @Test
  public void recordDateEqualsTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateEquals("1", "2020-01-01"));
  }

  /**
   * Record date equals false test.
   */
  @Test
  public void recordDateEqualsFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateEquals("1", "2021-01-01"));
  }

  /**
   * Record date equals catch test.
   */
  @Test
  public void recordDateEqualsCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEquals("1", "2"));
  }

  /**
   * Record date distinct test.
   */
  @Test
  public void recordDateDistinctTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateDistinct("1", "2020-01-01"));
  }

  /**
   * Record date distinct false test.
   */
  @Test
  public void recordDateDistinctFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateDistinct("1", "2020-01-01"));
  }

  /**
   * Record date distinct catch test.
   */
  @Test
  public void recordDateDistinctCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinct("1", "2"));
  }

  /**
   * Record date greater than test.
   */
  @Test
  public void recordDateGreaterThanTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2019-01-01"));
  }

  /**
   * Record date greater than false test.
   */
  @Test
  public void recordDateGreaterThanFalseTest() {
    fieldValue1.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThan("1", "2021-01-01"));
  }

  /**
   * Record date greater than catch test.
   */
  @Test
  public void recordDateGreaterThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2"));
  }

  /**
   * Record date less than test.
   */
  @Test
  public void recordDateLessThanTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateLessThan("1", "2020-01-01"));
  }

  /**
   * Record date less than false test.
   */
  @Test
  public void recordDateLessThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordDateLessThan("1", "2020-01-01"));
  }

  /**
   * Record date less than catch test.
   */
  @Test
  public void recordDateLessThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThan("1", "2"));
  }

  /**
   * Record date less than or equals than left branch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanLeftBranchTest() {
    fieldValue1.setValue("2019-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date less than or equals than right branch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date less than or equals than false test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2021-01-01");
    assertFalse(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date less than or equals than catch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2"));
  }

  /**
   * Record date greater than or equals than left branch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanLeftBranchTest() {
    fieldValue1.setValue("2021-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date greater than or equals than right branch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date greater than or equals than false test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanFalseTest() {
    fieldValue1.setValue("2019-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01"));
  }

  /**
   * Record date greater than or equals than catch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2"));
  }

  /**
   * Record date equals record test.
   */
  @Test
  public void recordDateEqualsRecordTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  /**
   * Rrecord date equals record false test.
   */
  @Test
  public void rrecordDateEqualsRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  /**
   * Record date equals record catch test.
   */
  @Test
  public void recordDateEqualsRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }

  /**
   * Record date distinct record test.
   */
  @Test
  public void recordDateDistinctRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  /**
   * Record date distinct record false test.
   */
  @Test
  public void recordDateDistinctRecordFalseTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  /**
   * Record date distinct record catch test.
   */
  @Test
  public void recordDateDistinctRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }

  /**
   * Record date greater than record test.
   */
  @Test
  public void recordDateGreaterThanRecordTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  /**
   * Record date greater than record false test.
   */
  @Test
  public void recordDateGreaterThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  /**
   * Record date greater than record catch test.
   */
  @Test
  public void recordDateGreaterThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }

  /**
   * Record date less than record test.
   */
  @Test
  public void recordDateLessThanRecordTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  /**
   * Record date less than record false test.
   */
  @Test
  public void recordDateLessThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  /**
   * Record date less than record catch test.
   */
  @Test
  public void recordDateLessThanRecordCatchTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }

  /**
   * Record date greater than or equals than record left branch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanRecordLeftBranchTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date greater than or equals than record right branch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanRecordRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date greater than or equals than record false test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date greater than or equals than record catch test.
   */
  @Test
  public void recordDateGreaterThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date less than or equals than record left branch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanRecordLeftBranchTest() {
    fieldValue1.setValue("2019-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date less than or equals than record right branch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanRecordRightBranchTest() {
    fieldValue1.setValue("2020-01-01");
    fieldValue2.setValue("2020-01-01");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date less than or equals than record false test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanRecordFalseTest() {
    fieldValue1.setValue("2021-01-01");
    fieldValue2.setValue("2020-01-01");
    assertFalse(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Record date less than or equals than record catch test.
   */
  @Test
  public void recordDateLessThanOrEqualsThanRecordCatchTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }

  /**
   * Field and test.
   */
  @Test
  public void fieldAndTest() {
    assertTrue(RuleOperators.fieldAnd(true, true));
  }

  /**
   * Field and left branch test.
   */
  @Test
  public void fieldAndLeftBranchTest() {
    assertFalse(RuleOperators.fieldAnd(false, true));
  }

  /**
   * Field and right branch test.
   */
  @Test
  public void fieldAndRightBranchTest() {
    assertFalse(RuleOperators.fieldAnd(true, false));
  }

  /**
   * Field or test.
   */
  @Test
  public void fieldOrTest() {
    assertFalse(RuleOperators.fieldOr(false, false));
  }

  /**
   * Field or left branch test.
   */
  @Test
  public void fieldOrLeftBranchTest() {
    assertTrue(RuleOperators.fieldOr(true, false));
  }

  /**
   * Field or right branch test.
   */
  @Test
  public void fieldOrRightBranchTest() {
    assertTrue(RuleOperators.fieldOr(false, true));
  }

  /**
   * Field not test.
   */
  @Test
  public void fieldNotTest() {
    assertTrue(RuleOperators.fieldNot(false));
  }

  /**
   * Field not false test.
   */
  @Test
  public void fieldNotFalseTest() {
    assertFalse(RuleOperators.fieldNot(true));
  }

  /**
   * Field null test.
   */
  @Test
  public void fieldNullTest() {
    assertTrue(RuleOperators.fieldNull(""));
  }

  /**
   * Field not null test.
   */
  @Test
  public void fieldNotNullTest() {
    assertTrue(RuleOperators.fieldNotNull("test"));
  }

  /**
   * Field not null false test.
   */
  @Test
  public void fieldNotNullFalseTest() {
    assertFalse(RuleOperators.fieldNotNull(""));
  }

  /**
   * Field number equals test.
   */
  @Test
  public void fieldNumberEqualsTest() {
    assertTrue(RuleOperators.fieldNumberEquals("1", 1));
  }

  /**
   * Field number equals catch test.
   */
  @Test
  public void fieldNumberEqualsCatchTest() {
    assertTrue(RuleOperators.fieldNumberEquals("", 1));
  }

  /**
   * Field number distinct test.
   */
  @Test
  public void fieldNumberDistinctTest() {
    assertTrue(RuleOperators.fieldNumberDistinct("1", 2));
  }

  /**
   * Field number distinct false test.
   */
  @Test
  public void fieldNumberDistinctFalseTest() {
    assertFalse(RuleOperators.fieldNumberDistinct("1", 1));
  }

  /**
   * Field number distinct catch test.
   */
  @Test
  public void fieldNumberDistinctCatchTest() {
    assertTrue(RuleOperators.fieldNumberDistinct("", 1));
  }

  /**
   * Field number greater than test.
   */
  @Test
  public void fieldNumberGreaterThanTest() {
    assertTrue(RuleOperators.fieldNumberGreaterThan("2", 1));
  }

  /**
   * Field number greater than false test.
   */
  @Test
  public void fieldNumberGreaterThanFalseTest() {
    assertFalse(RuleOperators.fieldNumberGreaterThan("1", 2));
  }

  /**
   * Field number greater catch test.
   */
  @Test
  public void fieldNumberGreaterCatchTest() {
    assertTrue(RuleOperators.fieldNumberGreaterThan("", 1));
  }

  /**
   * Field number less than test.
   */
  @Test
  public void fieldNumberLessThanTest() {
    assertTrue(RuleOperators.fieldNumberLessThan("1", 2));
  }

  /**
   * Field number less than false test.
   */
  @Test
  public void fieldNumberLessThanFalseTest() {
    assertFalse(RuleOperators.fieldNumberLessThan("2", 1));
  }

  /**
   * Field number less than catch test.
   */
  @Test
  public void fieldNumberLessThanCatchTest() {
    assertTrue(RuleOperators.fieldNumberLessThan("", 1));
  }

  /**
   * Field number greater than or equals than test.
   */
  @Test
  public void fieldNumberGreaterThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldNumberGreaterThanOrEqualsThan("2", 1));
  }

  /**
   * Field number greater than or equals than false test.
   */
  @Test
  public void fieldNumberGreaterThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldNumberGreaterThanOrEqualsThan("1", 2));
  }

  /**
   * Field number greater than or equals than catch test.
   */
  @Test
  public void fieldNumberGreaterThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldNumberGreaterThanOrEqualsThan("", 2));
  }

  /**
   * Field number less than or equals than test.
   */
  @Test
  public void fieldNumberLessThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldNumberLessThanOrEqualsThan("1", 2));
  }

  /**
   * Field number less than or equals than false test.
   */
  @Test
  public void fieldNumberLessThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldNumberLessThanOrEqualsThan("2", 1));
  }

  /**
   * Field number less than or equals than catch test.
   */
  @Test
  public void fieldNumberLessThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldNumberLessThanOrEqualsThan("", 2));
  }

  /**
   * Field number matches test.
   */
  @Test
  public void fieldNumberMatchesTest() {
    assertTrue(RuleOperators.fieldNumberMatches("123", "(.*)2(.*)"));
  }

  /**
   * Field number matches empty test.
   */
  @Test
  public void fieldNumberMatchesEmptyTest() {
    assertTrue(RuleOperators.fieldNumberMatches("", "(.*)2(.*)"));
  }

  /**
   * Field number matches false test.
   */
  @Test
  public void fieldNumberMatchesFalseTest() {
    assertFalse(RuleOperators.fieldNumberMatches("13", "(.*)2(.*)"));
  }

  /**
   * Field number matches catch test.
   */
  @Test
  public void fieldNumberMatchesCatchTest() {
    assertTrue(RuleOperators.fieldNumberMatches(null, "(.*)2(.*)"));
  }

  /**
   * Field string length test.
   */
  @Test
  public void fieldStringLengthTest() {
    assertEquals("4", RuleOperators.fieldStringLength("test"));
  }

  /**
   * Field string length null test.
   */
  @Test
  public void fieldStringLengthNullTest() {
    assertNull(RuleOperators.fieldStringLength(null));
  }

  /**
   * Field string equals test.
   */
  @Test
  public void fieldStringEqualsTest() {
    assertTrue(RuleOperators.fieldStringEquals("test", "test"));
  }

  /**
   * Field string equals catch test.
   */
  @Test
  public void fieldStringEqualsCatchTest() {
    assertTrue(RuleOperators.fieldStringEquals(null, null));
  }

  /**
   * Field string equals ignore case test.
   */
  @Test
  public void fieldStringEqualsIgnoreCaseTest() {
    assertTrue(RuleOperators.fieldStringEqualsIgnoreCase("test", "TEST"));
  }

  /**
   * Field string equals ignore case catch test.
   */
  @Test
  public void fieldStringEqualsIgnoreCaseCatchTest() {
    assertTrue(RuleOperators.fieldStringEqualsIgnoreCase(null, null));
  }

  /**
   * Field string matches test.
   */
  @Test
  public void fieldStringMatchesTest() {
    assertTrue(RuleOperators.fieldStringMatches("ES test", "{%R3_COUNTRY_CODE%} test"));
  }

  /**
   * Field string matches empty test.
   */
  @Test
  public void fieldStringMatchesEmptyTest() {
    assertTrue(RuleOperators.fieldStringMatches("", "{%R3_COUNTRY_CODE%} test"));
  }

  /**
   * Field string matches false test.
   */
  @Test
  public void fieldStringMatchesFalseTest() {
    assertFalse(RuleOperators.fieldStringMatches("ES", "{%R3_COUNTRY_CODE%} test"));
  }

  /**
   * Field string matches catch test.
   */
  @Test
  public void fieldStringMatchesCatchTest() {
    assertTrue(RuleOperators.fieldStringMatches(null, "{%R3_COUNTRY_CODE%} test"));
  }

  /**
   * Field day equals test.
   */
  @Test
  public void fieldDayEqualsTest() {
    assertTrue(RuleOperators.fieldDayEquals("2020-01-01", 1));
  }

  /**
   * Field day equals false test.
   */
  @Test
  public void fieldDayEqualsFalseTest() {
    assertFalse(RuleOperators.fieldDayEquals("2020-01-01", 2));
  }

  /**
   * Field day equals catch test.
   */
  @Test
  public void fieldDayEqualsCatchTest() {
    assertTrue(RuleOperators.fieldDayEquals("test", 1));
  }

  /**
   * Field day distinct test.
   */
  @Test
  public void fieldDayDistinctTest() {
    assertTrue(RuleOperators.fieldDayDistinct("2020-01-01", 2));
  }

  /**
   * Field day distinct false test.
   */
  @Test
  public void fieldDayDistinctFalseTest() {
    assertFalse(RuleOperators.fieldDayDistinct("2020-01-01", 1));
  }

  /**
   * Field day distinct catch test.
   */
  @Test
  public void fieldDayDistinctCatchTest() {
    assertTrue(RuleOperators.fieldDayDistinct("test", 1));
  }

  /**
   * Field day greater than test.
   */
  @Test
  public void fieldDayGreaterThanTest() {
    assertTrue(RuleOperators.fieldDayGreaterThan("2020-01-02", 1));
  }

  /**
   * Field day greater than false test.
   */
  @Test
  public void fieldDayGreaterThanFalseTest() {
    assertFalse(RuleOperators.fieldDayGreaterThan("2020-01-01", 2));
  }

  /**
   * Field day greater than catch test.
   */
  @Test
  public void fieldDayGreaterThanCatchTest() {
    assertTrue(RuleOperators.fieldDayGreaterThan("test", 2));
  }

  /**
   * Field day less than test.
   */
  @Test
  public void fieldDayLessThanTest() {
    assertTrue(RuleOperators.fieldDayLessThan("2020-01-01", 2));
  }

  /**
   * Field day less than false test.
   */
  @Test
  public void fieldDayLessThanFalseTest() {
    assertFalse(RuleOperators.fieldDayLessThan("2020-01-02", 1));
  }

  /**
   * Field day less than catch test.
   */
  @Test
  public void fieldDayLessThanCatchTest() {
    assertTrue(RuleOperators.fieldDayLessThan("test", 1));
  }

  /**
   * Field day greater than or equals than test.
   */
  @Test
  public void fieldDayGreaterThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan("2020-01-02", 1));
  }

  /**
   * Field day greater than or equals than false test.
   */
  @Test
  public void fieldDayGreaterThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldDayGreaterThanOrEqualsThan("2020-01-01", 2));
  }

  /**
   * Field day greater than or equals than catch test.
   */
  @Test
  public void fieldDayGreaterThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan("test", 2));
  }

  /**
   * Field day less than or equals than test.
   */
  @Test
  public void fieldDayLessThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan("2020-01-01", 2));
  }

  /**
   * Field day less than or equals than false test.
   */
  @Test
  public void fieldDayLessThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldDayLessThanOrEqualsThan("2020-01-02", 1));
  }

  /**
   * Field day less than or equals than catch test.
   */
  @Test
  public void fieldDayLessThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan("test", 1));
  }

  /**
   * Field month equals test.
   */
  @Test
  public void fieldMonthEqualsTest() {
    assertTrue(RuleOperators.fieldMonthEquals("2020-01-01", 1));
  }

  /**
   * Field month equals false test.
   */
  @Test
  public void fieldMonthEqualsFalseTest() {
    assertFalse(RuleOperators.fieldMonthEquals("2020-01-01", 2));
  }

  /**
   * Field month equals catch test.
   */
  @Test
  public void fieldMonthEqualsCatchTest() {
    assertTrue(RuleOperators.fieldMonthEquals("test", 1));
  }

  /**
   * Field month distinct test.
   */
  @Test
  public void fieldMonthDistinctTest() {
    assertTrue(RuleOperators.fieldMonthDistinct("2020-01-01", 2));
  }

  /**
   * Field month distinct false test.
   */
  @Test
  public void fieldMonthDistinctFalseTest() {
    assertFalse(RuleOperators.fieldMonthDistinct("2020-01-01", 1));
  }

  /**
   * Field month distinct catch test.
   */
  @Test
  public void fieldMonthDistinctCatchTest() {
    assertTrue(RuleOperators.fieldMonthDistinct("test", 1));
  }

  /**
   * Field month greater than test.
   */
  @Test
  public void fieldMonthGreaterThanTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThan("2020-02-01", 1));
  }

  /**
   * Field month greater than false test.
   */
  @Test
  public void fieldMonthGreaterThanFalseTest() {
    assertFalse(RuleOperators.fieldMonthGreaterThan("2020-01-01", 2));
  }

  /**
   * Field month greater than catch test.
   */
  @Test
  public void fieldMonthGreaterThanCatchTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThan("test", 2));
  }

  /**
   * Field month less than test.
   */
  @Test
  public void fieldMonthLessThanTest() {
    assertTrue(RuleOperators.fieldMonthLessThan("2020-01-01", 2));
  }

  /**
   * Field month less than false test.
   */
  @Test
  public void fieldMonthLessThanFalseTest() {
    assertFalse(RuleOperators.fieldMonthLessThan("2020-02-01", 1));
  }

  /**
   * Field month less than catch test.
   */
  @Test
  public void fieldMonthLessThanCatchTest() {
    assertTrue(RuleOperators.fieldMonthLessThan("test", 1));
  }

  /**
   * Field month greater than or equals than test.
   */
  @Test
  public void fieldMonthGreaterThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan("2020-02-01", 1));
  }

  /**
   * Field month greater than or equals than false test.
   */
  @Test
  public void fieldMonthGreaterThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldMonthGreaterThanOrEqualsThan("2020-01-01", 2));
  }

  /**
   * Field month greater than or equals than catch test.
   */
  @Test
  public void fieldMonthGreaterThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan("test", 2));
  }

  /**
   * Field month less than or equals than test.
   */
  @Test
  public void fieldMonthLessThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan("2020-01-01", 2));
  }

  /**
   * Field month less than or equals than false test.
   */
  @Test
  public void fieldMonthLessThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldMonthLessThanOrEqualsThan("2020-02-01", 1));
  }

  /**
   * Field month less than or equals than catch test.
   */
  @Test
  public void fieldMonthLessThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan("true", 1));
  }

  /**
   * Field year equals test.
   */
  @Test
  public void fieldYearEqualsTest() {
    assertTrue(RuleOperators.fieldYearEquals("2020-02-01", 2020));
  }

  /**
   * Field year equals false test.
   */
  @Test
  public void fieldYearEqualsFalseTest() {
    assertFalse(RuleOperators.fieldYearEquals("2020-02-01", 2021));
  }

  /**
   * Field year equals catch test.
   */
  @Test
  public void fieldYearEqualsCatchTest() {
    assertTrue(RuleOperators.fieldYearEquals("test", 2021));
  }

  /**
   * Field year distinct test.
   */
  @Test
  public void fieldYearDistinctTest() {
    assertTrue(RuleOperators.fieldYearDistinct("2020-02-01", 2021));
  }

  /**
   * Field year distinct false test.
   */
  @Test
  public void fieldYearDistinctFalseTest() {
    assertFalse(RuleOperators.fieldYearDistinct("2020-02-01", 2020));
  }

  /**
   * Field year distinct catch test.
   */
  @Test
  public void fieldYearDistinctCatchTest() {
    assertTrue(RuleOperators.fieldYearDistinct("test", 2020));
  }

  /**
   * Field year greater than test.
   */
  @Test
  public void fieldYearGreaterThanTest() {
    assertTrue(RuleOperators.fieldYearGreaterThan("2020-02-01", 2019));
  }

  /**
   * Field year greater than false test.
   */
  @Test
  public void fieldYearGreaterThanFalseTest() {
    assertFalse(RuleOperators.fieldYearGreaterThan("2020-02-01", 2021));
  }

  /**
   * Field year greater than catch test.
   */
  @Test
  public void fieldYearGreaterThanCatchTest() {
    assertTrue(RuleOperators.fieldYearGreaterThan("test", 2021));
  }

  /**
   * Field year less than test.
   */
  @Test
  public void fieldYearLessThanTest() {
    assertTrue(RuleOperators.fieldYearLessThan("2020-02-01", 2021));
  }

  /**
   * Field year less than false test.
   */
  @Test
  public void fieldYearLessThanFalseTest() {
    assertFalse(RuleOperators.fieldYearLessThan("2020-02-01", 2019));
  }

  /**
   * Field year less than catch test.
   */
  @Test
  public void fieldYearLessThanCatchTest() {
    assertTrue(RuleOperators.fieldYearLessThan("test", 2019));
  }

  /**
   * Field year greater than or equals than test.
   */
  @Test
  public void fieldYearGreaterThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan("2020-02-01", 2019));
  }

  /**
   * Field year greater than or equals than false test.
   */
  @Test
  public void fieldYearGreaterThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldYearGreaterThanOrEqualsThan("2020-02-01", 2021));
  }

  /**
   * Field year greater than or equals than catch test.
   */
  @Test
  public void fieldYearGreaterThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan("test", 2021));
  }

  /**
   * Field year less than or equals than test.
   */
  @Test
  public void fieldYearLessThanOrEqualsThanTest() {
    assertTrue(RuleOperators.fieldYearLessThanOrEqualsThan("2020-02-01", 2021));
  }

  /**
   * Field year less than or equals than false test.
   */
  @Test
  public void fieldYearLessThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldYearLessThanOrEqualsThan("2020-02-01", 2019));
  }

  /**
   * Field year less than or equals than catch test.
   */
  @Test
  public void fieldYearLessThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldYearLessThanOrEqualsThan("test", 2019));
  }

  /**
   * Field date equals test.
   */
  @Test
  public void fieldDateEqualsTest() {
    assertTrue(RuleOperators.fieldDateEquals("2020-01-01", "2020-01-01"));
  }

  /**
   * Field date equals false test.
   */
  @Test
  public void fieldDateEqualsFalseTest() {
    assertFalse(RuleOperators.fieldDateEquals("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date equals catch test.
   */
  @Test
  public void fieldDateEqualsCatchTest() {
    assertTrue(RuleOperators.fieldDateEquals("test", "test"));
  }

  /**
   * Field date distinct test.
   */
  @Test
  public void fieldDateDistinctTest() {
    assertTrue(RuleOperators.fieldDateDistinct("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date distinct false test.
   */
  @Test
  public void fieldDateDistinctFalseTest() {
    assertFalse(RuleOperators.fieldDateDistinct("2020-01-01", "2020-01-01"));
  }

  /**
   * Field date distinct catch test.
   */
  @Test
  public void fieldDateDistinctCatchTest() {
    assertTrue(RuleOperators.fieldDateDistinct("test", "test"));
  }

  /**
   * Field date greater than test.
   */
  @Test
  public void fieldDateGreaterThanTest() {
    assertTrue(RuleOperators.fieldDateGreaterThan("2020-01-02", "2020-01-01"));
  }

  /**
   * Field date greater than false test.
   */
  @Test
  public void fieldDateGreaterThanFalseTest() {
    assertFalse(RuleOperators.fieldDateGreaterThan("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date greater than catch test.
   */
  @Test
  public void fieldDateGreaterThanCatchTest() {
    assertTrue(RuleOperators.fieldDateGreaterThan("test", "test"));
  }

  /**
   * Field date less than test.
   */
  @Test
  public void fieldDateLessThanTest() {
    assertTrue(RuleOperators.fieldDateLessThan("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date less than false test.
   */
  @Test
  public void fieldDateLessThanFalseTest() {
    assertFalse(RuleOperators.fieldDateLessThan("2020-01-02", "2020-01-01"));
  }

  /**
   * Field date less than catch test.
   */
  @Test
  public void fieldDateLessThanCatchTest() {
    assertTrue(RuleOperators.fieldDateLessThan("test", "test"));
  }

  /**
   * Field date greater than or equals than left branch test.
   */
  @Test
  public void fieldDateGreaterThanOrEqualsThanLeftBranchTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-02", "2020-01-01"));
  }

  /**
   * Field date greater than or equals than right branch test.
   */
  @Test
  public void fieldDateGreaterThanOrEqualsThanRightBranchTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-02", "2020-01-02"));
  }

  /**
   * Field date greater than or equals than false test.
   */
  @Test
  public void fieldDateGreaterThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date greater than or equals than catch test.
   */
  @Test
  public void fieldDateGreaterThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("test", "test"));
  }

  /**
   * Field date less than or equals than left branch test.
   */
  @Test
  public void fieldDateLessThanOrEqualsThanLeftBranchTest() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-01", "2020-01-02"));
  }

  /**
   * Field date less than or equals than right branch test.
   */
  @Test
  public void fieldDateLessThanOrEqualsThanRightBranchTest() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-01", "2020-01-01"));
  }

  /**
   * Field date less than or equals than false test.
   */
  @Test
  public void fieldDateLessThanOrEqualsThanFalseTest() {
    assertFalse(RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-02", "2020-01-01"));
  }

  /**
   * Field date less than or equals than catch test.
   */
  @Test
  public void fieldDateLessThanOrEqualsThanCatchTest() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan("test", "test"));
  }


  @Test
  public void recordDayEqualsDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }


  @Test
  public void recordDayEqualsFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayEquals("1", 2));
  }


  @Test
  public void recordDayEqualsCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayEquals("1", 1));
  }


  @Test
  public void recordDayDistinctDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayDistinct("1", 2));
  }


  @Test
  public void recordDayDistinctFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayDistinct("1", 1));
  }


  @Test
  public void recordDayDistinctCatchDateTimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayDistinct("1", 1));
  }


  @Test
  public void recordDayGreaterThanDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }


  @Test
  public void recordDayGreaterThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayGreaterThan("1", 3));
  }


  @Test
  public void recordDayGreaterThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThan("1", 1));
  }


  @Test
  public void recordDayLessThanDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayLessThan("1", 2));
  }


  @Test
  public void recordDayLessThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayLessThan("1", 1));
  }


  @Test
  public void recordDayLessThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThan("1", 1));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 2));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordDayLessThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 2));
  }


  @Test
  public void recordDayLessThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordDayLessThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordDayEqualsRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }


  @Test
  public void recordDayEqualsRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayEqualsRecord("1", "2"));
  }


  @Test
  public void recordDayEqualsRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecord("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayDistinctRecord("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayLessThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2020-12-02 00:00:00");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("2020-12-01 00:00:00");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDayEqualsRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordDayEqualsRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordDayEqualsRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordDayDistinctRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayGreaterThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-12-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-12-02 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDayLessThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDayLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthEqualsDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }


  @Test
  public void recordMonthEqualsFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthEquals("1", 2));
  }


  @Test
  public void recordMonthEqualsCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthEquals("1", 1));
  }


  @Test
  public void recordMonthDistinctDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthDistinct("1", 2));
  }


  @Test
  public void recordMonthDistinctFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthDistinct("1", 1));
  }


  @Test
  public void recordMonthDistinctCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinct("1", 1));
  }


  @Test
  public void recordMonthGreaterThanDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }


  @Test
  public void recordMonthGreaterThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthGreaterThan("1", 2));
  }


  @Test
  public void recordMonthGreaterThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThan("1", 1));
  }


  @Test
  public void recordMonthLessThanDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthLessThan("1", 2));
  }


  @Test
  public void recordMonthLessThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    assertFalse(RuleOperators.recordMonthLessThan("1", 1));
  }


  @Test
  public void recordMonthLessThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThan("1", 1));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 2));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 2));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThan("1", 1));
  }


  @Test
  public void recordMonthEqualsRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }


  @Test
  public void recordMonthEqualsRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertFalse(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }


  @Test
  public void recordMonthEqualsRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecord("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecord("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }

  @Test
  public void recordMonthGreaterThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertFalse(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecord("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-02-01 00:00:00");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordMonthEqualsRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthEqualsRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthEqualsRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthDistinctRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("1");
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertFalse(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthGreaterThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2");
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-02-01 00:00:00");
    fieldValue2.setValue("1");
    assertFalse(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordMonthLessThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordMonthLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearEqualsDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }


  @Test
  public void recordYearEqualsFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearEquals("1", 2020));
  }


  @Test
  public void recordYearEqualsCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearEquals("1", 2020));
  }


  @Test
  public void recordYearDistinctDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }


  @Test
  public void recordYearDistinctFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearDistinct("1", 2020));
  }


  @Test
  public void recordYearDistinctCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearDistinct("1", 2020));
  }


  @Test
  public void recordYearGreaterThanDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }


  @Test
  public void recordYearGreaterThanFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearGreaterThan("1", 2020));
  }


  @Test
  public void recordYearGreaterThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThan("1", 2020));
  }

  @Test
  public void recordYearLessThanDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }


  @Test
  public void recordYearLessThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearLessThan("1", 2020));
  }


  @Test
  public void recordYearLessThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThan("1", 2020));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearLessThanOrEqualsThanDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearLessThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearLessThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThan("1", 2020));
  }


  @Test
  public void recordYearEqualsRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }


  @Test
  public void recordYearEqualsRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2021-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearEqualsRecord("1", "2"));
  }


  @Test
  public void recordYearEqualsRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecord("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearDistinctRecord("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecord("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearLessThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecord("1", "2"));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }

  @Test
  public void recordYearGreaterThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanOrEqualsThanRecordDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordYearLessThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordYearEqualsRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordYearEqualsRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2021");
    assertFalse(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordYearEqualsRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearEqualsRecordNumber("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordNumberDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2021");
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordYearDistinctRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearDistinctRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearLessThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearGreaterThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearGreaterThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020");
    assertFalse(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }

  @Test
  public void recordYearLessThanOrEqualsThanRecordNumberCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordYearLessThanOrEqualsThanRecordNumber("1", "2"));
  }


  @Test
  public void recordDateEqualsDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateEquals("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateEqualsFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateEquals("1", "2021-01-01 00:00:00"));
  }

  @Test
  public void recordDateEqualsCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEquals("1", "2"));
  }


  @Test
  public void recordDateDistinctDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateDistinct("1", "2020-01-01 00:00:00"));
  }

  @Test
  public void recordDateDistinctFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateDistinct("1", "2020-01-01 00:00:00"));
  }

  @Test
  public void recordDateDistinctCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinct("1", "2"));
  }


  @Test
  public void recordDateGreaterThanDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2019-01-01 00:00:00"));
  }


  @Test
  public void recordDateGreaterThanFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateGreaterThan("1", "2021-01-01 00:00:00"));
  }


  @Test
  public void recordDateGreaterThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThan("1", "2"));
  }


  @Test
  public void recordDateLessThanDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateLessThanFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateLessThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateLessThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThan("1", "2"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanLeftBranchDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanRightBranchDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateLessThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThan("1", "2"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanLeftBranchDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanRightBranchDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2020-01-01 00:00:00"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThan("1", "2"));
  }


  @Test
  public void recordDateEqualsRecordDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }


  @Test
  public void rrecordDateEqualsRecordFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateEqualsRecord("1", "2"));
  }


  @Test
  public void recordDateEqualsRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateEqualsRecord("1", "2"));
  }


  @Test
  public void recordDateDistinctRecordDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }


  @Test
  public void recordDateDistinctRecordFalseDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateDistinctRecord("1", "2"));
  }


  @Test
  public void recordDateDistinctRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateDistinctRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanRecordDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanRecordDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateLessThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    fieldValue2.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanRecordLeftBranchDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanRecordRightBranchDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateGreaterThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateGreaterThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanRecordLeftBranchDatetimeTest() {
    fieldValue1.setValue("2019-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanRecordRightBranchDatetimeTest() {
    fieldValue1.setValue("2020-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanRecordFalseDatetimeTest() {
    fieldValue1.setValue("2021-01-01 00:00:00");
    fieldValue2.setValue("2020-01-01 00:00:00");
    assertFalse(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void recordDateLessThanOrEqualsThanRecordCatchDatetimeTest() {
    fieldValue1.setValue(null);
    assertTrue(RuleOperators.recordDateLessThanOrEqualsThanRecord("1", "2"));
  }


  @Test
  public void fieldDayEqualsDatetimeTest() {
    assertTrue(RuleOperators.fieldDayEquals("2020-01-01 00:00:00", 1));
  }


  @Test
  public void fieldDayEqualsFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayEquals("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayEqualsCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayEquals("test", 1));
  }


  @Test
  public void fieldDayDistinctDatetimeTest() {
    assertTrue(RuleOperators.fieldDayDistinct("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayDistinctFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayDistinct("2020-01-01 00:00:00", 1));
  }


  @Test
  public void fieldDayDistinctCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayDistinct("test", 1));
  }


  @Test
  public void fieldDayGreaterThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDayGreaterThan("2020-01-02 00:00:00", 1));
  }


  @Test
  public void fieldDayGreaterThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayGreaterThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayGreaterThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayGreaterThan("test", 2));
  }


  @Test
  public void fieldDayLessThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDayLessThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayLessThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayLessThan("2020-01-02 00:00:00", 1));
  }


  @Test
  public void fieldDayLessThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayLessThan("test", 1));
  }


  @Test
  public void fieldDayGreaterThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan("2020-01-02 00:00:00", 1));
  }


  @Test
  public void fieldDayGreaterThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayGreaterThanOrEqualsThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayGreaterThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayGreaterThanOrEqualsThan("test", 2));
  }


  @Test
  public void fieldDayLessThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldDayLessThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDayLessThanOrEqualsThan("2020-01-02 00:00:00", 1));
  }


  @Test
  public void fieldDayLessThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDayLessThanOrEqualsThan("test", 1));
  }


  @Test
  public void fieldMonthEqualsDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthEquals("2020-01-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthEqualsFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthEquals("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthEqualsCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthEquals("test", 1));
  }


  @Test
  public void fieldMonthDistinctDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthDistinct("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthDistinctFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthDistinct("2020-01-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthDistinctCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthDistinct("test", 1));
  }


  @Test
  public void fieldMonthGreaterThanDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThan("2020-02-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthGreaterThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthGreaterThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthGreaterThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThan("test", 2));
  }


  @Test
  public void fieldMonthLessThanDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthLessThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthLessThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthLessThan("2020-02-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthLessThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthLessThan("test", 1));
  }


  @Test
  public void fieldMonthGreaterThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan("2020-02-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthGreaterThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthGreaterThanOrEqualsThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthGreaterThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthGreaterThanOrEqualsThan("test", 2));
  }


  @Test
  public void fieldMonthLessThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan("2020-01-01 00:00:00", 2));
  }


  @Test
  public void fieldMonthLessThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldMonthLessThanOrEqualsThan("2020-02-01 00:00:00", 1));
  }


  @Test
  public void fieldMonthLessThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldMonthLessThanOrEqualsThan("true", 1));
  }


  @Test
  public void fieldYearEqualsDatetimeTest() {
    assertTrue(RuleOperators.fieldYearEquals("2020-02-01 00:00:00", 2020));
  }


  @Test
  public void fieldYearEqualsFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearEquals("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearEqualsCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearEquals("test", 2021));
  }


  @Test
  public void fieldYearDistinctDatetimeTest() {
    assertTrue(RuleOperators.fieldYearDistinct("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearDistinctFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearDistinct("2020-02-01 00:00:00", 2020));
  }


  @Test
  public void fieldYearDistinctCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearDistinct("test", 2020));
  }


  @Test
  public void fieldYearGreaterThanDatetimeTest() {
    assertTrue(RuleOperators.fieldYearGreaterThan("2020-02-01 00:00:00", 2019));
  }


  @Test
  public void fieldYearGreaterThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearGreaterThan("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearGreaterThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearGreaterThan("test", 2021));
  }


  @Test
  public void fieldYearLessThanDatetimeTest() {
    assertTrue(RuleOperators.fieldYearLessThan("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearLessThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearLessThan("2020-02-01 00:00:00", 2019));
  }


  @Test
  public void fieldYearLessThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearLessThan("test", 2019));
  }


  @Test
  public void fieldYearGreaterThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan("2020-02-01 00:00:00", 2019));
  }


  @Test
  public void fieldYearGreaterThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearGreaterThanOrEqualsThan("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearGreaterThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearGreaterThanOrEqualsThan("test", 2021));
  }


  @Test
  public void fieldYearLessThanOrEqualsThanDatetimeTest() {
    assertTrue(RuleOperators.fieldYearLessThanOrEqualsThan("2020-02-01 00:00:00", 2021));
  }


  @Test
  public void fieldYearLessThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldYearLessThanOrEqualsThan("2020-02-01 00:00:00", 2019));
  }


  @Test
  public void fieldYearLessThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldYearLessThanOrEqualsThan("test", 2019));
  }


  @Test
  public void fieldDateEqualsDatetimeTest() {
    assertTrue(RuleOperators.fieldDateEquals("2020-01-01 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateEqualsFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDateEquals("2020-01-01 00:00:00", "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateEqualsCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateEquals("test", "test"));
  }


  @Test
  public void fieldDateDistinctDatetimeTest() {
    assertTrue(RuleOperators.fieldDateDistinct("2020-01-01 00:00:00", "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateDistinctFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDateDistinct("2020-01-01 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateDistinctCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateDistinct("test", "test"));
  }


  @Test
  public void fieldDateGreaterThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDateGreaterThan("2020-01-02 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateGreaterThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDateGreaterThan("2020-01-01 00:00:00", "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateGreaterThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateGreaterThan("test", "test"));
  }


  @Test
  public void fieldDateLessThanDatetimeTest() {
    assertTrue(RuleOperators.fieldDateLessThan("2020-01-01 00:00:00", "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateLessThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDateLessThan("2020-01-02 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateLessThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateLessThan("test", "test"));
  }


  @Test
  public void fieldDateGreaterThanOrEqualsThanLeftBranchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-02 00:00:00",
        "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateGreaterThanOrEqualsThanRightBranchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-02 00:00:00",
        "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateGreaterThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(RuleOperators.fieldDateGreaterThanOrEqualsThan("2020-01-01 00:00:00",
        "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateGreaterThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateGreaterThanOrEqualsThan("test", "test"));
  }


  @Test
  public void fieldDateLessThanOrEqualsThanLeftBranchDatetimeTest() {
    assertTrue(
        RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-01 00:00:00", "2020-01-02 00:00:00"));
  }


  @Test
  public void fieldDateLessThanOrEqualsThanRightBranchDatetimeTest() {
    assertTrue(
        RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-01 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateLessThanOrEqualsThanFalseDatetimeTest() {
    assertFalse(
        RuleOperators.fieldDateLessThanOrEqualsThan("2020-01-02 00:00:00", "2020-01-01 00:00:00"));
  }


  @Test
  public void fieldDateLessThanOrEqualsThanCatchDatetimeTest() {
    assertTrue(RuleOperators.fieldDateLessThanOrEqualsThan("test", "test"));
  }


}
