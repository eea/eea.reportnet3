package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.JavaType;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class RuleExpressionVO.
 */
public class RuleExpressionVO {

  /** The operator. */
  private RuleOperatorEnum operator;

  /** The params. */
  private List<Object> params;

  /**
   * Instantiates a new RuleExpressionVO. Used by SpringMVC to cast JSON into a RuleExpressionVO
   * object.
   */
  public RuleExpressionVO() {}

  /**
   * Instantiates a new RuleExpressionVO throw its string serialization.
   *
   * @param expression the expression
   */
  public RuleExpressionVO(String expression) {
    constructor(expression, 0, this);
  }

  /**
   * Gets the operator. Used by SpringMVC to cast RuleExpressionVO object into JSON.
   *
   * @return the operator
   */
  public RuleOperatorEnum getOperator() {
    return operator;
  }

  /**
   * Gets the params. Used by SpringMVC to cast RuleExpressionVO object into JSON.
   *
   * @return the params
   */
  public List<Object> getParams() {
    return params;
  }

  /**
   * Sets the operator. Used by SpringMVC to cast JSON into a RuleExpressionVO object.
   *
   * @param operator the new operator
   */
  public void setOperator(RuleOperatorEnum operator) {
    this.operator = operator;
  }

  /**
   * Sets the params. Used by SpringMVC to cast JSON into a RuleExpressionVO object.
   *
   * @param params the new params
   */
  @SuppressWarnings("unchecked")
  public void setParams(Object[] params) {

    this.params = new ArrayList<>();

    for (Object param : params) {
      if (param instanceof Map) {
        this.params.add(new RuleExpressionVO((Map<String, ?>) param));
      } else if (param instanceof String || param instanceof Integer || param instanceof Long
          || param instanceof Float || param instanceof Double) {
        this.params.add(param);
      } else {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid param: " + param);
      }
    }
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(operator != null ? operator.ordinal() : null, params);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof RuleExpressionVO) {
      RuleExpressionVO other = (RuleExpressionVO) obj;
      boolean operatorEquals;
      boolean paramsEquals;

      // Compare operator
      if (operator == null) {
        operatorEquals = other.operator == null;
      } else {
        operatorEquals = operator.equals(other.operator);
      }

      // Compare params
      if (params == null) {
        paramsEquals = other.params == null;
      } else {
        paramsEquals = params.equals(other.params);
      }

      return operatorEquals && paramsEquals;
    }

    return false;
  }

  /**
   * Serialize the object to generate a java compilable string instruction.
   *
   * @return the string
   */
  @Override
  public String toString() {

    if (null != operator && null != params && !params.isEmpty()) {
      boolean firstParam = true;
      StringBuilder sb = new StringBuilder();

      sb.append("this.");
      sb.append(operator.getFunctionName());
      sb.append("(");

      for (Object param : params) {
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
   * Checks if is data type compatible.
   *
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @return true, if is data type compatible
   */
  public boolean isDataTypeCompatible(EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap) {

    int index = 0;
    boolean rtn = false;
    String[] paramTypes = operator.getParamTypes();

    if (entityType.equals(operator.getEntityType()) && paramTypes.length == params.size()) {
      rtn = true;
      while (rtn && index < paramTypes.length) {

        String superInputType = paramTypes[index];
        Object param = params.get(index);

        if (param instanceof RuleExpressionVO) {
          rtn = isDataTypeCompatibleRule(entityType, dataTypeMap, superInputType,
              (RuleExpressionVO) param);
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
   * Instantiates a new rule expression VO.
   *
   * @param map the map
   */
  private RuleExpressionVO(Map<String, ?> map) {

    Object o = map.get("operator");
    List<?> p = (ArrayList<?>) map.get("params");

    if (null != o && null != p) {
      setOperator(RuleOperatorEnum.valueOf((String) o));
      setParams(p.toArray());
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid param: " + map);
    }
  }

  /**
   * Constructor.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int constructor(String expression, int index, RuleExpressionVO rule) {
    index = readOperator(expression, index, rule);
    index = readParams(expression, index, rule);
    return index;
  }

  /**
   * Read operator.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readOperator(String expression, int index, RuleExpressionVO rule) {
    int beginIndex = index + "this.".length();
    int endIndex = expression.indexOf('(', beginIndex);
    rule.operator =
        RuleOperatorEnum.valueOfFunctionName(expression.substring(beginIndex, endIndex));
    return endIndex;
  }

  /**
   * Read params.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readParams(String expression, int index, RuleExpressionVO rule) {

    rule.params = new ArrayList<>();
    int length = expression.length();

    if (expression.contains("isIntegrityConstraint")) {
      return length;
    }
    loop: while (index < length) {
      switch (expression.charAt(index)) {
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
          index = readNumber(expression, index, rule);
          break;
        case 't':
          index = readFunction(expression, index, rule);
          break;
        case 'v':
          index = readValue(index, rule);
          break;
        case '"':
          index = readString(expression, index, rule);
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
    throw new IllegalStateException("readParams - Invalid expression: " + expression);

  }

  /**
   * Read number.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readNumber(String expression, int index, RuleExpressionVO rule) {

    char actual;
    int lenght = expression.length();
    boolean isDouble = false;
    StringBuilder number = new StringBuilder();

    while (index < lenght) {
      actual = expression.charAt(index);
      switch (actual) {
        case ',':
        case ')':
          if (isDouble) {
            rule.params.add(Double.parseDouble(number.toString()));
          }
          rule.params.add(Long.parseLong(number.toString()));
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

    throw new IllegalStateException("readNumber - Invalid expression: " + expression);
  }

  /**
   * Read function.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readFunction(String expression, int index, RuleExpressionVO rule) {
    RuleExpressionVO otherRule = new RuleExpressionVO();
    index = constructor(expression, index, otherRule);
    rule.params.add(otherRule);
    return index;
  }

  /**
   * Read value.
   *
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readValue(int index, RuleExpressionVO rule) {
    rule.params.add("VALUE");
    return index + "value".length();
  }

  /**
   * Read string.
   *
   * @param expression the expression
   * @param index the index
   * @param rule the rule
   * @return the int
   */
  private int readString(String expression, int index, RuleExpressionVO rule) {

    int length = expression.length();
    boolean includeNext = false;
    StringBuilder string = new StringBuilder();
    char actual;

    while (index < length) {
      actual = expression.charAt(++index);
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
          rule.params.add(string.toString());
          return index + 1;
        default:
          string.append(actual);
      }
    }

    throw new IllegalStateException("readString - Invalid expression: " + expression);
  }

  /**
   * Checks if is data type compatible rule.
   *
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @param superInputType the super input type
   * @param rule the rule
   * @return true, if is data type compatible rule
   */
  private boolean isDataTypeCompatibleRule(EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap, String superInputType, RuleExpressionVO rule) {
    boolean typesMatch = rule.getOperator().getReturnType().equals(superInputType);
    boolean ruleDataTypeCompatible = rule.isDataTypeCompatible(entityType, dataTypeMap);
    return typesMatch && ruleDataTypeCompatible;
  }

  /**
   * Checks if is data type compatible number.
   *
   * @param superInputType the super input type
   * @return true, if is data type compatible number
   */
  private boolean isDataTypeCompatibleNumber(String superInputType) {
    return superInputType.equals(JavaType.NUMBER);
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

    if (string.equals("VALUE") || isValid(string)) {
      return superInputType.equals(dataTypeMap.get(string).getJavaType());
    }

    if (superInputType.equals(JavaType.DATE)) {
      return string.matches("[0-9]{4}-(?:0[0-9]|1[0-2])-(?:[0-2][0-9]|3[01])");
    }

    return superInputType.equals(JavaType.STRING);
  }

  /**
   * To string branch.
   *
   * @param branch the branch
   * @return the string
   */
  private String toStringBranch(Object branch) {

    if (branch instanceof String) {
      String string = (String) branch;
      if (string.equals("VALUE")) {
        return "value";
      }
      return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    return branch.toString();
  }

  /**
   * Checks if is valid objectId. We create that method to evaluate if the value is valid objectId
   *
   * @param hexString the hex string
   * @return true, if is valid
   */
  private static boolean isValid(final String hexString) {
    if (hexString == null) {
      throw new IllegalArgumentException();
    }

    int len = hexString.length();
    if (len != 24) {
      return false;
    }

    for (int i = 0; i < len; i++) {
      char c = hexString.charAt(i);
      if (c >= '0' && c <= '9') {
        continue;
      }
      if (c >= 'a' && c <= 'f') {
        continue;
      }
      if (c >= 'A' && c <= 'F') {
        continue;
      }

      return false;
    }

    return true;
  }
}
