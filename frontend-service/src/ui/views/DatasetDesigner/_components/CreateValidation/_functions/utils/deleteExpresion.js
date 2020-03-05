import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

export const deleteExpresion = (expresionId, expresions) => {
  const [deleteCandidate] = expresions.filter(expresion => expresion.expresionId == expresionId);
  if (expresions.length > 1) {
    if (deleteCandidate.expresions.length === 0) {
      const remainRules = pullAllWith(expresions, [deleteCandidate], isEqual);
      return remainRules;
    }
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
  return [];
};
