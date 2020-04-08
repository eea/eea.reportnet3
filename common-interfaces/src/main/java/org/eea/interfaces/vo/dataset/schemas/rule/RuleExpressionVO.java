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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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

  /** The operator. */
  private RuleOperatorEnum operator;

  /** The argument 1. */
  private Object arg1;

  /** The argument 2. */
  private Object arg2;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Instantiates a new RuleExpressionVO.
   */
  public RuleExpressionVO() {}

  /**
   * Instantiates a new RuleExpressionVO from a Java expression contained in the String.
   *
   * @param expression the Java expression
   */
  public RuleExpressionVO(String expression) {
    Map<Integer, Integer> map = new HashMap<>();
    List<String> tokens = new ArrayList<>();
    tokenize(expression, tokens, map, 0, 0);
    compose(0, tokens, map);
  }

  /**
   * Instantiates a new RuleExpressionVO. Used to construct a new object recursively from a
   * serialized RuleExpressionVO.
   *
   * @param map contains the RuleExpressionVO serialized.
   */
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

  /**
   * Instantiates a new RuleExpressionVO. Used to generate a new RuleExpressionVO from a Java
   * expression contained in a String.
   *
   * @param index the index
   * @param tokens the tokens
   * @param map the map
   */
  private RuleExpressionVO(int index, List<String> tokens, Map<Integer, Integer> map) {
    compose(index, tokens, map);
  }

  /**
   * Sets the arg 1.
   *
   * @param arg1 the new arg 1
   */
  @SuppressWarnings("unchecked")
  public void setArg1(Object arg1) {
    if (arg1 != null) {

      if (arg1 instanceof Map) {
        this.arg1 = new RuleExpressionVO((Map<String, Object>) arg1);
        return;
      }

      if (arg1 instanceof Number) {
        if (arg1 instanceof Integer || arg1 instanceof Long || arg1 instanceof Float
            || arg1 instanceof Double) {
          this.arg1 = arg1;
          return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number: " + arg1);
      }

      if (arg1 instanceof String) {
        this.arg1 = arg1;
        return;
      }

      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: " + arg2);
    }
  }

  /**
   * Sets the arg 2.
   *
   * @param arg2 the new arg 2
   */
  @SuppressWarnings("unchecked")
  public void setArg2(Object arg2) {
    if (arg2 != null) {

      if (arg2 instanceof Map) {
        this.arg2 = new RuleExpressionVO((Map<String, Object>) arg2);
        return;
      }

      if (arg2 instanceof Number) {
        if (arg2 instanceof Integer || arg2 instanceof Long || arg2 instanceof Float
            || arg2 instanceof Double) {
          this.arg2 = arg2;
          return;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number: " + arg2);
      }

      if (arg2 instanceof String) {
        this.arg2 = arg2;
        return;
      }

      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type: " + arg2);
    }
  }


  /**
   * Tokenize a function. A function starts with ".", the first word is the operator, and the
   * elements in parenthesis are the arguments. Ej. .equals("hello").<br>
   * <b>*NOTE*</b> Actually only reads the first argument, but it is intented to read all of them.
   *
   * @param expression the expression
   * @param index the index
   * @param tokens the tokens
   * @return the int
   */
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
        break;
    }

    return index;
  }

  /**
   * Auxiliary function to tokenize a raw value. Ej.: &&, >=, equals, equalsIgnoreCase...
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
   * Auxiliary function to tokenize an String. A word is considered String if begins with ". Ej.:
   * "hello", "world", "\"quoted\"", "\\scaped backslash"
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
          break;
      }
    }

    LOG_ERROR.error("Error tokenizing string: index={}, expression={}", index, expression);
    return length;
  }

  /**
   * Split the string into tokens. Each token is a word. Tokenization example:
   * (value.equals("hello")) || (value.equals("world")) -> [(, VALUE, SEQ, "hello", ), ||, (, VALUE,
   * SEQ, "world", )]
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
        case '.':
          i = tokenizeFunction(expression, i + 1, tokens);
          break;
        default:
          i = tokenizeElement(expression, i, tokens);
          break;
      }
    }

    return length;
  }

  /**
   * Organize all tokens to convert them into a RuleExpressionVO.
   *
   * @param index the index
   * @param tokens the tokens
   * @param map the map
   */
  private void compose(int index, List<String> tokens, Map<Integer, Integer> map) {

    String actual = null;
    List<Object> args = new ArrayList<>();

    while (index < tokens.size()) {
      actual = tokens.get(index);

      // End of expression: Finish
      if (actual.equals(")")) {
        break;
      }

      // Begin of expression: Compose recursively and move the reading pointer to the supposed end
      if (actual.equals("(")) {
        args.add(new RuleExpressionVO(index + 1, tokens, map));
        index = map.get(index) + 1;
        continue;
      }

      // RuleOperatorEnum match: Set the operator for this RuleExpressionVO
      if (RuleOperatorEnum.valueOfLabel(actual) != null) {
        operator = RuleOperatorEnum.valueOfLabel(actual);
        index++;
        continue;
      }

      // String match: Remove quotes and escape characters
      if (actual.startsWith("\"")) {
        args.add(
            actual.substring(1, actual.length() - 1).replace("\\\\", "\\").replace("\\\"", "\""));
        index++;
        continue;
      }

      // Keywords 'value' or 'this' match: Transform to keyword 'VALUE'
      if (actual.equals("value") || actual.equals("this")) {
        args.add("VALUE");
        index++;
        continue;
      }

      // Double match: Assume that double values contains '.' character
      if (actual.contains(".")) {
        args.add(Double.parseDouble(actual));
        index++;
        continue;
      }

      // Long match
      args.add(Long.parseLong(actual));
      index++;
    }

    // Compose the arguments depending on the operator
    if (operator != null) {
      switch (operator) {
        case NOT:
        case LEN:
          arg1 = args.get(0);
          break;
        default:
          arg1 = args.get(0);
          arg2 = args.get(1);
          break;
      }
    } else {
      LOG_ERROR.error("Error composing RuleExpressionVO: operator={}, args={}", operator, args);
    }
  }

  /**
   * Classifies a character: 0 (whitespace), 1 (number), 2 (logical operator), 3 (function
   * operator), 4 (end of expression) and 5 (other element)
   *
   * @param actual the actual
   * @param lastInputType the last input type
   * @return the int
   */
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
        return lastInputType == 1 ? 1 : 3;
      case ')':
        return 4;
      case 'E':
        return lastInputType == 1 ? 1 : 5;
      default:
        return 5;
    }
  }

  /**
   * Auxiliary method to string serialize recursively a RuleExpressionVO object.
   *
   * @param branch the branch
   * @return the string
   */
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

  /**
   * Serializes a RuleExpressionVO object into a Java string expression.
   *
   * @return the string
   */
  @Override
  public String toString() {
    if (operator != null) {
      switch (operator) {
        case NOT:
          return operator.getLabel() + "(" + arg1 + ")";
        case EQ:
        case DIST:
        case GT:
        case LT:
        case GTEQ:
        case LTEQ:
        case AND:
        case OR:
          return toStringBranch(arg1) + " " + operator.getLabel() + " " + toStringBranch(arg2);
        case LEN:
          return "value." + operator.getLabel() + "()";
        case SEQ:
        case SEQIC:
        case MATCH:
          return "value." + operator.getLabel() + "(" + toStringBranch(arg2) + ")";
        case EQ_DATE:
        case DIST_DATE:
        case GT_DATE:
        case LT_DATE:
        case GTEQ_DATE:
        case LTEQ_DATE:
          return "this." + operator.getLabel() + "(" + toStringBranch(arg2) + ")";
      }
    }

    LOG_ERROR.error("Error stringifying RuleExpressionVO: operator is null");
    throw new IllegalStateException("Operator cannot be null");
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(operator != null ? operator.ordinal() : null, arg1, arg2);
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
