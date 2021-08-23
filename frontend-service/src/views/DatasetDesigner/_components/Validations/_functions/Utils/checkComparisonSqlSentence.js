import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const checkComparisonSqlSentence = sentence => {
  return !(isNil(sentence) || isEmpty(sentence));
};
