import findIndex from 'lodash/findIndex';
import pullAllWith from 'lodash/pullAllWith';
import isEqual from 'lodash/isEqual';

import { getEmptyExpresion } from './getEmptyExpresion';

export const groupExpresions = (rules, groupRulesActive, groupCandidate, dispatcher) => {
  if (groupRulesActive >= 2) {
    //take firs rule to group position in array

    const [firstId, restIds] = groupCandidate;
    const firstRulePosition = findIndex(rules, rule => rule.ruleId == firstId);

    //get to rules and remove from rules
    const rulesToGroup = rules.filter(rule => groupCandidate.includes(rule.ruleId));

    // compose group rule
    const newGroup = getEmptyExpresion();
    const [firstGroupRule] = rulesToGroup;
    newGroup.union = firstGroupRule.union;
    newGroup.rules = rulesToGroup;

    // add to rules in first rule to group position
    rules.splice(firstRulePosition, 0, newGroup);

    //remove groupedElements from array
    pullAllWith(rules, rulesToGroup, isEqual);

    dispatcher({
      type: 'UPDATE_RULES',
      payload: rules
    });
  }
};
