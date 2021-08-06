import isEmpty from 'lodash/isEmpty';
import pullAt from 'lodash/pullAt';

import { isAGroup } from './isAGroup';

export const deleteExpressionRecursively = (expressionId, expressionCollection, isInAGroup = false) => {
  expressionCollection.forEach((expression, i) => {
    //the expression is not part of a group, is not a group, there are more than one expressions,
    //it matches and it is not the only one
    //delete expression
    if (
      expression.expressionId === expressionId &&
      expressionCollection.length > 1 &&
      !isInAGroup &&
      !isAGroup(expression)
    ) {
      pullAt(expressionCollection, i);
    }

    //the expression is part of a group, is not a group and it matches
    //delete expression
    if (expression.expressionId === expressionId && isInAGroup && !isAGroup(expression)) {
      pullAt(expressionCollection, i);
    }

    //the expression is a group, the ID of the expression does not match
    //continue searching for a match
    if (isAGroup(expression) && expression.expressionId !== expressionId) {
      const resultingExpressions = deleteExpressionRecursively(expressionId, expression.expressions, true);
      if (!isEmpty(resultingExpressions) && resultingExpressions.length < 2) {
        pullAt(expressionCollection, i);
        resultingExpressions.forEach((subexpression, index) => {
          if (index === 0) subexpression.union = expression.union;
          expressionCollection.splice(i + index, 0, subexpression);
        });
      }
    }

    //the expression is a group and it matches
    //ungroup and bring expressions to the group level
    if (isAGroup(expression) && expression.expressionId === expressionId) {
      const { expressions: subexpressions } = expression;
      pullAt(expressionCollection, i);
      subexpressions.forEach((subexpression, index) => {
        if (index === 0) subexpression.union = expression.union;
        expressionCollection.splice(i + index, 0, subexpression);
      });
    }
  });
  return expressionCollection;
};
