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
    Object r = map.get("rightArg");

    // leftArg
    if (l instanceof Map) {
      leftArg = new RuleExpressionVO((Map<String, Object>) l);
    } else {
      leftArg = l;
    }

    // operator
    setOperator(RuleOperatorEnum.valueOf((String) map.get("operator")));

    // rightArg
    if (r instanceof Map) {
      rightArg = new RuleExpressionVO((Map<String, Object>) r);
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
    } else if (actual.equals("VALUE") || actual.equals("LENGTH")) {
      leftArg = actual;
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
    operator = RuleOperatorEnum.valueOf(tokens.get(index++));

    // rightArg
    actual = tokens.get(index);
    if (actual.equals("(")) {
      rightArg = new RuleExpressionVO(index + 1, tokens, map);
      index = map.get(index);
    } else if (actual.equals("VALUE") || actual.equals("LENGTH")) {
      rightArg = actual;
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
   * Input type.
   *
   * @param actual the actual
   * @return the int
   */
  private int inputType(char actual) {

    if (actual == ' ') {
      return 0;
    }

    if (actual >= '0' && actual <= '9' || actual == '.') {
      return 1;
    }

    if (actual == '=' || actual == '!' || actual == '<' || actual == '>' || actual == '|'
        || actual == '&') {
      return 2;
    }

    return 3;
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

    int inputType = inputType(expression.charAt(index));
    int length = expression.length();
    StringBuilder token = new StringBuilder();
    char actual;

    for (int i = index; i < length; i++) {
      actual = expression.charAt(i);
      if (inputType != inputType(actual)) {
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
          ignoreNext = true;
          break;
        case '"':
          if (ignoreNext) {
            token.append(actual);
            ignoreNext = false;
            break;
          } else {
            tokens.add("\"" + token + "\"");
            return i;
          }
        default:
          token.append(actual);
      }
    }

    return length;
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
          map.put(begin, tokens.size() - 1); // tokens based
          // map.put(index - 1, i); // expression based
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
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    if (leftArg instanceof RuleExpressionVO) {
      sb.append("(");
      sb.append(((RuleExpressionVO) leftArg).toString());
      sb.append(")");
    } else if (leftArg instanceof String) {
      if (leftArg.equals("VALUE") || leftArg.equals("LENGTH")) {
        sb.append(leftArg);
      } else {
        sb.append("\"");
        sb.append(leftArg);
        sb.append("\"");
      }
    } else {
      sb.append(leftArg);
    }

    sb.append(" ");
    sb.append(operator);
    sb.append(" ");

    if (rightArg instanceof RuleExpressionVO) {
      sb.append("(");
      sb.append(((RuleExpressionVO) rightArg).toString());
      sb.append(")");
    } else if (rightArg instanceof String) {
      if (rightArg.equals("VALUE") || rightArg.equals("LENGTH")) {
        sb.append(rightArg);
      } else {
        sb.append("\"");
        sb.append(rightArg);
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
      boolean o = operator.equals(other.operator);
      boolean l = leftArg.equals(other.leftArg);
      boolean r = rightArg.equals(other.rightArg);
      return o && l && r;
    }

    return false;
  }
}
