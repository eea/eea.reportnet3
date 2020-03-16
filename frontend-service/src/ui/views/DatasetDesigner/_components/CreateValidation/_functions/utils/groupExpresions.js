import findIndex from 'lodash/findIndex';
import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

import { getEmptyExpresion } from './getEmptyExpresion';

export const groupExpresions = (expresions, groupExpresionsActive, groupCandidate) => {
  if (groupExpresionsActive >= 2) {
    //take position in array of the first expresion of the group
    const [firstId] = groupCandidate;
    const firstexpresionPosition = findIndex(expresions, expresion => expresion.expresionId == firstId);

    //get the expresions involved in the process
    const expresionsToGroup = expresions.filter(expresion => groupCandidate.includes(expresion.expresionId));

    // deactivate grouping check
    expresionsToGroup.forEach(expresionToGroup => {
      expresionToGroup.group = null;
    });

    // compose group expresion
    const newGroup = getEmptyExpresion();
    const [firstGroupExpresion] = expresionsToGroup;
    newGroup.union = firstGroupExpresion.union;
    newGroup.expresions = expresionsToGroup;

    // add to expresions in the order of the first expresions involved
    expresions.splice(firstexpresionPosition, 0, newGroup);

    //remove grouped elements from expresions array
    pullAllWith(expresions, expresionsToGroup, isEqual);

    return { expresions, newGroup };
  }
};
