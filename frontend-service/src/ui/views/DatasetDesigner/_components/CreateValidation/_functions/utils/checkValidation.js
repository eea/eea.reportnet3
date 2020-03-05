import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { checkExpresions } from './checkExpresions';

export const checkValidation = candidateRule => {
  let isValidated = true;
  const ruleKeys = Object.keys(candidateRule);

  ruleKeys.forEach(ruleKey => {
    if (ruleKey != 'expresions' && ruleKey != 'active') {
      if (isNil(candidateRule[ruleKey]) || isEmpty(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (ruleKey == 'expresions') {
      if (checkExpresions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    }
  });
  return isValidated;
};
