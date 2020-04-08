import findIndex from 'lodash/findIndex';
import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';
import isNil from 'lodash/isNil';

import { getEmptyExpression } from './getEmptyExpression';

export const groupExpressions = (expressions, groupExpressionsActive, groupCandidate) => {
  if (groupExpressionsActive >= 2) {
    //take position in array of the first expression of the group
    const [firstId] = groupCandidate;
    const firstexpressionPosition = findIndex(expressions, expression => expression.expressionId == firstId);

    //get the expressions involved in the process
    const expressionsToGroup = expressions.filter(expression => groupCandidate.includes(expression.expressionId));

    // deactivate grouping check
    expressionsToGroup.forEach(expressionToGroup => {
      expressionToGroup.group = false;
    });

    // compose group expression
    const newGroup = getEmptyExpression();
    const [firstGroupExpression] = expressionsToGroup;
    if (!isNil(firstGroupExpression)) {
      newGroup.union = firstGroupExpression.union;
      newGroup.expressions = expressionsToGroup;

      // add to expressions in the order of the first expressions involved
      expressions.splice(firstexpressionPosition, 0, newGroup);

      //remove grouped elements from expressions array
      pullAllWith(expressions, expressionsToGroup, isEqual);

      return { expressions, newGroup };
    }
    return { expressions };
  }
};
