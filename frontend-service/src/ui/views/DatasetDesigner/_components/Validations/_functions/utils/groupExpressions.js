import findIndex from 'lodash/findIndex';
import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';
import pullAllWith from 'lodash/pullAllWith';

import { getEmptyExpression } from './getEmptyExpression';

export const groupExpressions = (expressions, groupExpressionsActive, groupCandidate) => {
  if (groupExpressionsActive >= 2) {
    //take position in array of the first expression of the group
    const [firstId] = groupCandidate;
    const firstExpressionPosition = findIndex(expressions, expression => expression.expressionId === firstId);

    //get the expressions involved in the process
    const expressionsToGroup = expressions.filter(expression => groupCandidate.includes(expression.expressionId));

    if (expressionsToGroup.length > 0) {
      // deactivate grouping check
      expressionsToGroup.forEach(expressionToGroup => {
        expressionToGroup.group = false;
      });

      // compose group expression
      const newGroup = getEmptyExpression();
      const [firstGroupExpression] = expressionsToGroup;
      if (!isNil(firstGroupExpression)) {
        newGroup.union = firstGroupExpression.union;
        firstGroupExpression.union = '';
        newGroup.expressions = expressionsToGroup;

        // add to expressions in the order of the first expressions involved
        expressions.splice(firstExpressionPosition, 0, newGroup);

        //remove grouped elements from expressions array
        pullAllWith(expressions, expressionsToGroup, isEqual);

        return { expressions, newGroup };
      }
    } else {
      let returnGroup = null;
      expressions.forEach(expression => {
        if (expression.expressions.length > 0) {
          const returnedExpressions = groupExpressions(expression.expressions, groupExpressionsActive, groupCandidate);
          if (returnedExpressions) {
            const { expressions: newExpressions, newGroup } = returnedExpressions;
            expression.expressions = newExpressions;
            returnGroup = newGroup;
          }
        }
      });
      if (!isNil(returnGroup)) {
        return { expressions, newGroup: returnGroup };
      }
    }
    return { expressions };
  }
};
