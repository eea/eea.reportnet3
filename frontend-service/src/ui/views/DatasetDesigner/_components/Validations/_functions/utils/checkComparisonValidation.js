import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { checkComparisonExpressions } from './checkComparisonExpressions';

export const checkComparisonValidation = candidateRule => {
  let isValidated = true;
  const ruleKeys = Object.keys(candidateRule);
  const requiredFields = ['table', 'name', 'shortCode', 'errorMessage', 'errorLevel'];
  ruleKeys.forEach(ruleKey => {
    if (requiredFields.includes(ruleKey)) {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey == 'expressions' && !isEmpty(candidateRule.expressions)) {
      if (checkComparisonExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }
  });

  return isValidated;
};
