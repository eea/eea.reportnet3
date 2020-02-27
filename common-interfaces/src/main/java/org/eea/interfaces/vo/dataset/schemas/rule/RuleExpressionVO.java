package org.eea.interfaces.vo.dataset.schemas.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import lombok.Getter;

@Getter
public class RuleExpressionVO implements Serializable {

  private static final long serialVersionUID = 23125812873889682L;

  private Object leftArg;

  private String operator;

  private Object rightArg;

  public RuleExpressionVO() {}

  public RuleExpressionVO(String expression) {
    Map<Integer, Integer> map = new HashMap<>();
    List<String> tokens = new ArrayList<>();
    tokenize(expression, tokens, map);
    ruleExpression(0, tokens.size(), tokens, map);
  }

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
    operator = (String) map.get("operator");

    // rightArg
    if (r instanceof Map) {
      rightArg = new RuleExpressionVO((Map<String, Object>) r);
    } else {
      rightArg = r;
    }
  }

  private RuleExpressionVO(int begin, int end, List<String> tokens, Map<Integer, Integer> map) {
    ruleExpression(begin, end, tokens, map);
  }

  private void ruleExpression(int begin, int end, List<String> tokens, Map<Integer, Integer> map) {

    int index = begin;
    String actual = null;

    // leftArg
    actual = tokens.get(index);
    if (actual.equals("(")) {
      leftArg = new RuleExpressionVO(index + 1, index = map.get(index), tokens, map);
    } else if (actual.startsWith("\"")) {
      leftArg = actual.substring(1, actual.length() - 1);
    } else if (actual.contains(".")) {
      leftArg = Double.parseDouble(actual);
    } else {
      leftArg = Long.parseLong(actual);
    }
    index++;

    // operator
    operator = tokens.get(index++);

    // rightArg
    actual = tokens.get(index);
    if (actual.equals("(")) {
      rightArg = new RuleExpressionVO(index + 1, index = map.get(index), tokens, map);
    } else if (actual.startsWith("\"")) {
      rightArg = actual.substring(1, actual.length() - 1);
    } else if (actual.contains(".")) {
      rightArg = Double.parseDouble(actual);
    } else {
      rightArg = Long.parseLong(actual);
    }
  }

  private void tokenize(String expression, List<String> tokens, Map<Integer, Integer> map) {

    Stack<Integer> stack = new Stack<>();
    int length = expression.length();
    String token = "";
    char actual;
    boolean isString = false;

    for (int i = 0; i < length; i++) {
      actual = expression.charAt(i);
      switch (actual) {
        case '(':
          if (!token.isEmpty()) {
            tokens.add(token);
            token = "";
          }
          token += actual;
          tokens.add(token);
          token = "";
          stack.push(tokens.size() - 1);
          break;
        case ')':
          token += actual;
          tokens.add(token);
          token = "";
          map.put(stack.pop(), tokens.size() - 1);
          break;
        case '"':
          token += actual;
          if (!(isString = !isString)) {
            tokens.add(token);
            token = "";
          }
          break;
        case ' ':
          if (!token.isEmpty()) {
            tokens.add(token);
            token = "";
          }
          break;
        default:
          token += actual;
          if (i + 1 == length || (isNum(actual) != isNum(expression.charAt(i + 1)))) {
            tokens.add(token);
            token = "";
          }
          break;
      }
    }
  }

  private boolean isNum(char c) {
    return c >= '0' && c <= '9' || c == '.';
  }

  @SuppressWarnings("unchecked")
  public void setLeftArg(Object leftArg) {
    if (leftArg instanceof Map) {
      this.leftArg = new RuleExpressionVO((Map<String, Object>) leftArg);
    } else {
      this.leftArg = leftArg;
    }
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @SuppressWarnings("unchecked")
  public void setRightArg(Object rightArg) {
    if (rightArg instanceof Map) {
      this.rightArg = new RuleExpressionVO((Map<String, Object>) rightArg);
    } else {
      this.rightArg = rightArg;
    }
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    if (leftArg instanceof RuleExpressionVO) {
      sb.append("(");
      sb.append(((RuleExpressionVO) leftArg).toString());
      sb.append(")");
    } else if (leftArg instanceof String) {
      sb.append("\"");
      sb.append(leftArg);
      sb.append("\"");
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
      sb.append("\"");
      sb.append(rightArg);
      sb.append("\"");
    } else {
      sb.append(rightArg);
    }

    return sb.toString();
  }

  @Override
  public int hashCode() {

    Object l = leftArg;
    Object r = rightArg;

    if (l instanceof RuleExpressionVO) {
      l = ((RuleExpressionVO) l).hashCode();
    }

    if (r instanceof RuleExpressionVO) {
      r = ((RuleExpressionVO) r).hashCode();
    }

    return Objects.hash(l, operator, r);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (obj instanceof RuleExpressionVO) {
      RuleExpressionVO other = (RuleExpressionVO) obj;
      if (leftArg.equals(other.leftArg) && operator.equals(other.operator)
          && rightArg.equals(other.rightArg)) {
        return true;
      }
    }

    return false;
  }
}
