import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { checkComparisonExpressions } from './checkComparisonExpressions';
import { checkComparisonRelation } from './checkComparisonRelation';

export const checkComparisonValidation = candidateRule => {
  let isValidated = true;

  const ruleKeys = Object.keys(candidateRule);

  const requiredFields = ['table', 'name', 'shortCode', 'errorMessage', 'errorLevel'];

  ruleKeys.forEach(ruleKey => {
    if (requiredFields.includes(ruleKey)) {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }

    if (
      ruleKey === 'expressions' &&
      !isEmpty(candidateRule.expressions) &&
      candidateRule.expressionType !== 'sqlSentence'
    ) {
      if (checkComparisonExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }

    if (
      ruleKey === 'relations' &&
      candidateRule.expressionType === 'fieldRelations' &&
      !isEmpty(candidateRule.relations.links)
    ) {
      if (checkComparisonRelation(candidateRule[ruleKey].links)) {
        isValidated = false;
      }
    }

    if (
      ruleKey === 'sqlSentence' &&
      candidateRule.expressionType === 'sqlSentence' &&
      (isNil(candidateRule['sqlSentence']) || isEmpty(candidateRule['sqlSentence']))
    ) {
      isValidated = false;
    }
  });

  return isValidated;
};
