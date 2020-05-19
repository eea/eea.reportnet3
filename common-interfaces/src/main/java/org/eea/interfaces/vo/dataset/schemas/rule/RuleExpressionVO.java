package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RuleExpressionVO {

  private RuleOperatorEnum operator;

  private List<Object> params;

  public RuleExpressionVO() {}

  public RuleExpressionVO(String expression) {
    constructor(expression, 0, this);
  }

  public RuleOperatorEnum getOperator() {
    return operator;
  }

  public List<Object> getParams() {
    return params;
  }

  public void setOperator(RuleOperatorEnum operator) {
    this.operator = operator;
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(operator != null ? operator.ordinal() : null, params);
  }

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

  public boolean isDataTypeCompatible(EntityTypeEnum entityType, DataType dataType) {

    int index = 0;
    boolean rtn = false;
    String[] paramTypes = operator.getParamTypes();

    if (entityType.equals(operator.getEntityType()) && paramTypes.length == params.size()) {
      rtn = true;
      while (rtn && index < paramTypes.length) {

        String superInputType = paramTypes[index];
        Object param = params.get(index);

        if (param instanceof RuleExpressionVO) {
          rtn = isDataTypeCompatibleRule(entityType, dataType, superInputType,
              (RuleExpressionVO) param);
        } else if (param instanceof Number) {
          rtn = isDataTypeCompatibleNumber(superInputType);
        } else if (param instanceof String) {
          rtn = isDataTypeCompatibleString(dataType, superInputType, (String) param);
        } else {
          rtn = false;
        }

        index++;
      }
    }

    return rtn;
  }

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

  private int constructor(String expression, int index, RuleExpressionVO rule) {
    index = readOperator(expression, index, rule);
    index = readParams(expression, index, rule);
    return index;
  }

  private int readOperator(String expression, int index, RuleExpressionVO rule) {
    int beginIndex = index + "this.".length();
    int endIndex = expression.indexOf('(', beginIndex);
    rule.operator =
        RuleOperatorEnum.valueOfFunctionName(expression.substring(beginIndex, endIndex));
    return endIndex;
  }

  private int readParams(String expression, int index, RuleExpressionVO rule) {

    rule.params = new ArrayList<>();
    int length = expression.length();

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

  private int readFunction(String expression, int index, RuleExpressionVO rule) {
    RuleExpressionVO otherRule = new RuleExpressionVO();
    index = constructor(expression, index, otherRule);
    rule.params.add(otherRule);
    return index;
  }

  private int readValue(int index, RuleExpressionVO rule) {
    rule.params.add("VALUE");
    return index + "value".length();
  }

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

  private boolean isDataTypeCompatibleRule(EntityTypeEnum entityType, DataType dataType,
      String superInputType, RuleExpressionVO rule) {
    boolean typesMatch = rule.getOperator().getReturnType().equals(superInputType);
    boolean ruleDataTypeCompatible = rule.isDataTypeCompatible(entityType, dataType);
    return typesMatch && ruleDataTypeCompatible;
  }

  private boolean isDataTypeCompatibleNumber(String superInputType) {
    return superInputType.equals("Number");
  }

  private boolean isDataTypeCompatibleString(DataType dataType, String superInputType,
      String string) {

    if (string.equals("VALUE")) {
      return superInputType.equals(dataType.getJavaType());
    }

    if (superInputType.equals("Date")) {
      return string.matches("[0-9]{4}-(?:0[0-9]|1[0-2])-(?:[0-2][0-9]|3[01])");
    }

    return superInputType.equals("String");
  }

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
}
