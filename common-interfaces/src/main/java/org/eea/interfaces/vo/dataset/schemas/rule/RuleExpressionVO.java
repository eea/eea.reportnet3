package org.eea.interfaces.vo.dataset.schemas.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class RuleExpressionVO.
 */
@Getter
@Setter
public class RuleExpressionVO implements Serializable {

  private static final long serialVersionUID = 23125812873889682L;

  private RuleOperatorEnum operator;

  private Object arg1;

  private Object arg2;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  public RuleExpressionVO() {}

  public RuleExpressionVO(String expression) {
    Map<Integer, Integer> map = new HashMap<>();
    List<String> tokens = new ArrayList<>();
    tokenize(expression, tokens, map, 0, 0);
    compose(0, tokens, map);
  }

  private RuleExpressionVO(Map<String, Object> map) {

    Object mapEntry;

    // set operator
    if ((mapEntry = map.get("operator")) != null) {
      setOperator(RuleOperatorEnum.valueOf((String) mapEntry));

      // set arg1
      if ((mapEntry = map.get("arg1")) != null) {
        setArg1(mapEntry);

        // set arg2
        if ((mapEntry = map.get("arg2")) != null) {
          setArg2(mapEntry);
        }
      } else {
        LOG_ERROR.error("Error creating RuleExpressionVO: arg1 not found");
      }
    } else {
      LOG_ERROR.error("Error creating RuleExpressionVO: operator not found");
    }
  }

  private RuleExpressionVO(int index, List<String> tokens, Map<Integer, Integer> map) {
    compose(index, tokens, map);
  }

  @SuppressWarnings("unchecked")
  public void setArg1(Object arg1) {
    if (arg1 != null) {
      if (arg1 instanceof Map) {
        this.arg1 = new RuleExpressionVO((Map<String, Object>) arg1);
      } else {
        this.arg1 = arg1;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void setArg2(Object arg2) {
    if (arg2 != null) {
      if (arg2 instanceof Map) {
        this.arg2 = new RuleExpressionVO((Map<String, Object>) arg2);
      } else {
        this.arg2 = arg2;
      }
    }
  }

  private int tokenizeFunction(String expression, int index, List<String> tokens) {

    char actual;
    StringBuilder function = new StringBuilder();

    while ((actual = expression.charAt(index++)) != '(') {
      function.append(actual);
    }
    tokens.add(function.toString());

    switch (expression.charAt(index)) {
      case '"':
        index = tokenizeString(expression, index + 1, tokens) + 1;
        break;
      case ')':
        break;
      default:
        index = tokenizeElement(expression, index, tokens);
    }

    return index;
  }

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

    LOG_ERROR.error("Error tokenizing string: index={}, expression={}", index, expression);
    return length;
  }

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
          System.out.println(tokens);
          return i;
        case '"':
          i = tokenizeString(expression, i + 1, tokens);
          break;
        case ' ':
          break;
        case '.':
          i = tokenizeFunction(expression, i + 1, tokens);
          break;
        default:
          i = tokenizeElement(expression, i, tokens);
      }
    }

    return length;
  }

  private void compose(int index, List<String> tokens, Map<Integer, Integer> map) {

    String actual = null;
    List<Object> args = new ArrayList<>();

    while (index < tokens.size()) {
      actual = tokens.get(index);

      if (actual.equals(")")) {
        break;
      }

      if (actual.equals("(")) {
        args.add(new RuleExpressionVO(index + 1, tokens, map));
        index = map.get(index) + 1;
        continue;
      }

      if (RuleOperatorEnum.valueOfLabel(actual) != null) {
        operator = RuleOperatorEnum.valueOfLabel(actual);
        index++;
        continue;
      }

      if (actual.startsWith("\"")) {
        args.add(
            actual.substring(1, actual.length() - 1).replace("\\\\", "\\").replace("\\\"", "\""));
        index++;
        continue;
      }

      if (actual.equals("value")) {
        args.add("VALUE");
        index++;
        continue;
      }

      if (actual.contains(".")) {
        args.add(Double.parseDouble(actual));
        index++;
        continue;
      }

      Long n = Long.parseLong(actual);
      if (n <= Integer.MAX_VALUE) {
        args.add(n.intValue());
      }
      index++;
    }

    if (operator != null) {
      switch (operator) {
        case NOT:
        case LEN:
          arg1 = args.get(0);
          break;
        default:
          arg1 = args.get(0);
          arg2 = args.get(1);
      }
    } else {
      LOG_ERROR.error("Error composing RuleExpressionVO: operator={}, args={}", operator, args);
    }
  }

  private int inputType(char actual, int lastInputType) {
    switch (actual) {
      case ' ':
        return 0;
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
        return 1;
      case '=':
      case '!':
      case '<':
      case '>':
      case '|':
      case '&':
        return 2;
      case '.':
        if (lastInputType == 1) {
          return 1;
        }
        return 3;
      case ')':
        return 4;
      default:
        return 5;
    }
  }

  private String toStringBranch(Object branch) {

    if (branch instanceof RuleExpressionVO) {
      return "(" + branch + ")";
    }

    if (branch instanceof String) {
      String string = (String) branch;
      switch (string) {
        case "VALUE":
          return "value";
        default:
          return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
      }
    }

    return branch.toString();
  }

  @Override
  public String toString() {
    if (operator != null) {
      switch (operator) {
        case EQ:
        case DIST:
        case GT:
        case LT:
        case GTEQ:
        case LTEQ:
        case AND:
        case OR:
          return toStringBranch(arg1) + " " + operator.getLabel() + " " + toStringBranch(arg2);
        case NOT:
          return operator.getLabel() + "(" + arg1 + ")";
        case LEN:
          return "value." + operator.getLabel() + "()";
        case SEQ:
        case SEQIC:
          return "value." + operator.getLabel() + "(" + toStringBranch(arg2) + ")";
      }
    }

    LOG_ERROR.error("Error stringifying RuleExpressionVO: operator is null");
    return "OPERATOR IS NULL";
  }

  @Override
  public int hashCode() {
    return Objects.hash(operator != null ? operator.ordinal() : null, arg1, arg2);
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (obj instanceof RuleExpressionVO) {
      RuleExpressionVO other = (RuleExpressionVO) obj;
      boolean o, a1, a2;

      // Compare operator
      if (operator == null) {
        o = other.operator == null;
      } else {
        o = operator.equals(other.operator);
      }

      // Compare arg1
      if (arg1 == null) {
        a1 = other.arg1 == null;
      } else {
        a1 = arg1.equals(other.arg1);
      }

      // Compare arg2
      if (arg2 == null) {
        a2 = other.arg2 == null;
      } else {
        a2 = arg2.equals(other.arg2);
      }

      return o && a1 && a2;
    }

    return false;
  }
}
