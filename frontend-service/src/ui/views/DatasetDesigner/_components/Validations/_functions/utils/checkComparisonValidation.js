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
    } else if (ruleKey === 'expressions' && !isEmpty(candidateRule.expressions)) {
      if (checkComparisonExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey == 'relations' && !isEmpty(candidateRule.relations.links)) {
      if (checkComparisonRelation(candidateRule[ruleKey].links)) {
        isValidated = false;
      }
    }
  });

  return isValidated;
};
