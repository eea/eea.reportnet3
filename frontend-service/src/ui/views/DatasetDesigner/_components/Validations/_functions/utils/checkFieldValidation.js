import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { checkExpressions } from './checkExpressions';

export const checkFieldValidation = candidateRule => {
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
      candidateRule.expressionType !== 'sqlSentence'
    ) {
      if (checkExpressions(candidateRule[ruleKey])) {
        isValidated = false;
      }
    } else if (
      ruleKey === 'expressions' &&
      candidateRule.expressionType === 'sqlSentence' &&
      (isNil(candidateRule['sqlSentence']) || isEmpty(candidateRule['sqlSentence']))
    ) {
      isValidated = false;
    }
  });

  return isValidated;
};
