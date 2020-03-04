package org.eea.interfaces.vo.dataset.schemas.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class RuleExpressionVO.
 */
@Getter
@Setter
public class RuleExpressionVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 23125812873889682L;

  /** The left arg. */
  private Object leftArg;

  /** The operator. */
  private RuleOperatorEnum operator;

  /** The right arg. */
  private Object rightArg;

  /**
   * Instantiates a new rule expression VO.
   */
  public RuleExpressionVO() {}

  /**
   * Instantiates a new rule expression VO.
   *
   * @param expression the expression
   */
  public RuleExpressionVO(String expression) {
    Map<Integer, Integer> map = new HashMap<>();
    List<String> tokens = new ArrayList<>();
    tokenize(expression, tokens, map, 0, 0);
    compose(0, tokens, map);
  }

  /**
   * Instantiates a new rule expression VO.
   *
   * @param map the map
   */
  @SuppressWarnings("unchecked")
  private RuleExpressionVO(Map<String, Object> map) {

    Object l = map.get("leftArg");
    Object o = map.get("operator");
    Object r = map.get("rightArg");

    // set leftArg
    if (l instanceof Map) {
      leftArg = new RuleExpressionVO((Map<String, Object>) l);
    } else if (l instanceof String) {
      leftArg = ((String) l).replace("\\", "");
    } else {
      leftArg = l;
    }

    // set operator
    setOperator(RuleOperatorEnum.valueOf((String) o));

    // set rightArg
    if (r instanceof Map) {
      rightArg = new RuleExpressionVO((Map<String, Object>) r);
    } else if (r instanceof String) {
      rightArg = ((String) r).replace("\\", "");
    } else {
      rightArg = r;
    }
  }

  /**
   * Instantiates a new rule expression VO.
   *
   * @param index the index
   * @param tokens the tokens
   * @param map the map
   */
  private RuleExpressionVO(int index, List<String> tokens, Map<Integer, Integer> map) {
    compose(index, tokens, map);
  }

  /**
   * Sets the left arg.
   *
   * @param leftArg the new left arg
   */
  @SuppressWarnings("unchecked")
  public void setLeftArg(Object leftArg) {
    if (leftArg instanceof Map) {
      this.leftArg = new RuleExpressionVO((Map<String, Object>) leftArg);
    } else {
      this.leftArg = leftArg;
    }
  }

  /**
   * Sets the right arg.
   *
   * @param rightArg the new right arg
   */
  @SuppressWarnings("unchecked")
  public void setRightArg(Object rightArg) {
    if (rightArg instanceof Map) {
      this.rightArg = new RuleExpressionVO((Map<String, Object>) rightArg);
    } else {
      this.rightArg = rightArg;
    }
  }

  /**
   * Tokenize.
   *
   * @param expression the expression
   * @param tokens the tokens
   * @param map the map
   * @param index the index
   * @param begin the begin
   * @return the int
   */
  private int tokenize(String expression, List<String> tokens, Map<Integer, Integer> map, int index,
      int begin) {

    int length = expression.length();
    char actual;

    for (int i = index; i < length; i++) {
      actual = expression.charAt(i);
      switch (actual) {
        case '(':
          tokens.add("(");
          i = tokenize(expression, tokens, map, i + 1, tokens.size() - 1);
          break;
        case ')':
          tokens.add(")");
          map.put(begin, tokens.size() - 1);
          return i;
        case '"':
          i = tokenizeString(expression, i + 1, tokens);
          break;
        case ' ':
          break;
        default:
          i = tokenizeElement(expression, i, tokens);
      }
    }

    return length;
  }

  /**
   * Tokenize element.
   *
   * @param expression the expression
   * @param index the index
   * @param tokens the tokens
   * @return the int
   */
  private int tokenizeElement(String expression, int index, List<String> tokens) {

    int inputType = inputType(expression.charAt(index), 0);
    int length = expression.length();
    StringBuilder token = new StringBuilder();
    char actual;

    for (int i = index; i < length; i++) {
      actual = expression.charAt(i);
      if (inputType != inputType(actual, inputType)) {
        tokens.add(token.toString());
        return i - 1;
      }
      token.append(actual);
    }

    tokens.add(token.toString());
    return length;
  }

  /**
   * Tokenize string.
   *
   * @param expression the expression
   * @param index the index
   * @param tokens the tokens
   * @return the int
   */
  private int tokenizeString(String expression, int index, List<String> tokens) {

    int length = expression.length();
    boolean ignoreNext = false;
    StringBuilder token = new StringBuilder();
    char actual;

    for (int i = index; i < length; i++) {
      actual = expression.charAt(i);
      switch (actual) {
        case '\\':
          if (ignoreNext) {
            token.append(actual);
            ignoreNext = false;
            break;
          }
          ignoreNext = true;
          break;
        case '"':
          if (ignoreNext) {
            token.append(actual);
            ignoreNext = false;
            break;
          }
          tokens.add("\"" + token + "\"");
          return i;
        default:
          token.append(actual);
      }
    }

    return length;
  }

  /**
   * Compose.
   *
   * @param index the index
   * @param tokens the tokens
   * @param map the map
   */
  private void compose(int index, List<String> tokens, Map<Integer, Integer> map) {

    String actual = null;

    // leftArg
    actual = tokens.get(index);
    if (actual.equals("(")) {
      leftArg = new RuleExpressionVO(index + 1, tokens, map);
      index = map.get(index);
    } else if (actual.equals("value")) {
      leftArg = "VALUE";
    } else if (actual.equals("value.length()")) {
      leftArg = "LENGTH";
    } else if (actual.startsWith("\"")) {
      leftArg = actual.substring(1, actual.length() - 1);
    } else if (actual.contains(".")) {
      leftArg = Double.parseDouble(actual);
    } else {
      leftArg = Long.parseLong(actual);
      if ((Long) leftArg <= Integer.MAX_VALUE) {
        leftArg = ((Long) leftArg).intValue();
      }
    }
    index++;

    // operator
    operator = RuleOperatorEnum.valueOfLabel(tokens.get(index++));

    // rightArg
    actual = tokens.get(index);
    if (actual.equals("(")) {
      rightArg = new RuleExpressionVO(index + 1, tokens, map);
      index = map.get(index);
    } else if (actual.equals("value")) {
      rightArg = "VALUE";
    } else if (actual.equals("value.length()")) {
      rightArg = "LENGTH";
    } else if (actual.startsWith("\"")) {
      rightArg = actual.substring(1, actual.length() - 1);
    } else if (actual.contains(".")) {
      rightArg = Double.parseDouble(actual);
    } else {
      rightArg = Long.parseLong(actual);
      if ((Long) rightArg <= Integer.MAX_VALUE) {
        rightArg = ((Long) rightArg).intValue();
      }
    }
  }

  /**
   * Input type.
   *
   * @param actual the actual
   * @return the int
   */
  private int inputType(char actual, int lastInputType) {

    // White space
    if (actual == ' ') {
      return 0;
    }

    // Dot
    if (actual == '.') {
      return lastInputType;
    }

    // Number
    if (actual >= '0' && actual <= '9') {
      return 2;
    }

    // Operator
    if (actual == '=' || actual == '!' || actual == '<' || actual == '>' || actual == '|'
        || actual == '&') {
      return 3;
    }

    // Rest of characters
    return 4;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {

    if (leftArg == null || operator == null || rightArg == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    // Write leftArg
    if (leftArg instanceof RuleExpressionVO) {
      sb.append("(");
      sb.append(((RuleExpressionVO) leftArg).toString());
      sb.append(")");
    } else if (leftArg instanceof String) {
      switch ((String) leftArg) {
        case "VALUE":
          sb.append("value");
          break;
        case "LENGTH":
          sb.append("value.length()");
          break;
        default:
          sb.append("\"");
          sb.append(((String) leftArg).replace("\\", "\\\\").replace("\"", "\\\""));
          sb.append("\"");
      }
    } else {
      sb.append(leftArg);
    }

    // Write operator
    sb.append(" ");
    sb.append(operator.getValue());
    sb.append(" ");

    // Write rightArg
    if (rightArg instanceof RuleExpressionVO) {
      sb.append("(");
      sb.append(((RuleExpressionVO) rightArg).toString());
      sb.append(")");
    } else if (rightArg instanceof String) {
      switch ((String) leftArg) {
        case "VALUE":
          sb.append("value");
          break;
        case "LENGTH":
          sb.append("value.length()");
          break;
        default:
          sb.append("\"");
          sb.append(((String) leftArg).replace("\\", "\\\\").replace("\"", "\\\""));
          sb.append("\"");
      }
    } else {
      sb.append(rightArg);
    }

    return sb.toString();
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(leftArg, operator.ordinal(), rightArg);
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
      boolean l = leftArg.equals(other.leftArg);
      boolean o = operator.equals(other.operator);
      boolean r = rightArg.equals(other.rightArg);
      return l && o && r;
    }

    return false;
  }
}
