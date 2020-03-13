import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

export const deleteExpresion = (expresionId, allExpresions) => {
  const [deleteCandidate] = allExpresions.filter(expresion => expresion.expresionId == expresionId);
  if (allExpresions.length > 1) {
    const remainRules = pullAllWith(allExpresions, [deleteCandidate], isEqual);
    return remainRules;
  } else {
    const expresionsKey = Object.keys(deleteCandidate);
    expresionsKey.forEach(expresionKey => {
      if (expresionKey != 'expresionId') {
        if (expresionKey == 'expresions') {
          deleteCandidate[expresionKey] = [];
        } else {
          deleteCandidate[expresionKey] = '';
        }
      }
    });
    return [deleteCandidate];
  }
};
