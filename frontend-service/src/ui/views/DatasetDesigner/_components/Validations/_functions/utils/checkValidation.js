import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { checkExpressions } from './checkExpressions';

export const checkValidation = candidateRule => {
  let isValidated = true;
  const ruleKeys = Object.keys(candidateRule);
  const requiredFields = ['table', 'name', 'field', 'shortCode', 'errorMessage', 'errorLevel'];

  ruleKeys.forEach(ruleKey => {
    if (requiredFields.includes(ruleKey)) {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (
      ruleKey === 'expressions' &&
      !isEmpty(candidateRule.expressions) &&
      candidateRule.expressionType !== 'SQLsentence'
    ) {
      if (checkExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }

    if (
      ruleKey === 'expressions' &&
      candidateRule.expressionType === 'SQLsentence' &&
      (isNil(candidateRule['SQLsentence']) || isEmpty(candidateRule['SQLsentence']))
    ) {
      isValidated = false;
    }
  });

  return isValidated;
};
