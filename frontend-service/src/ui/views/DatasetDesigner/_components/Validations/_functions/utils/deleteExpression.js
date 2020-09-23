import isEqual from 'lodash/isEqual';
import pullAllWith from 'lodash/pullAllWith';

export const deleteExpression = (expressionId, allExpressions) => {
  const [deleteCandidate] = allExpressions.filter(expression => expression.expressionId === expressionId);
  if (allExpressions.length > 1) {
    const remainRules = pullAllWith(allExpressions, [deleteCandidate], isEqual);
    return remainRules;
  } else {
    const expressionsKey = Object.keys(deleteCandidate);
    expressionsKey.forEach(expressionKey => {
      if (expressionKey !== 'expressionId') {
        if (expressionKey === 'expressions') {
          deleteCandidate[expressionKey] = [];
        } else {
          deleteCandidate[expressionKey] = '';
        }
      }
    });
    return [deleteCandidate];
  }
};
