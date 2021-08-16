import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { checkComparisonExpressions } from './checkComparisonExpressions';

export const checkComparisonValidationIfThen = candidateRule => {
  let isValidated = true;

  const ruleKeys = Object.keys(candidateRule);

  const requiredFields = ['table', 'name', 'shortCode', 'errorMessage', 'errorLevel'];

  ruleKeys.forEach(ruleKey => {
    if (requiredFields.includes(ruleKey)) {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey === 'expressionsIf' && !isEmpty(candidateRule.expressionsIf)) {
      if (checkComparisonExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey === 'expressionsThen' && !isEmpty(candidateRule.expressionsThen)) {
      if (checkComparisonExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }
  });

  return isValidated;
};
