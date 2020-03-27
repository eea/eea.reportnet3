import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { checkExpressions } from './checkExpressions';

export const checkValidation = candidateRule => {
  let isValidated = true;
  const ruleKeys = Object.keys(candidateRule);

  ruleKeys.forEach(ruleKey => {
    if (ruleKey != 'expressions' && ruleKey != 'active' && ruleKey != 'description') {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey == 'expressions') {
      if (checkExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }
  });
  return isValidated;
};
