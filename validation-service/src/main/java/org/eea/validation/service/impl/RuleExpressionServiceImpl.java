package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import org.eea.validation.service.RuleExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** The Class RuleExpressionServiceImpl. */
@Service
public class RuleExpressionServiceImpl implements RuleExpressionService {

  /** The Constant VALUE */
  private static final String VALUE = "VALUE";

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant REGEX_DATE. */
  private static final String REGEX_DATE = "[0-9]{4}-(?:0[0-9]|1[0-2])-(?:[0-2][0-9]|3[01])";

  /** The Constant REGEX_DATETIME. */
  private static final String REGEX_DATETIME =
      "[0-9]{4}-(?:0[0-9]|1[0-2])-(?:[0-2][0-9]|3[01]) \\d{2}:\\d{2}:\\d{2}";

  /**
   * Converts a String containing a rule expression (Java code) into a RuleExpressionDTO data
   * structure.
   *
   * @param ruleExpressionString the rule expression string
   * @return the rule expression DTO
   */
  @Override
  public RuleExpressionDTO convertToDTO(String ruleExpressionString) {
    RuleExpressionDTO ruleExpressionDTO = new RuleExpressionDTO();
    constructor(ruleExpressionString, 0, ruleExpressionDTO);
    return ruleExpressionDTO;
  }

  /**
   * Converts the RuleExpressionDTO data structure into a Java executable code.
   *
   * @param ruleExpressionDTO the rule expression DTO
   * @return the string
   */
  @Override
  public String convertToString(RuleExpressionDTO ruleExpressionDTO) {
    if (null != ruleExpressionDTO.getOperator() && null != ruleExpressionDTO.getParams()
        && !ruleExpressionDTO.getParams().isEmpty()) {
      boolean firstParam = true;
      StringBuilder sb = new StringBuilder();

      sb.append("RuleOperators.");
      sb.append(ruleExpressionDTO.getOperator().getFunctionName());
      sb.append("(");

      for (Object param : ruleExpressionDTO.getParams()) {
        if (firstParam) {
          firstParam = false;
        } else {
          sb.append(", ");
        }
        sb.append(toStringBranch(param));
      }

      sb.append(")");

      return sb.toString();
    }

    throw new IllegalStateException("toString - Malformed RuleExpressionVO");
  }

  /**
   * Checks if the syntax of the rule expression String is Java compatible.
   *
   * @param ruleExpressionString the rule expression string
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @return true, if is data type compatible
   */
  @Override
  public boolean isDataTypeCompatible(String ruleExpressionString, EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap) {
    boolean compatible = true;
    if (ruleExpressionString == null || ruleExpressionString.isEmpty()) {
      compatible = false;
    } else {
      try {
        compatible =
            isDataTypeCompatible(convertToDTO(ruleExpressionString), entityType, dataTypeMap);
      } catch (IllegalStateException e) {
        compatible = false;
        LOG_ERROR.error("Error with the rule {}", ruleExpressionString);
      }
    }
    return compatible;
  }

  /**
   * Checks if is data type compatible.
   *
   * @param ruleExpressionDTO the rule expression DTO
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @return true, if is data type compatible
   */
  @Override
  public boolean isDataTypeCompatible(RuleExpressionDTO ruleExpressionDTO,
      EntityTypeEnum entityType, Map<String, DataType> dataTypeMap) {

    RuleOperatorEnum operator = ruleExpressionDTO.getOperator();
    List<Object> params = ruleExpressionDTO.getParams();
    int index = 0;
    boolean rtn = false;
    String[] paramTypes = operator.getParamTypes();

    if (entityType.equals(operator.getEntityType()) && paramTypes.length == params.size()) {
      rtn = true;
      while (rtn && index < paramTypes.length) {

        String superInputType = paramTypes[index];
        Object param = params.get(index);

        if (param instanceof RuleExpressionDTO) {
          rtn = isDataTypeCompatibleRule(entityType, dataTypeMap, superInputType,
              (RuleExpressionDTO) param);
        } else if (param instanceof Number) {
          rtn = isDataTypeCompatibleNumber(superInputType);
        } else if (param instanceof String) {
          rtn = isDataTypeCompatibleString(dataTypeMap, superInputType, (String) param);
        } else {
          rtn = false;
        }

        index++;
      }
    }

    return rtn;
  }

  /**
   * Instantiates a new rule expression service.
   */
  private RuleExpressionServiceImpl() {}

  /**
   * Constructor.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int constructor(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {
    index = readOperator(ruleExpressionString, index, ruleExpressionDTO);
    index = readParams(ruleExpressionString, index, ruleExpressionDTO);
    return index;
  }

  /**
   * Read operator.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readOperator(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {
    int beginIndex = index + "RuleOperators.".length();
    int endIndex = ruleExpressionString.indexOf('(', beginIndex);
    String ruleOperatorString = ruleExpressionString.substring(beginIndex, endIndex);
    ruleExpressionDTO.setOperator(RuleOperatorEnum.valueOfFunctionName(ruleOperatorString));
    return endIndex;
  }

  /**
   * Read params.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readParams(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {

    ruleExpressionDTO.setParams(new ArrayList<>());
    int length = ruleExpressionString.length();

    loop: while (index < length) {
      switch (ruleExpressionString.charAt(index)) {
        case '-':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          index = readNumber(ruleExpressionString, index, ruleExpressionDTO);
          break;
        case 'R':
          index = readFunction(ruleExpressionString, index, ruleExpressionDTO);
          break;
        case 'v':
          index = readValue(index, ruleExpressionDTO);
          break;
        case '"':
          index = readString(ruleExpressionString, index, ruleExpressionDTO);
          break;
        case '(':
        case ',':
        case ' ':
          index++;
          break;
        case ')':
          return index + 1;
        default:
          break loop;
      }
    }
    throw new IllegalStateException("readParams - Invalid expression: " + ruleExpressionString);
  }

  /**
   * Read number.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readNumber(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {

    char actual;
    int lenght = ruleExpressionString.length();
    boolean isDouble = false;
    StringBuilder number = new StringBuilder();

    while (index < lenght) {
      actual = ruleExpressionString.charAt(index);
      switch (actual) {
        case ',':
        case ')':
          if (isDouble) {
            ruleExpressionDTO.getParams().add(Double.parseDouble(number.toString()));
          } else {
            ruleExpressionDTO.getParams().add(Long.parseLong(number.toString()));
          }
          return index;
        case '.':
          index++;
          isDouble = true;
          number.append(actual);
          break;
        default:
          index++;
          number.append(actual);
      }
    }

    throw new IllegalStateException("readNumber - Invalid expression: " + ruleExpressionString);
  }

  /**
   * Read function.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readFunction(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {
    RuleExpressionDTO otherRule = new RuleExpressionDTO();
    index = constructor(ruleExpressionString, index, otherRule);
    ruleExpressionDTO.getParams().add(otherRule);
    return index;
  }

  /**
   * Read value.
   *
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readValue(int index, RuleExpressionDTO ruleExpressionDTO) {
    ruleExpressionDTO.getParams().add(VALUE);
    return index + "value".length();
  }

  /**
   * Read string.
   *
   * @param ruleExpressionString the rule expression string
   * @param index the index
   * @param ruleExpressionDTO the rule expression DTO
   * @return the int
   */
  private int readString(String ruleExpressionString, int index,
      RuleExpressionDTO ruleExpressionDTO) {

    int length = ruleExpressionString.length();
    boolean includeNext = false;
    StringBuilder string = new StringBuilder();
    char actual;

    while (index < length) {
      actual = ruleExpressionString.charAt(++index);
      switch (actual) {
        case '\\':
          if (includeNext) {
            string.append('\\');
          }
          includeNext = !includeNext;
          break;
        case '"':
          if (includeNext) {
            string.append('"');
            includeNext = false;
            break;
          }
          ruleExpressionDTO.getParams().add(string.toString());
          return index + 1;
        default:
          string.append(actual);
      }
    }

    throw new IllegalStateException("readString - Invalid expression: " + ruleExpressionString);
  }

  /**
   * To string branch.
   *
   * @param branch the branch
   * @return the string
   */
  @SuppressWarnings("unchecked")
  private String toStringBranch(Object branch) {

    if (branch instanceof String) {
      String string = (String) branch;
      if (string.equals(VALUE)) {
        return "value";
      }
      return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    if (branch instanceof Map) {
      Map<?, ?> branchMap = (Map<?, ?>) branch;
      RuleExpressionDTO branchRule = new RuleExpressionDTO();
      branchRule.setOperator(RuleOperatorEnum.valueOf((String) branchMap.get("operator")));
      branchRule.setParams((ArrayList<Object>) branchMap.get("params"));
      return convertToString(branchRule);
    }

    if (branch instanceof RuleExpressionDTO) {
      return convertToString((RuleExpressionDTO) branch);
    }

    return branch.toString();
  }

  /**
   * Checks if is data type compatible rule.
   *
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @param superInputType the super input type
   * @param ruleExpressionDTO the rule expression DTO
   * @return true, if is data type compatible rule
   */
  private boolean isDataTypeCompatibleRule(EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap, String superInputType,
      RuleExpressionDTO ruleExpressionDTO) {
    boolean typesMatch = ruleExpressionDTO.getOperator().getReturnType().equals(superInputType);
    boolean ruleDataTypeCompatible =
        isDataTypeCompatible(ruleExpressionDTO, entityType, dataTypeMap);
    return typesMatch && ruleDataTypeCompatible;
  }

  /**
   * Checks if is data type compatible number.
   *
   * @param superInputType the super input type
   * @return true, if is data type compatible number
   */
  private boolean isDataTypeCompatibleNumber(String superInputType) {
    return superInputType.equals(JavaType.OBJECT) || superInputType.equals(JavaType.NUMBER);
  }

  /**
   * Checks if is data type compatible string.
   *
   * @param dataTypeMap the data type map
   * @param superInputType the super input type
   * @param string the string
   * @return true, if is data type compatible string
   */
  private boolean isDataTypeCompatibleString(Map<String, DataType> dataTypeMap,
      String superInputType, String string) {

    if (superInputType.equals(JavaType.OBJECT)) {
      return true;
    }

    if (string.equals(VALUE) || ObjectId.isValid(string)) {
      // this check between Date and Timestamp is forced to maintain compatibility using the same
      // rule
      // like FIELD_DAY_GT... in Date and Timestamp
      if (JavaType.TIMESTAMP.equals(dataTypeMap.get(string).getJavaType())
          && JavaType.DATE.equals(superInputType)) {
        return true;
      }
      return superInputType.equals(dataTypeMap.get(string).getJavaType());
    }

    if (superInputType.equals(JavaType.DATE)) {
      // check date and timestamp
      return string.matches(REGEX_DATE) || string.matches(REGEX_DATETIME);
    }

    return superInputType.equals(JavaType.STRING);
  }
}
