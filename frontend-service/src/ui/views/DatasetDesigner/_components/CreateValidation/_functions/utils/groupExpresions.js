import findIndex from 'lodash/findIndex';
import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

import { getEmptyExpresion } from './getEmptyExpresion';

export const groupExpresions = (expresions, groupExpresionsActive, groupCandidate) => {
  console.info('#'.repeat(60));
  console.info('groupExpresions');
  console.info('expresions: ', expresions);
  console.info('groupExpresionsActive: ', groupExpresionsActive);
  console.info('groupCandidate: ', groupCandidate);
  console.info('-'.repeat(60));
  if (groupExpresionsActive >= 2) {
    //take position in array of the first expresion of the group
    const [firstId] = groupCandidate;
    const firstexpresionPosition = findIndex(expresions, expresion => expresion.expresionId == firstId);
    console.info('firstexpresionPosition', firstexpresionPosition);
    console.info('-'.repeat(60));

    //get the expresions involved in the process
    const expresionsToGroup = expresions.filter(expresion => groupCandidate.includes(expresion.expresionId));
    console.info('expresionsToGroup', expresionsToGroup);
    console.info('-'.repeat(60));

    // deactivate grouping check
    expresionsToGroup.forEach(expresionToGroup => {
      expresionToGroup.group = null;
    });

    // compose group expresion
    const newGroup = getEmptyExpresion();
    const [firstGroupExpresion] = expresionsToGroup;
    newGroup.union = firstGroupExpresion.union;
    newGroup.expresions = expresionsToGroup;
    console.info('newGroup', newGroup);
    console.info('-'.repeat(60));

    // add to expresions in the order of the first expresions involved
    expresions.splice(firstexpresionPosition, 0, newGroup);
    console.info('expresions', expresions);
    console.info('-'.repeat(60));

    //remove grouped elements from array
    pullAllWith(expresions, expresionsToGroup, isEqual);
    console.info('expresions', expresions);
    console.info('-'.repeat(60));

    return expresions;
  }
};
