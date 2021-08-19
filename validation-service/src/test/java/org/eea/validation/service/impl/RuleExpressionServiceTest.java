package org.eea.validation.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Arrays;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class RuleExpressionServiceTest {

  @InjectMocks
  private RuleExpressionServiceImpl ruleExpressionService;

  private RuleExpressionDTO ruleExpressionDTO;

  private String ruleExpressionString;

  private EntityTypeEnum entityType;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  public RuleExpressionServiceTest(RuleExpressionDTO ruleExpressionDTO, String ruleExpressionString,
      EntityTypeEnum entityType) {
    this.ruleExpressionDTO = ruleExpressionDTO;
    this.ruleExpressionString = ruleExpressionString;
    this.entityType = entityType;
  }

  @Parameters
  @SuppressWarnings({"rawtypes"})
  public static Collection data() {

    String fieldSchemaId1 = "5eb4269d06390651aced7c93"; // Number Integer
    String fieldSchemaId2 = "5ef9d170a99fd533b046abb8"; // Number Decimal
    String fieldSchemaId3 = "5ef4843ca99fd54e786953e0"; // Text
    String fieldSchemaId4 = "5eb4269d06390651aced7c94"; // Text
    String fieldSchemaId5 = "5eb426aa06390652b024ba1c"; // Date
    List<Object> params_1 = Arrays.asList(new Object[] {fieldSchemaId1, 5});
    List<Object> params_2 = Arrays.asList(new Object[] {fieldSchemaId2, 10.1234});
    List<Object> params_3 = Arrays.asList(new Object[] {fieldSchemaId1, fieldSchemaId2});
    List<Object> params_4 = Arrays.asList(new Object[] {fieldSchemaId1, "[0-9]"});
    List<Object> params_5 = Arrays.asList(new Object[] {fieldSchemaId3});
    List<Object> params_6 = Arrays.asList(new Object[] {fieldSchemaId3, 5});
    List<Object> params_7 = Arrays.asList(new Object[] {fieldSchemaId3, fieldSchemaId1});
    List<Object> params_8 = Arrays.asList(new Object[] {fieldSchemaId3, "other \"quoted\" \\"});
    List<Object> params_9 = Arrays.asList(new Object[] {fieldSchemaId3, fieldSchemaId4});
    List<Object> params_10 = Arrays.asList(new Object[] {fieldSchemaId5, 31});
    List<Object> params_11 = Arrays.asList(new Object[] {fieldSchemaId5, fieldSchemaId5});
    List<Object> params_12 = Arrays.asList(new Object[] {"VALUE", 5});

    RuleExpressionDTO recordNumberEquals = new RuleExpressionDTO();
    recordNumberEquals.setOperator(RuleOperatorEnum.RECORD_EQ);
    recordNumberEquals.setParams(params_1);

    RuleExpressionDTO recordNumberDistinct = new RuleExpressionDTO();
    recordNumberDistinct.setOperator(RuleOperatorEnum.RECORD_DIST);
    recordNumberDistinct.setParams(params_2);

    List<Object> params_13 = Arrays.asList(new Object[] {recordNumberEquals, recordNumberDistinct});
    List<Object> params_14 = Arrays.asList(new Object[] {recordNumberEquals});

    RuleExpressionDTO recordIfThen = new RuleExpressionDTO();
    recordIfThen.setOperator(RuleOperatorEnum.RECORD_IF);
    recordIfThen.setParams(params_13);

    RuleExpressionDTO recordAnd = new RuleExpressionDTO();
    recordAnd.setOperator(RuleOperatorEnum.RECORD_AND);
    recordAnd.setParams(params_13);

    RuleExpressionDTO recordOr = new RuleExpressionDTO();
    recordOr.setOperator(RuleOperatorEnum.RECORD_OR);
    recordOr.setParams(params_13);

    RuleExpressionDTO recordNot = new RuleExpressionDTO();
    recordNot.setOperator(RuleOperatorEnum.RECORD_NOT);
    recordNot.setParams(params_14);

    RuleExpressionDTO recordNumberGreaterThan = new RuleExpressionDTO();
    recordNumberGreaterThan.setOperator(RuleOperatorEnum.RECORD_GT);
    recordNumberGreaterThan.setParams(params_1);

    RuleExpressionDTO recordNumberLessThan = new RuleExpressionDTO();
    recordNumberLessThan.setOperator(RuleOperatorEnum.RECORD_LT);
    recordNumberLessThan.setParams(params_1);

    RuleExpressionDTO recordNumberGreaterThanOrEqualsThan = new RuleExpressionDTO();
    recordNumberGreaterThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_GTEQ);
    recordNumberGreaterThanOrEqualsThan.setParams(params_1);

    RuleExpressionDTO recordNumberLessThanOrEqualsThan = new RuleExpressionDTO();
    recordNumberLessThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_LTEQ);
    recordNumberLessThanOrEqualsThan.setParams(params_1);

    RuleExpressionDTO recordNumberEqualsRecord = new RuleExpressionDTO();
    recordNumberEqualsRecord.setOperator(RuleOperatorEnum.RECORD_EQ_RECORD);
    recordNumberEqualsRecord.setParams(params_3);

    RuleExpressionDTO recordNumberDistinctRecord = new RuleExpressionDTO();
    recordNumberDistinctRecord.setOperator(RuleOperatorEnum.RECORD_DIST_RECORD);
    recordNumberDistinctRecord.setParams(params_3);

    RuleExpressionDTO recordNumberGreaterThanRecord = new RuleExpressionDTO();
    recordNumberGreaterThanRecord.setOperator(RuleOperatorEnum.RECORD_GT_RECORD);
    recordNumberGreaterThanRecord.setParams(params_3);

    RuleExpressionDTO recordNumberLessThanRecord = new RuleExpressionDTO();
    recordNumberLessThanRecord.setOperator(RuleOperatorEnum.RECORD_LT_RECORD);
    recordNumberLessThanRecord.setParams(params_3);

    RuleExpressionDTO recordNumberGreaterThanOrEqualsThanRecord = new RuleExpressionDTO();
    recordNumberGreaterThanOrEqualsThanRecord.setOperator(RuleOperatorEnum.RECORD_GTEQ_RECORD);
    recordNumberGreaterThanOrEqualsThanRecord.setParams(params_3);

    RuleExpressionDTO recordNumberLessThanOrEqualsThanRecord = new RuleExpressionDTO();
    recordNumberLessThanOrEqualsThanRecord.setOperator(RuleOperatorEnum.RECORD_LTEQ_RECORD);
    recordNumberLessThanOrEqualsThanRecord.setParams(params_3);

    RuleExpressionDTO recordNumberMatches = new RuleExpressionDTO();
    recordNumberMatches.setOperator(RuleOperatorEnum.RECORD_NUM_MATCH);
    recordNumberMatches.setParams(params_4);

    RuleExpressionDTO recordStringLength = new RuleExpressionDTO();
    recordStringLength.setOperator(RuleOperatorEnum.RECORD_LEN);
    recordStringLength.setParams(params_5);

    RuleExpressionDTO recordStringLengthEquals = new RuleExpressionDTO();
    recordStringLengthEquals.setOperator(RuleOperatorEnum.RECORD_LEN_EQ);
    recordStringLengthEquals.setParams(params_6);

    RuleExpressionDTO recordStringLengthDistinct = new RuleExpressionDTO();
    recordStringLengthDistinct.setOperator(RuleOperatorEnum.RECORD_LEN_DIST);
    recordStringLengthDistinct.setParams(params_6);

    RuleExpressionDTO recordStringLengthGreaterThan = new RuleExpressionDTO();
    recordStringLengthGreaterThan.setOperator(RuleOperatorEnum.RECORD_LEN_GT);
    recordStringLengthGreaterThan.setParams(params_6);

    RuleExpressionDTO recordStringLengthLessThan = new RuleExpressionDTO();
    recordStringLengthLessThan.setOperator(RuleOperatorEnum.RECORD_LEN_LT);
    recordStringLengthLessThan.setParams(params_6);

    RuleExpressionDTO recordStringLengthGreaterThanOrEqualsThan = new RuleExpressionDTO();
    recordStringLengthGreaterThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_LEN_GTEQ);
    recordStringLengthGreaterThanOrEqualsThan.setParams(params_6);

    RuleExpressionDTO recordStringLengthLessThanOrEqualsThan = new RuleExpressionDTO();
    recordStringLengthLessThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_LEN_LTEQ);
    recordStringLengthLessThanOrEqualsThan.setParams(params_6);

    RuleExpressionDTO recordStringLengthEqualsRecord = new RuleExpressionDTO();
    recordStringLengthEqualsRecord.setOperator(RuleOperatorEnum.RECORD_LEN_EQ_RECORD);
    recordStringLengthEqualsRecord.setParams(params_7);

    RuleExpressionDTO recordStringLengthDistinctRecord = new RuleExpressionDTO();
    recordStringLengthDistinctRecord.setOperator(RuleOperatorEnum.RECORD_LEN_DIST_RECORD);
    recordStringLengthDistinctRecord.setParams(params_7);

    RuleExpressionDTO recordStringLengthGreaterThanRecord = new RuleExpressionDTO();
    recordStringLengthGreaterThanRecord.setOperator(RuleOperatorEnum.RECORD_LEN_GT_RECORD);
    recordStringLengthGreaterThanRecord.setParams(params_7);

    RuleExpressionDTO recordStringLengthLessThanRecord = new RuleExpressionDTO();
    recordStringLengthLessThanRecord.setOperator(RuleOperatorEnum.RECORD_LEN_LT_RECORD);
    recordStringLengthLessThanRecord.setParams(params_7);

    RuleExpressionDTO recordStringLengthGreaterThanOrEqualsThanRecord = new RuleExpressionDTO();
    recordStringLengthGreaterThanOrEqualsThanRecord
        .setOperator(RuleOperatorEnum.RECORD_LEN_GTEQ_RECORD);
    recordStringLengthGreaterThanOrEqualsThanRecord.setParams(params_7);

    RuleExpressionDTO recordStringLengthLessThanOrEqualsThanRecord = new RuleExpressionDTO();
    recordStringLengthLessThanOrEqualsThanRecord
        .setOperator(RuleOperatorEnum.RECORD_LEN_LTEQ_RECORD);
    recordStringLengthLessThanOrEqualsThanRecord.setParams(params_7);

    RuleExpressionDTO recordStringEquals = new RuleExpressionDTO();
    recordStringEquals.setOperator(RuleOperatorEnum.RECORD_SEQ);
    recordStringEquals.setParams(params_8);

    RuleExpressionDTO recordStringEqualsIgnoreCase = new RuleExpressionDTO();
    recordStringEqualsIgnoreCase.setOperator(RuleOperatorEnum.RECORD_SEQIC);
    recordStringEqualsIgnoreCase.setParams(params_8);

    RuleExpressionDTO recordStringMatches = new RuleExpressionDTO();
    recordStringMatches.setOperator(RuleOperatorEnum.RECORD_MATCH);
    recordStringMatches.setParams(params_8);

    RuleExpressionDTO recordStringEqualsRecord = new RuleExpressionDTO();
    recordStringEqualsRecord.setOperator(RuleOperatorEnum.RECORD_SEQ_RECORD);
    recordStringEqualsRecord.setParams(params_9);

    RuleExpressionDTO recordStringEqualsIgnoreCaseRecord = new RuleExpressionDTO();
    recordStringEqualsIgnoreCaseRecord.setOperator(RuleOperatorEnum.RECORD_SEQIC_RECORD);
    recordStringEqualsIgnoreCaseRecord.setParams(params_9);

    RuleExpressionDTO recordStringMatchesRecord = new RuleExpressionDTO();
    recordStringMatchesRecord.setOperator(RuleOperatorEnum.RECORD_MATCH_RECORD);
    recordStringMatchesRecord.setParams(params_9);

    RuleExpressionDTO recordDayEquals = new RuleExpressionDTO();
    recordDayEquals.setOperator(RuleOperatorEnum.RECORD_EQ_DAY);
    recordDayEquals.setParams(params_10);

    RuleExpressionDTO recordDayDistinct = new RuleExpressionDTO();
    recordDayDistinct.setOperator(RuleOperatorEnum.RECORD_DIST_DAY);
    recordDayDistinct.setParams(params_10);

    RuleExpressionDTO recordDayGreaterThan = new RuleExpressionDTO();
    recordDayGreaterThan.setOperator(RuleOperatorEnum.RECORD_GT_DAY);
    recordDayGreaterThan.setParams(params_10);

    RuleExpressionDTO recordDayLessThan = new RuleExpressionDTO();
    recordDayLessThan.setOperator(RuleOperatorEnum.RECORD_LT_DAY);
    recordDayLessThan.setParams(params_10);

    RuleExpressionDTO recordDayGreaterThanOrEqualsThan = new RuleExpressionDTO();
    recordDayGreaterThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_GTEQ_DAY);
    recordDayGreaterThanOrEqualsThan.setParams(params_10);

    RuleExpressionDTO recordDayLessThanOrEqualsThan = new RuleExpressionDTO();
    recordDayLessThanOrEqualsThan.setOperator(RuleOperatorEnum.RECORD_LTEQ_DAY);
    recordDayLessThanOrEqualsThan.setParams(params_10);

    RuleExpressionDTO recordDayEqualsRecord = new RuleExpressionDTO();
    recordDayEqualsRecord.setOperator(RuleOperatorEnum.RECORD_EQ_DAY_RECORD);
    recordDayEqualsRecord.setParams(params_11);

    RuleExpressionDTO recordDayDistinctRecord = new RuleExpressionDTO();
    recordDayDistinctRecord.setOperator(RuleOperatorEnum.RECORD_DIST_DAY_RECORD);
    recordDayDistinctRecord.setParams(params_11);

    RuleExpressionDTO recordDayGreaterThanRecord = new RuleExpressionDTO();
    recordDayGreaterThanRecord.setOperator(RuleOperatorEnum.RECORD_GT_DAY_RECORD);
    recordDayGreaterThanRecord.setParams(params_11);

    RuleExpressionDTO fieldNumberEquals = new RuleExpressionDTO();
    fieldNumberEquals.setOperator(RuleOperatorEnum.FIELD_EQ);
    fieldNumberEquals.setParams(params_12);

    RuleExpressionDTO recordNotNull = new RuleExpressionDTO();
    recordNotNull.setOperator(RuleOperatorEnum.RECORD_NOT_NULL);
    recordNotNull.setParams(params_5);

    RuleExpressionDTO fieldNotNull = new RuleExpressionDTO();
    fieldNotNull.setOperator(RuleOperatorEnum.FIELD_NOT_NULL);
    fieldNotNull.setParams(params_5);

    return Arrays.asList(new Object[][] {
        // Case 0: RECORD_IF
        {recordIfThen,
            "RuleOperators.recordIfThen(RuleOperators.recordNumberEquals(\"5eb4269d06390651aced7c93\", 5), RuleOperators.recordNumberDistinct(\"5ef9d170a99fd533b046abb8\", 10.1234))",
            EntityTypeEnum.RECORD},

        // Case 1: RECORD_AND
        {recordAnd,
            "RuleOperators.recordAnd(RuleOperators.recordNumberEquals(\"5eb4269d06390651aced7c93\", 5), RuleOperators.recordNumberDistinct(\"5ef9d170a99fd533b046abb8\", 10.1234))",
            EntityTypeEnum.RECORD},

        // Case 2: RECORD_OR
        {recordOr,
            "RuleOperators.recordOr(RuleOperators.recordNumberEquals(\"5eb4269d06390651aced7c93\", 5), RuleOperators.recordNumberDistinct(\"5ef9d170a99fd533b046abb8\", 10.1234))",
            EntityTypeEnum.RECORD},

        // Case 3: RECORD_NOT
        {recordNot,
            "RuleOperators.recordNot(RuleOperators.recordNumberEquals(\"5eb4269d06390651aced7c93\", 5))",
            EntityTypeEnum.RECORD},

        // Case 4: RECORD_EQ
        {recordNumberEquals, "RuleOperators.recordNumberEquals(\"5eb4269d06390651aced7c93\", 5)",
            EntityTypeEnum.RECORD},

        // Case 5: RECORD_DIST
        {recordNumberDistinct,
            "RuleOperators.recordNumberDistinct(\"5ef9d170a99fd533b046abb8\", 10.1234)",
            EntityTypeEnum.RECORD},

        // Case 6: RECORD_GT
        {recordNumberGreaterThan,
            "RuleOperators.recordNumberGreaterThan(\"5eb4269d06390651aced7c93\", 5)",
            EntityTypeEnum.RECORD},

        // Case 7: RECORD_LT
        {recordNumberLessThan,
            "RuleOperators.recordNumberLessThan(\"5eb4269d06390651aced7c93\", 5)",
            EntityTypeEnum.RECORD},

        // Case 8: RECORD_GTEQ
        {recordNumberGreaterThanOrEqualsThan,
            "RuleOperators.recordNumberGreaterThanOrEqualsThan(\"5eb4269d06390651aced7c93\", 5)",
            EntityTypeEnum.RECORD},

        // Case 9: RECORD_LTEQ
        {recordNumberLessThanOrEqualsThan,
            "RuleOperators.recordNumberLessThanOrEqualsThan(\"5eb4269d06390651aced7c93\", 5)",
            EntityTypeEnum.RECORD},

        // Case 10: RECORD_EQ_RECORD
        {recordNumberEqualsRecord,
            "RuleOperators.recordNumberEqualsRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 11: RECORD_DIST_RECORD
        {recordNumberDistinctRecord,
            "RuleOperators.recordNumberDistinctRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 12: RECORD_GT_RECORD
        {recordNumberGreaterThanRecord,
            "RuleOperators.recordNumberGreaterThanRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 13: RECORD_LT_RECORD
        {recordNumberLessThanRecord,
            "RuleOperators.recordNumberLessThanRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 14: RECORD_GTEQ_RECORD
        {recordNumberGreaterThanOrEqualsThanRecord,
            "RuleOperators.recordNumberGreaterThanOrEqualsThanRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 15: RECORD_LTEQ_RECORD
        {recordNumberLessThanOrEqualsThanRecord,
            "RuleOperators.recordNumberLessThanOrEqualsThanRecord(\"5eb4269d06390651aced7c93\", \"5ef9d170a99fd533b046abb8\")",
            EntityTypeEnum.RECORD},

        // Case 16: RECORD_NUM_MATCH
        {recordNumberMatches,
            "RuleOperators.recordNumberMatches(\"5eb4269d06390651aced7c93\", \"[0-9]\")",
            EntityTypeEnum.RECORD},

        // Case 17: RECORD_LEN
        {recordStringLength, "RuleOperators.recordStringLength(\"5ef4843ca99fd54e786953e0\")",
            EntityTypeEnum.RECORD},

        // Case 18: RECORD_LEN_EQ
        {recordStringLengthEquals,
            "RuleOperators.recordStringLengthEquals(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 19: RECORD_LEN_DIST
        {recordStringLengthDistinct,
            "RuleOperators.recordStringLengthDistinct(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 20: RECORD_LEN_GT
        {recordStringLengthGreaterThan,
            "RuleOperators.recordStringLengthGreaterThan(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 21: RECORD_LEN_LT
        {recordStringLengthLessThan,
            "RuleOperators.recordStringLengthLessThan(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 22: RECORD_LEN_GTEQ
        {recordStringLengthGreaterThanOrEqualsThan,
            "RuleOperators.recordStringLengthGreaterThanOrEqualsThan(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 23: RECORD_LEN_LTEQ
        {recordStringLengthLessThanOrEqualsThan,
            "RuleOperators.recordStringLengthLessThanOrEqualsThan(\"5ef4843ca99fd54e786953e0\", 5)",
            EntityTypeEnum.RECORD},

        // Case 24: RECORD_LEN_EQ_RECORD
        {recordStringLengthEqualsRecord,
            "RuleOperators.recordStringLengthEqualsRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 25: RECORD_LEN_DIST_RECORD
        {recordStringLengthDistinctRecord,
            "RuleOperators.recordStringLengthDistinctRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 26: RECORD_LEN_GT_RECORD
        {recordStringLengthGreaterThanRecord,
            "RuleOperators.recordStringLengthGreaterThanRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 27: RECORD_LEN_LT_RECORD
        {recordStringLengthLessThanRecord,
            "RuleOperators.recordStringLengthLessThanRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 28: RECORD_LEN_GTEQ_RECORD
        {recordStringLengthGreaterThanOrEqualsThanRecord,
            "RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 29: RECORD_LEN_LTEQ_RECORD
        {recordStringLengthLessThanOrEqualsThanRecord,
            "RuleOperators.recordStringLengthLessThanOrEqualsThanRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c93\")",
            EntityTypeEnum.RECORD},

        // Case 30: RECORD_SEQ
        {recordStringEquals,
            "RuleOperators.recordStringEquals(\"5ef4843ca99fd54e786953e0\", \"other \\\"quoted\\\" \\\\\")",
            EntityTypeEnum.RECORD},

        // Case 31: RECORD_SEQIC
        {recordStringEqualsIgnoreCase,
            "RuleOperators.recordStringEqualsIgnoreCase(\"5ef4843ca99fd54e786953e0\", \"other \\\"quoted\\\" \\\\\")",
            EntityTypeEnum.RECORD},

        // Case 32: RECORD_MATCH
        {recordStringMatches,
            "RuleOperators.recordStringMatches(\"5ef4843ca99fd54e786953e0\", \"other \\\"quoted\\\" \\\\\")",
            EntityTypeEnum.RECORD},

        // Case 33: RECORD_SEQ_RECORD
        {recordStringEqualsRecord,
            "RuleOperators.recordStringEqualsRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c94\")",
            EntityTypeEnum.RECORD},

        // Case 34: RECORD_SEQIC_RECORD
        {recordStringEqualsIgnoreCaseRecord,
            "RuleOperators.recordStringEqualsIgnoreCaseRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c94\")",
            EntityTypeEnum.RECORD},

        // Case 35: RECORD_MATCH_RECORD
        {recordStringMatchesRecord,
            "RuleOperators.recordStringMatchesRecord(\"5ef4843ca99fd54e786953e0\", \"5eb4269d06390651aced7c94\")",
            EntityTypeEnum.RECORD},

        // Case 36: RECORD_EQ_DAY
        {recordDayEquals, "RuleOperators.recordDayEquals(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 37: RECORD_DIST_DAY
        {recordDayDistinct, "RuleOperators.recordDayDistinct(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 38: RECORD_GT_DAY
        {recordDayGreaterThan,
            "RuleOperators.recordDayGreaterThan(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 39: RECORD_LT_DAY
        {recordDayLessThan, "RuleOperators.recordDayLessThan(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 40: RECORD_GTEQ_DAY
        {recordDayGreaterThanOrEqualsThan,
            "RuleOperators.recordDayGreaterThanOrEqualsThan(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 41: RECORD_LTEQ_DAY
        {recordDayLessThanOrEqualsThan,
            "RuleOperators.recordDayLessThanOrEqualsThan(\"5eb426aa06390652b024ba1c\", 31)",
            EntityTypeEnum.RECORD},

        // Case 42: RECORD_EQ_DAY_RECORD
        {recordDayEqualsRecord,
            "RuleOperators.recordDayEqualsRecord(\"5eb426aa06390652b024ba1c\", \"5eb426aa06390652b024ba1c\")",
            EntityTypeEnum.RECORD},

        // Case 43: RECORD_DIST_DAY_RECORD
        {recordDayDistinctRecord,
            "RuleOperators.recordDayDistinctRecord(\"5eb426aa06390652b024ba1c\", \"5eb426aa06390652b024ba1c\")",
            EntityTypeEnum.RECORD},

        // Case 44: RECORD_GT_DAY_RECORD
        {recordDayGreaterThanRecord,
            "RuleOperators.recordDayGreaterThanRecord(\"5eb426aa06390652b024ba1c\", \"5eb426aa06390652b024ba1c\")",
            EntityTypeEnum.RECORD},

        // Case 45: FIELD_EQ
        {fieldNumberEquals, "RuleOperators.fieldNumberEquals(value, 5)", EntityTypeEnum.FIELD},

        // Case 46: RECORD_NOT_NULL
        {recordNotNull, "RuleOperators.recordNotNull(\"5ef4843ca99fd54e786953e0\")",
            EntityTypeEnum.RECORD},

        // Case 47: FIELD_NOT_NULL
        {fieldNotNull, "RuleOperators.fieldNotNull(\"5ef4843ca99fd54e786953e0\")",
            EntityTypeEnum.FIELD}

    });
  }

  @Test
  public void convertToDTOTest() {
    RuleExpressionDTO actual = ruleExpressionService.convertToDTO(ruleExpressionString);
    Assert.assertEquals(ruleExpressionDTO.toString(), actual.toString());
  }

  @Test
  public void convertToStringTest() {
    String actual = ruleExpressionService.convertToString(ruleExpressionDTO);
    Assert.assertEquals(ruleExpressionString, actual);
  }

  @Test
  public void isDataTypeCompatibleTest() {
    Map<String, DataType> dataTypeMap = new HashMap<>();
    dataTypeMap.put("5eb4269d06390651aced7c93", DataType.NUMBER_INTEGER);
    dataTypeMap.put("5ef9d170a99fd533b046abb8", DataType.NUMBER_DECIMAL);
    dataTypeMap.put("5ef4843ca99fd54e786953e0", DataType.TEXT);
    dataTypeMap.put("5eb4269d06390651aced7c94", DataType.TEXT);
    dataTypeMap.put("5eb426aa06390652b024ba1c", DataType.DATE);
    dataTypeMap.put("VALUE", DataType.NUMBER_INTEGER);
    Assert.assertTrue(
        ruleExpressionService.isDataTypeCompatible(ruleExpressionDTO, entityType, dataTypeMap));
  }
}
