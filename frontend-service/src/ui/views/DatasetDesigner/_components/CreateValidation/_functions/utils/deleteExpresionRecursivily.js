import isEmpty from 'lodash/isEmpty';
import pullAt from 'lodash/pullAt';

import { isAGroup } from './isAGroup';

export const deleteExpresionRecursivily = (expressionId, expressionColection, isInAGroup = false) => {
  expressionColection.forEach((expression, i) => {
    //the expression is not part of a group, is not a group, there are more than one expressions,
    //it matches and it is not the only one
    //delete expression
    if (
      expression.expressionId == expressionId &&
      expressionColection.length > 1 &&
      !isInAGroup &&
      !isAGroup(expression)
    ) {
      pullAt(expressionColection, i);
    }
    //the expression is part of a group, is not a group and it matches
    //delete expression
    if (expression.expressionId == expressionId && isInAGroup && !isAGroup(expression)) {
      pullAt(expressionColection, i);
    }

    //the expression is a group, the ID of the expression does not match
    //continue searching for a match
    if (isAGroup(expression) && expression.expressionId != expressionId) {
      const resultingExpressions = deleteExpresionRecursivily(expressionId, expression.expressions, true);
      if (!isEmpty(resultingExpressions) && resultingExpressions.length < 2) {
        pullAt(expressionColection, i);
        resultingExpressions.forEach((subexpression, index) => {
          expressionColection.splice(i + index, 0, subexpression);
        });
      }
    }

    //the expression is a group and it matches
    //ungroup and bring expressions to the group level
    if (isAGroup(expression) && expression.expressionId == expressionId) {
      const { expressions: subexpressions } = expression;
      pullAt(expressionColection, i);
      subexpressions.forEach((subexpression, index) => {
        expressionColection.splice(i + index, 0, subexpression);
      });
    }
  });
  return expressionColection;
};
