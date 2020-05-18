import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

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
    } else if (ruleKey == 'expressions' && !isEmpty(candidateRule.expressions)) {
      // console.log('checkValidation', candidateRule.expressions);

      if (checkExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }
  });

  return isValidated;
};
